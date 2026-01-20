package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.http.HttpUtil;

import java.io.IOException;

// Router fuer alle /api/media/... Pfade.
// Grund: HttpServer kann keine variablen Pfade direkt auf verschiedene Handler mappen.
public class MediaRouterHandler implements HttpHandler {
    private final MediaHandler mediaHandler;
    private final FavoritesHandler favoritesHandler;
    private final RatingHandler ratingHandler;

    public MediaRouterHandler(MediaHandler mediaHandler, FavoritesHandler favoritesHandler, RatingHandler ratingHandler) {
        this.mediaHandler = mediaHandler;
        this.favoritesHandler = favoritesHandler;
        this.ratingHandler = ratingHandler;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // /api/media -> Media CRUD
        if (path.equals("/api/media") || path.matches("^/api/media/\\d+$")) {
            mediaHandler.handle(exchange);
            return;
        }

        // /api/media/{id}/favorite -> Favorites
        if (path.matches("^/api/media/\\d+/favorite$")) {
            favoritesHandler.handle(exchange);
            return;
        }

        // /api/media/{id}/rate -> Rating
        if (path.matches("^/api/media/\\d+/rate$")) {
            ratingHandler.handle(exchange);
            return;
        }

        HttpUtil.sendError(exchange, 404, "Not Found");
    }
}
