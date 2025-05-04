package org.leonbet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final List<String> TARGET_SPORTS = List.of("Football", "Tennis", "Ice Hockey", "Basketball");
    private static final int MAX_MATCHES = 2;
    private static final int MAX_THREADS = 3;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    private static final HttpClient httpClient = HttpClient.newBuilder().executor(executor).build();

    private static final DateTimeFormatter UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    private static final boolean PRINT_TO_FILE = false;
    private static final String OUTPUT_FILE_PATH = UTC_FORMATTER.format( Instant.now()) + ".txt";


    public static void main(String[] args) {
        if (PRINT_TO_FILE) {
            PrinterUtils.initFile(OUTPUT_FILE_PATH);
        }

        new Main().start();
    }

    public void start() {
        fetchJson("https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2")
                .thenApplyAsync(json -> {
                    System.out.println(Thread.currentThread().getName());
                    return json.isArray() ? json : null;
                }, executor)
                .thenAcceptAsync(sportsArray -> {
                    System.out.println(Thread.currentThread().getName());
                    if (sportsArray == null) return;

                    List<CompletableFuture<Void>> leagueFutures = new ArrayList<>();

                    for (JsonNode sport : sportsArray) {
                        String sportName = sport.path("name").asText();
                        if (!TARGET_SPORTS.contains(sportName)) continue;

                        for (JsonNode region : sport.path("regions")) {
                            for (JsonNode league : region.path("leagues")) {
                                if (!league.path("top").asBoolean()) continue;

                                long leagueId = league.path("id").asLong();
                                String leagueName = league.path("name").asText();

                                leagueFutures.add(processLeague(sportName, leagueName, leagueId));
                            }
                        }
                    }

                    CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]))
                            .whenComplete((v, t) -> {
                                if (t != null) System.err.println("Error in processing: " + t.getMessage());
                                executor.shutdown();

                                if (PRINT_TO_FILE) {
                                    PrinterUtils.closeFile();
                                }
                            });
                }, executor).join();
    }

    private CompletableFuture<Void> processLeague(String sportName, String leagueName, long leagueId) {
        String url = String.format("https://leonbets.com/api-2/betline/events/all?ctag=en-US&league_id=%d&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup", leagueId);
        return fetchJson(url)
                .thenApplyAsync(json -> {
                    System.out.println(Thread.currentThread().getName());
                    return json.path("events").isArray() ? json.path("events") : null;
                }, executor)
                .thenComposeAsync(eventsArray -> {
                    System.out.println(Thread.currentThread().getName());

                    if (eventsArray == null) return CompletableFuture.completedFuture(null);

                    List<JsonNode> events = new ArrayList<>();
                    for (JsonNode event : eventsArray) {
                        if (events.size() >= MAX_MATCHES) break;
                        events.add(event);
                    }

                    List<CompletableFuture<Void>> matchFutures = events.stream()
                            .map(event -> {
                                long eventId = event.path("id").asLong();
                                return fetchJson("https://leonbets.com/api-2/betline/event/all?ctag=en-US&eventId="
                                        + eventId +
                                        "&flags=reg,urlv2,mm2,rrc,nodup,smgv2,outv2,wd2,dar")
                                        .thenAccept(fullEvent -> printMatch(sportName, leagueName, fullEvent));
                            })
                            .toList();

                    return CompletableFuture.allOf(matchFutures.toArray(new CompletableFuture[0]));
                }, executor);
    }

    private void printMatch(String sport, String league, JsonNode event) {
        String matchName = event.path("name").asText();
        long kickoffTimestamp = event.path("kickoff").asLong();
        long matchId = event.path("id").asLong();

        Instant kickoffInstant = kickoffTimestamp > 9999999999L
                ? Instant.ofEpochMilli(kickoffTimestamp)
                : Instant.ofEpochSecond(kickoffTimestamp);

        String kickoffTime = UTC_FORMATTER.format(kickoffInstant);

        synchronized (System.out) {
            PrinterUtils.print(0, sport + ", " + league, PRINT_TO_FILE);
            PrinterUtils.print(1, matchName + ", " + kickoffTime + ", " + matchId, PRINT_TO_FILE);

            JsonNode markets = event.path("markets");
            if (markets.isArray()) {
                for (JsonNode market : markets) {
                    String marketName = market.path("name").asText();
                    PrinterUtils.print(2, marketName, PRINT_TO_FILE);

                    JsonNode runners = market.path("runners");
                    if (runners.isArray()) {
                        for (JsonNode runner : runners) {
                            String outcomeName = runner.path("name").asText();
                            double outcomePrice = runner.path("price").asDouble();
                            long outcomeId = runner.path("id").asLong();
                            PrinterUtils.print(3, outcomeName + ", " + outcomePrice + ", " + outcomeId, PRINT_TO_FILE);
                        }
                    }
                }
            }
            System.out.println();
        }
    }

    private static CompletableFuture<JsonNode> fetchJson(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (compatible; LeonScraper/1.0)")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(stringHttpResponse -> {
                    System.out.println(Thread.currentThread().getName());
                    return stringHttpResponse.body();
                }, executor)
                .thenApplyAsync(body -> {
                    System.out.println(Thread.currentThread().getName());
                    try {
                        return mapper.readTree(body);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, executor);
    }
}



