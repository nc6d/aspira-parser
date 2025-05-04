package org.leonbet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.leonbet.client.ApiClient;
import org.leonbet.client.LeonBetsApiClient;
import org.leonbet.config.AppConfig;
import org.leonbet.service.SportScraperService;
import org.leonbet.util.BenchmarkUtils;
import org.leonbet.util.PrinterUtils;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            processArguments(args);
        }

        if (AppConfig.isPrintToFile()) {
            String outputFilePath = AppConfig.getDateFormatter().format(Instant.now()) + ".txt";
            PrinterUtils.initFile(outputFilePath);
        }

        ExecutorService executor = Executors.newFixedThreadPool(AppConfig.MAX_THREADS);
        HttpClient httpClient = HttpClient.newBuilder().executor(executor).build();
        ObjectMapper mapper = new ObjectMapper();
        
        ApiClient apiClient = new LeonBetsApiClient(httpClient, mapper, executor);
        SportScraperService scraperService = new SportScraperService(apiClient, executor);
        
        scraperService.start();
    }

    private static void processArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--print-to-file":
                    AppConfig.setPrintToFile(true);
                    break;
                case "--no-print-to-file":
                    AppConfig.setPrintToFile(false);
                    break;
                case "--reports-dir":
                    if (i + 1 < args.length) {
                        AppConfig.setReportsDir(args[++i]);
                    }
                    break;
                case "--timezone":
                    if (i + 1 < args.length) {
                        AppConfig.setTimeZone(args[++i]);
                    }
                    break;
                case "--benchmark":
                    BenchmarkUtils.setEnabled(true);
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    System.err.println("Unknown argument: " + arg);
                    printHelp();
                    System.exit(1);
            }
        }
    }

    private static void printHelp() {
        System.out.println("Usage: run.sh [options]");
        System.out.println("Options:");
        System.out.println("  --print-to-file        Enable writing output to a file");
        System.out.println("  --no-print-to-file     Disable writing output to a file (default)");
        System.out.println("  --reports-dir <dir>    Set custom directory for report files");
        System.out.println("  --timezone <zone>      Set timezone (e.g., UTC, Europe/London)");
        System.out.println("  --benchmark            Enable performance benchmarking");
        System.out.println("  --force-rebuild        Force rebuild the project");
        System.out.println("  --help                 Show this help message");
    }
}



