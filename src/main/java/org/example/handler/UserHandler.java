package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.User;
import org.example.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UserHandler implements HttpHandler {

    // Service für Business-Logik
    private final UserService userService;

    // ObjectMapper wandelt JSON ↔ Java Objekte um
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructor: Bekommt den Service (nicht mehr das Repository!)
    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("POST")) {

            if (path.equals("/api/users/login")) {
                handleLogin(exchange);
            } else {
                handleRegistration(exchange);
            }

        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // ========================================
    // REGISTRATION - nur HTTP, keine Business-Logik
    // ========================================
    private void handleRegistration(HttpExchange exchange) throws IOException {

        // 1. JSON lesen
        InputStream requestBody = exchange.getRequestBody();
        User user = objectMapper.readValue(requestBody, User.class);

        try {
            // 2. Service aufrufen (der macht die Business-Logik)
            User savedUser = userService.register(user);

            // 3. Erfolg - User zurückschicken
            String jsonResponse = objectMapper.writeValueAsString(savedUser);
            sendResponse(exchange, 201, jsonResponse);

        } catch (Exception e) {
            // Fehler vom Service - Fehlermeldung zurückschicken
            sendResponse(exchange, 400, e.getMessage());
        }
    }

    // ========================================
    // LOGIN - nur HTTP, keine Business-Logik
    // ========================================
    private void handleLogin(HttpExchange exchange) throws IOException {

        // 1. JSON lesen
        InputStream requestBody = exchange.getRequestBody();
        User loginData = objectMapper.readValue(requestBody, User.class);

        try {
            // 2. Service aufrufen (der macht die Business-Logik)
            String token = userService.login(loginData.getUsername(), loginData.getPassword());

            // 3. Erfolg - Token zurückschicken
            String jsonResponse = "{\"token\": \"" + token + "\"}";
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            // Fehler vom Service - Fehlermeldung zurückschicken
            sendResponse(exchange, 401, e.getMessage());
        }
    }

    // ========================================
    // HILFSMETHODE - Antwort senden
    // ========================================
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
