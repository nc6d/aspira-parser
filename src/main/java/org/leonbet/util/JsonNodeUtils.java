package org.leonbet.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.leonbet.config.AppConfig;

public final class JsonNodeUtils {
    private JsonNodeUtils() {} // Prevent instantiation
    
    public static boolean isSportSupported(JsonNode sportNode) {
        return AppConfig.TARGET_SPORTS.contains(sportNode.path("name").asText());
    }
    
    public static boolean isTopLeague(JsonNode leagueNode) {
        return leagueNode.path("top").asBoolean();
    }
    
    public static String getSportName(JsonNode sportNode) {
        return sportNode.path("name").asText();
    }
    
    public static String getLeagueName(JsonNode leagueNode) {
        return leagueNode.path("name").asText();
    }
    
    public static long getLeagueId(JsonNode leagueNode) {
        return leagueNode.path("id").asLong();
    }
    
    public static long getEventId(JsonNode eventNode) {
        return eventNode.path("id").asLong();
    }
} 