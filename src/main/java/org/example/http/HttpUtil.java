package org.example.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public final class HttpUtil {
    // Ein ObjectMapper fuer alle Handler (JSON <-> Java).
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private HttpUtil() {
        // Utility-Klasse; keine Instanz erlauben.
    }

    // JSON-Body lesen und in ein Objekt mappen.
    public static <T> T readJson(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            return MAPPER.readValue(requestBody, clazz);
        }
    }


    // Beliebiges Objekt als JSON zuruecksenden.
    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] data = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    // Standardisiertes Error-JSON: {"error": "..."}.
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, Map.of("error", message));
    }

    // Antwort ohne Body senden (z. B. 204).
    public static void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
    }

    // Text senden (nur fuer einfache Test-Endpoints).
    public static void sendText(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    // Query-Parameter aus der URL lesen (z. B. ?title=abc&year=2020).
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }
}
