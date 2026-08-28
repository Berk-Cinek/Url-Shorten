package com.berk.urlshorten.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

@Component
public class ApiCliRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port:8080}")
    private int port;

    @Override
    public void run(String... args) {
        Thread cliThread = new Thread(this::startCli);
        cliThread.setDaemon(true);
        cliThread.start();
    }

    private void startCli() {
        Scanner scanner = new Scanner(System.in);
        String baseUrl = "http://localhost:" + port;

        System.out.println("=== API CLI ready. Example: PUT /shorten/1 ===");
        System.out.println("Type 'exit' to stop the CLI (server keeps running).");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;

            String firstLine = scanner.nextLine().trim();
            if (firstLine.equalsIgnoreCase("exit")) break;
            if (firstLine.isEmpty()) continue;

            String[] parts = firstLine.split("\\s+", 2);
            if (parts.length < 2) {
                System.out.println("Format: METHOD /path   e.g. GET /shorten/1");
                continue;
            }

            String method = parts[0].toUpperCase();
            String path = parts[1];
            String body = null;

            if (method.equals("PUT") || method.equals("POST") || method.equals("PATCH")) {
                body = readJsonBody(scanner);
            }

            executeRequest(baseUrl, method, path, body);
        }
        System.out.println("CLI stopped.");
    }

    private String readJsonBody(Scanner scanner) {
        StringBuilder sb = new StringBuilder();
        int braceCount = 0;
        boolean started = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append("\n");

            for (char c : line.toCharArray()) {
                if (c == '{') { braceCount++; started = true; }
                if (c == '}') braceCount--;
            }
            if (started && braceCount == 0) break;
        }
        return sb.toString().trim();
    }

    private void executeRequest(String baseUrl, String method, String path, String body) {
        String url = baseUrl + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.valueOf(method), entity, String.class);
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
        } catch (HttpStatusCodeException e) {
            System.out.println("Status: " + e.getStatusCode());
            System.out.println("Body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}