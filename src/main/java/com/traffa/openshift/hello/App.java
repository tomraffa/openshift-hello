package com.traffa.openshift.hello;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class App {

    private static final int DEFAULT_PORT = 8080;

    private App() {
        // Utility class
    }

    public static void main(String[] args) throws IOException {
        int port = getPort();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", App::handleRoot);
        server.createContext("/hello", App::handleHello);
        server.createContext("/health", App::handleHealth);

        server.setExecutor(null);
        server.start();

        System.out.printf(
            "OpenShift Hello service started on port %d%n",
            port
        );
    }

    private static void handleRoot(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        sendJson(
            exchange,
            200,
            """
            {
              "service": "openshift-hello",
              "endpoints": ["/hello", "/health"]
            }
            """
        );
    }

    private static void handleHello(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String hostname = System.getenv().getOrDefault(
            "HOSTNAME",
            "local-development"
        );

        String response = """
            {
              "message": "Hello from the updated MicroShift pipeline",
              "version": "1.0.1",
              "hostname": "%s",
              "timestamp": "%s"
            }
            """.formatted(
                escapeJson(hostname),
                Instant.now()
            );

        sendJson(exchange, 200, response);
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        sendJson(
            exchange,
            200,
            """
            {
              "status": "UP"
            }
            """
        );
    }

    private static int getPort() {
        String configuredPort = System.getenv("PORT");

        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(configuredPort);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                "PORT must be a valid integer: " + configuredPort,
                exception
            );
        }
    }

    private static void sendJson(
        HttpExchange exchange,
        int statusCode,
        String response
    ) throws IOException {
        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        sendResponse(exchange, statusCode, response);
    }

    private static void sendResponse(
        HttpExchange exchange,
        int statusCode,
        String response
    ) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }

    private static String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }
}