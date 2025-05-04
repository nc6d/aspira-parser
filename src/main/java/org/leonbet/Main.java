package org.leonbet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.leonbet.client.ApiClient;
import org.leonbet.client.LeonBetsApiClient;
import org.leonbet.config.AppConfig;
import org.leonbet.service.SportScraperService;
import org.leonbet.util.PrinterUtils;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        if (AppConfig.PRINT_TO_FILE) {
            String outputFilePath = AppConfig.UTC_FORMATTER.format(java.time.Instant.now()) + ".txt";
            PrinterUtils.initFile(outputFilePath);
        }

        ExecutorService executor = Executors.newFixedThreadPool(AppConfig.MAX_THREADS);
        HttpClient httpClient = HttpClient.newBuilder().executor(executor).build();
        ObjectMapper mapper = new ObjectMapper();
        
        ApiClient apiClient = new LeonBetsApiClient(httpClient, mapper, executor);
        SportScraperService scraperService = new SportScraperService(apiClient, executor);
        
        scraperService.start();
    }
}



