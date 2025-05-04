package org.leonbet.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;

public interface ApiClient {
    CompletableFuture<JsonNode> fetchSports();
    CompletableFuture<JsonNode> fetchLeagueEvents(long leagueId);
    CompletableFuture<JsonNode> fetchEventDetails(long eventId);
} 