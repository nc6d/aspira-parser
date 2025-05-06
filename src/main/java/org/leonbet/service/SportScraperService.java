package org.leonbet.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.leonbet.client.ApiClient;
import org.leonbet.config.AppConfig;
import org.leonbet.util.BenchmarkUtils;
import org.leonbet.util.JsonNodeUtils;
import org.leonbet.util.PrinterUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SportScraperService {
    private final ApiClient apiClient;
    private final ExecutorService executor;

    public SportScraperService(ApiClient apiClient, ExecutorService executor) {
        this.apiClient = apiClient;
        this.executor = executor;
    }

    // TODO: Needs to clarify - don't sure if sports parallel processing is allowed.
    public void start() {
        long startTime = System.currentTimeMillis();
        try {
            apiClient.fetchSports()
                    .thenApplyAsync(json -> json.isArray() ? json : null, executor)
                    .thenAcceptAsync(this::processSports, executor)
                    .whenComplete((v, t) -> {
                        if (t != null) {
                            System.err.println("Error in processing: " + t.getMessage());
                        }
                        executor.shutdown();
                        if (AppConfig.isPrintToFile()) {
                            PrinterUtils.closeFile();
                        }
                    })
                    .join();
        } finally {
            BenchmarkUtils.record("Total Execution", startTime);
            if (BenchmarkUtils.isEnabled()) {
                BenchmarkUtils.printReport();
            }
        }
    }

    private void processSports(JsonNode sportsArray) {
        if (sportsArray == null) return;

        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> leagueFutures = new ArrayList<>();

        for (JsonNode sport : sportsArray) {
            if (!JsonNodeUtils.isSportSupported(sport)) continue;

            String sportName = JsonNodeUtils.getSportName(sport);
            for (JsonNode region : sport.path("regions")) {
                for (JsonNode league : region.path("leagues")) {
                    if (!JsonNodeUtils.isTopLeague(league)) continue;

                    long leagueId = JsonNodeUtils.getLeagueId(league);
                    String leagueName = JsonNodeUtils.getLeagueName(league);

                    leagueFutures.add(processLeague(sportName, leagueName, leagueId));
                }
            }
        }

        CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0])).join();
        BenchmarkUtils.record("Process Sports", startTime);
    }

    private CompletableFuture<Void> processLeague(String sportName, String leagueName, long leagueId) {
        long startTime = System.currentTimeMillis();
        return apiClient.fetchLeagueEvents(leagueId)
                .thenApplyAsync(json -> json.path("events").isArray() ? json.path("events") : null, executor)
                .thenComposeAsync(eventsArray -> {
                    if (eventsArray == null) return CompletableFuture.completedFuture(null);

                    List<JsonNode> events = new ArrayList<>();
                    for (JsonNode event : eventsArray) {
                        if (events.size() >= AppConfig.MAX_MATCHES
                                || !"prematch".equalsIgnoreCase(event.path("betline").asText())) break;
                        events.add(event);
                    }

                    List<CompletableFuture<Void>> matchFutures = events.stream()
                            .map(event -> {
                                long eventId = JsonNodeUtils.getEventId(event);
                                return apiClient.fetchEventDetails(eventId)
                                        .thenAccept(fullEvent -> printMatch(sportName, leagueName, fullEvent));
                            })
                            .toList();

                    return CompletableFuture.allOf(matchFutures.toArray(new CompletableFuture[0]))
                            .whenComplete((v, t) -> BenchmarkUtils.record("Process League " + leagueName, startTime));
                }, executor);
    }

    private void printMatch(String sport, String league, JsonNode event) {
        long startTime = System.currentTimeMillis();
        String matchName = event.path("name").asText();
        long kickoffTimestamp = event.path("kickoff").asLong();
        long matchId = JsonNodeUtils.getEventId(event);

        Instant kickoffInstant = kickoffTimestamp > 9999999999L
                ? Instant.ofEpochMilli(kickoffTimestamp)
                : Instant.ofEpochSecond(kickoffTimestamp);

        String kickoffTime = AppConfig.getDateFormatter().format(kickoffInstant);

        // TODO: Needs to clarify - should we group outcomes by its market name?
        PrinterUtils.print(0, sport + ", " + league);
        PrinterUtils.print(1, matchName + ", " + kickoffTime + ", " + matchId);

        JsonNode markets = event.path("markets");
        if (markets.isArray()) {
            for (JsonNode market : markets) {
                String marketName = market.path("name").asText();
                PrinterUtils.print(2, marketName);

                JsonNode runners = market.path("runners");
                if (runners.isArray()) {
                    for (JsonNode runner : runners) {
                        String outcomeName = runner.path("name").asText();
                        double outcomePrice = runner.path("price").asDouble();
                        long outcomeId = runner.path("id").asLong();
                        PrinterUtils.print(3, outcomeName + ", " + outcomePrice + ", " + outcomeId);
                    }
                }
            }
        }
        PrinterUtils.print(0, "");
        BenchmarkUtils.record("Print Match " + matchName, startTime);
    }
} 