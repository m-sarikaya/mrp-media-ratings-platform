package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.User;
import org.example.http.HttpUtil;
import org.example.service.FavoriteService;

import java.io.IOException;

// Handler fuer /api/media/{id}/favorite (markieren/entfernen).
public class FavoritesHandler implements HttpHandler {
    // Service fuer Favoriten-Logik.
    private final FavoriteService favoriteService;

    public FavoritesHandler(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Routing fuer POST und DELETE.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        int mediaId;
        try {
            mediaId = extractMediaId(path);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, "Ungueltige Media-ID");
            return;
        }

        User authUser = (User) exchange.getAttribute("authUser");
        if (authUser == null) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }

        if (method.equals("POST")) {
            try {
                favoriteService.addFavorite(authUser.getId(), mediaId);
                HttpUtil.sendEmpty(exchange, 200);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, e.getMessage());
            }
            return;
        }

        if (method.equals("DELETE")) {
            try {
                favoriteService.removeFavorite(authUser.getId(), mediaId);
                HttpUtil.sendEmpty(exchange, 204);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, e.getMessage());
            }
            return;
        }

        HttpUtil.sendError(exchange, 405, "Method Not Allowed");
    }

    // /api/media/{id}/favorite -> {id}
    private int extractMediaId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 2]);
    }
}
