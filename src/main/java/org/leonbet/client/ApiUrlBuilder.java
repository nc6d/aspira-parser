package org.leonbet.client;

import org.leonbet.config.AppConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiUrlBuilder {
    private static final String DEFAULT_CTAG = "en-US";
    
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> flags;

    private ApiUrlBuilder(String path) {
        this.path = path;
        this.queryParams = new HashMap<>();
        this.flags = new HashMap<>();
        this.queryParams.put("ctag", DEFAULT_CTAG);
    }

    public static ApiUrlBuilder sports() {
        return new ApiUrlBuilder("/betline/sports")
                .addFlag("urlv2");
    }

    public static ApiUrlBuilder leagueEvents(long leagueId) {
        ApiUrlBuilder builder = new ApiUrlBuilder("/betline/events/all")
                .addQueryParam("league_id", String.valueOf(leagueId))
                .addQueryParam("hideClosed", "true");
        
        AppConfig.COMMON_API_FLAGS.forEach(builder::addFlag);
        return builder;
    }

    public static ApiUrlBuilder eventDetails(long eventId) {
        ApiUrlBuilder builder = new ApiUrlBuilder("/betline/event/all")
                .addQueryParam("eventId", String.valueOf(eventId));
        
        AppConfig.COMMON_API_FLAGS.forEach(builder::addFlag);
        AppConfig.EVENT_DETAIL_FLAGS.forEach(builder::addFlag);
        return builder;
    }

    public ApiUrlBuilder addQueryParam(String key, String value) {
        queryParams.put(key, value);
        return this;
    }

    public ApiUrlBuilder addFlag(String flag) {
        flags.put(flag, "");
        return this;
    }

    public String build() {
        StringBuilder url = new StringBuilder(AppConfig.BASE_API_URL).append(path);
        
        String queryString = queryParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
                
        if (!flags.isEmpty()) {
            queryString += (queryString.isEmpty() ? "" : "&") + "flags=" + 
                    String.join(",", flags.keySet());
        }

        if (!queryString.isEmpty()) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }
} 