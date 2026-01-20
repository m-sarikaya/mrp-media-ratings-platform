package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.RatingInput;
import org.example.domain.User;
import org.example.http.HttpUtil;
import org.example.service.RatingService;

import java.io.IOException;

// Handler fuer Rating-Endpunkte (rate, update, delete, like, confirm).
public class RatingHandler implements HttpHandler {

    // Service fuer Rating-Business-Logik.
    private final RatingService ratingService;

    public RatingHandler(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        User authUser = (User) exchange.getAttribute("authUser");
        if (authUser == null) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }

        if (path.matches("^/api/media/\\d+/rate$")) {
            handleRateMedia(exchange, method, path);
            return;
        }

        if (path.matches("^/api/ratings/\\d+$")) {
            handleRatingUpdateDelete(exchange, method, path);
            return;
        }

        if (path.matches("^/api/ratings/\\d+/like$")) {
            handleLike(exchange, method, path);
            return;
        }

        if (path.matches("^/api/ratings/\\d+/confirm$")) {
            handleConfirm(exchange, method, path);
            return;
        }

        HttpUtil.sendError(exchange, 404, "Not Found");
    }

    // POST /api/media/{id}/rate
    private void handleRateMedia(HttpExchange exchange, String method, String path) throws IOException {
        if (!method.equals("POST")) {
            HttpUtil.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        int mediaId = extractId(path, 2);
        RatingInput input = HttpUtil.readJson(exchange, RatingInput.class);
        User authUser = (User) exchange.getAttribute("authUser");

        try {
            var rating = ratingService.createRating(mediaId, authUser.getId(), input.getStars(), input.getComment());
            HttpUtil.sendJson(exchange, 201, rating);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // PUT oder DELETE /api/ratings/{id}
    private void handleRatingUpdateDelete(HttpExchange exchange, String method, String path) throws IOException {
        if (method.equals("PUT")) {
            int ratingId;
            try {
                ratingId = extractId(path, 1);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, "Ungueltige Rating-ID");
                return;
            }
            RatingInput input = HttpUtil.readJson(exchange, RatingInput.class);
            User authUser = (User) exchange.getAttribute("authUser");
            try {
                var rating = ratingService.updateRating(ratingId, authUser.getId(), input.getStars(), input.getComment());
                HttpUtil.sendJson(exchange, 200, rating);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 403, e.getMessage());
            }
            return;
        }
        if (method.equals("DELETE")) {
            int ratingId;
            try {
                ratingId = extractId(path, 1);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, "Ungueltige Rating-ID");
                return;
            }
            User authUser = (User) exchange.getAttribute("authUser");
            try {
                ratingService.deleteRating(ratingId, authUser.getId());
                HttpUtil.sendEmpty(exchange, 204);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 403, e.getMessage());
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method Not Allowed");
    }

    // POST /api/ratings/{id}/like
    private void handleLike(HttpExchange exchange, String method, String path) throws IOException {
        if (!method.equals("POST")) {
            HttpUtil.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        int ratingId = extractId(path, 2);
        User authUser = (User) exchange.getAttribute("authUser");
        try {
            ratingService.likeRating(ratingId, authUser.getId());
            HttpUtil.sendEmpty(exchange, 200);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // POST /api/ratings/{id}/confirm
    private void handleConfirm(HttpExchange exchange, String method, String path) throws IOException {
        if (!method.equals("POST")) {
            HttpUtil.sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        int ratingId = extractId(path, 2);
        User authUser = (User) exchange.getAttribute("authUser");
        try {
            ratingService.confirmComment(ratingId, authUser.getId());
            HttpUtil.sendEmpty(exchange, 200);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 403, e.getMessage());
        }
    }

    // Hilfsmethode: ID aus Pfad holen, z. B. /api/ratings/5 -> 5
    private int extractId(String path, int indexFromEnd) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - indexFromEnd]);
    }
}
