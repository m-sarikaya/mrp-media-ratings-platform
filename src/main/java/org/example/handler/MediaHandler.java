package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.MediaEntry;
import org.example.domain.User;
import org.example.service.MediaService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MediaHandler implements HttpHandler {

    private final MediaService mediaService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MediaHandler(MediaService mediaService, UserService userService) {
        this.mediaService = mediaService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // CRUD Operationen
        switch (method) {
            case "GET":
                handleGet(exchange, path);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange, path);
                break;
            case "DELETE":
                handleDelete(exchange, path);
                break;
            default:
                sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // ========================================
    // GET - Medien abrufen
    // ========================================
    private void handleGet(HttpExchange exchange, String path) throws IOException {

        // GET /api/media → Alle Medien
        // GET /api/media/1 → Medium mit ID 1

        if (path.equals("/api/media")) {
            // Alle Medien
            List<MediaEntry> mediaList = mediaService.getAll();
            String json = objectMapper.writeValueAsString(mediaList);
            sendResponse(exchange, 200, json);

        } else {
            // Einzelnes Medium anhand ID
            try {
                int id = extractIdFromPath(path);
                MediaEntry media = mediaService.getById(id);
                String json = objectMapper.writeValueAsString(media);
                sendResponse(exchange, 200, json);

            } catch (Exception e) {
                sendResponse(exchange, 404, e.getMessage());
            }
        }
    }

    // ========================================
    // POST - Neues Medium erstellen
    // ========================================
    private void handlePost(HttpExchange exchange) throws IOException {

        // 1. User authentifizieren
        User user = authenticateUser(exchange);
        if (user == null) {
            sendResponse(exchange, 401, "Nicht eingeloggt");
            return;
        }

        // 2. JSON lesen
        InputStream requestBody = exchange.getRequestBody();
        MediaEntry media = objectMapper.readValue(requestBody, MediaEntry.class);

        try {
            // 3. Service aufrufen
            MediaEntry saved = mediaService.create(media, user.getId());

            // 4. Erfolg
            String json = objectMapper.writeValueAsString(saved);
            sendResponse(exchange, 201, json);

        } catch (Exception e) {
            sendResponse(exchange, 400, e.getMessage());
        }
    }

    // ========================================
    // PUT - Medium aktualisieren
    // ========================================
    private void handlePut(HttpExchange exchange, String path) throws IOException {

        // 1. User authentifizieren
        User user = authenticateUser(exchange);
        if (user == null) {
            sendResponse(exchange, 401, "Nicht eingeloggt");
            return;
        }

        // 2. ID aus Pfad extrahieren
        int id;
        try {
            id = extractIdFromPath(path);
        } catch (Exception e) {
            sendResponse(exchange, 400, "Ungültige ID");
            return;
        }

        // 3. JSON lesen
        InputStream requestBody = exchange.getRequestBody();
        MediaEntry updatedMedia = objectMapper.readValue(requestBody, MediaEntry.class);

        try {
            // 4. Service aufrufen
            MediaEntry result = mediaService.update(id, updatedMedia, user.getId());

            // 5. Erfolg
            String json = objectMapper.writeValueAsString(result);
            sendResponse(exchange, 200, json);

        } catch (Exception e) {
            sendResponse(exchange, 403, e.getMessage());
        }
    }

    // ========================================
    // DELETE - Medium löschen
    // ========================================
    private void handleDelete(HttpExchange exchange, String path) throws IOException {

        // 1. User authentifizieren
        User user = authenticateUser(exchange);
        if (user == null) {
            sendResponse(exchange, 401, "Nicht eingeloggt");
            return;
        }

        // 2. ID aus Pfad extrahieren
        int id;
        try {
            id = extractIdFromPath(path);
        } catch (Exception e) {
            sendResponse(exchange, 400, "Ungültige ID");
            return;
        }

        try {
            // 3. Service aufrufen
            mediaService.delete(id, user.getId());

            // 4. Erfolg
            sendResponse(exchange, 200, "Medium gelöscht");

        } catch (Exception e) {
            sendResponse(exchange, 403, e.getMessage());
        }
    }

    // ========================================
    // HILFSMETHODE - User anhand Token authentifizieren
    // ========================================
    private User authenticateUser(HttpExchange exchange) {
        // Token aus Header lesen
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // "Bearer max-mrpToken" → "max-mrpToken"
        String token = authHeader.substring(7);

        // User anhand Token finden
        return userService.findByToken(token);
    }

    // ========================================
    // HILFSMETHODE - ID aus Pfad extrahieren
    // ========================================
    private int extractIdFromPath(String path) throws Exception {
        // /api/media/5 → 5
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
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
