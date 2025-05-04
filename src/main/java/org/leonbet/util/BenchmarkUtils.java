package org.leonbet.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class BenchmarkUtils {
    private static final Map<String, AtomicLong> totalTime = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<String, AtomicLong> callCount = Collections.synchronizedMap(new LinkedHashMap<>());
    private static boolean enabled = false;

    // Column widths for table formatting
    private static final int OPERATION_COL_WIDTH = 75;
    private static final int CALLS_COL_WIDTH = 10;
    private static final int TOTAL_TIME_COL_WIDTH = 15;
    private static final int AVG_TIME_COL_WIDTH = 15;
    private static final String SEPARATOR = "+";
    private static final String HORIZONTAL_LINE = "-";

    private BenchmarkUtils() {} // Prevent instantiation

    public static void setEnabled(boolean value) {
        if (value != enabled) {
            enabled = value;
            if (enabled) {
                reset();
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void reset() {
        if (!enabled) return;
        synchronized (totalTime) {
            totalTime.clear();
            callCount.clear();
        }
    }

    public static void record(String operation, long startTime) {
        if (!enabled) return;
        
        long duration = System.currentTimeMillis() - startTime;
        synchronized (totalTime) {
            totalTime.computeIfAbsent(operation, k -> new AtomicLong()).addAndGet(duration);
            callCount.computeIfAbsent(operation, k -> new AtomicLong()).incrementAndGet();
        }
    }

    private static String createHorizontalLine() {
        return SEPARATOR +
               HORIZONTAL_LINE.repeat(OPERATION_COL_WIDTH) + SEPARATOR +
               HORIZONTAL_LINE.repeat(CALLS_COL_WIDTH) + SEPARATOR +
               HORIZONTAL_LINE.repeat(TOTAL_TIME_COL_WIDTH) + SEPARATOR +
               HORIZONTAL_LINE.repeat(AVG_TIME_COL_WIDTH) + SEPARATOR;
    }

    private static String formatRow(String operation, String calls, String totalTime, String avgTime) {
        return String.format("| %-" + (OPERATION_COL_WIDTH-1) + "s" +
                           "| %" + (CALLS_COL_WIDTH-1) + "s" +
                           "| %" + (TOTAL_TIME_COL_WIDTH-1) + "s" +
                           "| %" + (AVG_TIME_COL_WIDTH-1) + "s|",
                           operation, calls, totalTime, avgTime);
    }

    public static void printReport() {
        if (!enabled || totalTime.isEmpty()) return;

        System.out.println("\n=== Benchmark Report ===");
        
        String horizontalLine = createHorizontalLine();
        System.out.println(horizontalLine);
        System.out.println(formatRow("Operation", "Calls", "Total Time(ms)", "Avg Time(ms)"));
        System.out.println(horizontalLine);

        synchronized (totalTime) {
            totalTime.forEach((operation, time) -> {
                long calls = callCount.get(operation).get();
                double avgTime = (double) time.get() / calls;
                System.out.println(formatRow(
                    operation,
                    String.valueOf(calls),
                    String.valueOf(time.get()),
                    String.format("%.2f", avgTime)
                ));
            });
        }
        
        System.out.println(horizontalLine);
        System.out.println();
    }
} 