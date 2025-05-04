package org.leonbet.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.leonbet.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class LeonBetsApiClient implements ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final ExecutorService executor;

    public LeonBetsApiClient(HttpClient httpClient, ObjectMapper mapper, ExecutorService executor) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<JsonNode> fetchSports() {
        return sendRequest(ApiUrlBuilder.sports().build());
    }

    @Override
    public CompletableFuture<JsonNode> fetchLeagueEvents(long leagueId) {
        return sendRequest(ApiUrlBuilder.leagueEvents(leagueId).build());
    }

    @Override
    public CompletableFuture<JsonNode> fetchEventDetails(long eventId) {
        return sendRequest(ApiUrlBuilder.eventDetails(eventId).build());
    }

    private CompletableFuture<JsonNode> sendRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (compatible; LeonScraper/1.0)")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body, executor)
                .thenApplyAsync(body -> {
                    try {
                        return mapper.readTree(body);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, executor);
    }
} 