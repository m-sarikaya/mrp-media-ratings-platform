package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.MediaEntry;
import org.example.domain.User;
import org.example.http.HttpUtil;
import org.example.service.MediaService;

import java.io.IOException;
import java.util.List;

// Handler fuer /api/media und /api/media/{id}.
public class MediaHandler implements HttpHandler {
    // Business-Logik fuer Media.
    private final MediaService mediaService;

    public MediaHandler(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // Zentrales Routing nach HTTP-Methode.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

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
                HttpUtil.sendError(exchange, 405, "Method Not Allowed");
        }
    }

    // GET /api/media oder /api/media/{id}
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/media")) {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || query.isBlank()) {
                List<MediaEntry> list = mediaService.getAll();
                HttpUtil.sendJson(exchange, 200, list);
                return;
            }

            // Query-Parameter auslesen und filtern.
            var params = HttpUtil.parseQuery(query);
            String title = params.get("title");
            String genre = params.get("genre");
            String mediaType = params.get("mediaType");
            Integer releaseYear = parseInt(params.get("releaseYear"));
            Integer ageRestriction = parseInt(params.get("ageRestriction"));
            Double rating = parseDouble(params.get("rating"));
            String sortBy = params.get("sortBy");

            List<MediaEntry> list = mediaService.search(title, genre, mediaType, releaseYear, ageRestriction, rating, sortBy);
            HttpUtil.sendJson(exchange, 200, list);
            return;
        }

        try {
            int id = extractIdFromPath(path);
            MediaEntry media = mediaService.getById(id);
            HttpUtil.sendJson(exchange, 200, media);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 404, e.getMessage());
        }
    }

    // POST /api/media
    private void handlePost(HttpExchange exchange) throws IOException {
        User authUser = (User) exchange.getAttribute("authUser");
        if (authUser == null) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }

        MediaEntry media = HttpUtil.readJson(exchange, MediaEntry.class);
        try {
            MediaEntry saved = mediaService.create(media, authUser.getId());
            HttpUtil.sendJson(exchange, 201, saved);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // PUT /api/media/{id}
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        User authUser = (User) exchange.getAttribute("authUser");
        if (authUser == null) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }

        int id;
        try {
            id = extractIdFromPath(path);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, "Ungueltige ID");
            return;
        }

        MediaEntry updated = HttpUtil.readJson(exchange, MediaEntry.class);
        try {
            MediaEntry result = mediaService.update(id, updated, authUser.getId());
            HttpUtil.sendJson(exchange, 200, result);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 403, e.getMessage());
        }
    }

    // DELETE /api/media/{id}
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        User authUser = (User) exchange.getAttribute("authUser");
        if (authUser == null) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }

        int id;
        try {
            id = extractIdFromPath(path);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, "Ungueltige ID");
            return;
        }

        try {
            mediaService.delete(id, authUser.getId());
            HttpUtil.sendEmpty(exchange, 204);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 403, e.getMessage());
        }
    }

    // /api/media/5 -> 5
    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    // Hilfsmethode für optionale Integer-Parameter.
    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Hilfsmethode für optionale Double-Parameter.
    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
