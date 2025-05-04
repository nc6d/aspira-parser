package org.leonbet.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public final class AppConfig {
    public static final List<String> TARGET_SPORTS = List.of("Football", "Tennis", "Ice Hockey", "Basketball");
    public static final int MAX_MATCHES = 2;
    public static final int MAX_THREADS = 3;
    public static final String BASE_API_URL = "https://leonbets.com/api-2";
    
    private static boolean PRINT_TO_FILE = false;
    private static String reportsDir = "reports";
    private static ZoneId timeZone = ZoneId.of("UTC");
    private static DateTimeFormatter UTC_FORMATTER;
    
    static {
        updateDateFormatter();
    }
    
    private static void updateDateFormatter() {
        UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(timeZone);
    }
    
    public static boolean isPrintToFile() {
        return PRINT_TO_FILE;
    }
    
    public static void setPrintToFile(boolean value) {
        PRINT_TO_FILE = value;
    }
    
    public static String getReportsDir() {
        return reportsDir;
    }
    
    public static void setReportsDir(String dir) {
        reportsDir = dir;
    }
    
    public static void setTimeZone(String zoneId) {
        timeZone = ZoneId.of(zoneId);
        updateDateFormatter();
    }
    
    public static DateTimeFormatter getDateFormatter() {
        return UTC_FORMATTER;
    }

    public static final Set<String> COMMON_API_FLAGS = Set.of(
            "reg",      // Regular markets
            "urlv2",    // URL ver-2
            "mm2",      // Market metadata ver-2
            "rrc",      // Real-time changes
            "nodup"     // No duplicates
    );
    
    public static final Set<String> EVENT_DETAIL_FLAGS = Set.of(
            "smgv2",    // Special markets group ver-2
            "outv2",    // Outcomes ver-2
            "wd2",      // Withdrawal ver-2
            "dar"       // Direct access to runners
    );
    
    private AppConfig() {} // Prevent instantiation
} 