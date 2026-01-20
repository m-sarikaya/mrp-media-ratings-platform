package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.User;
import org.example.domain.UserProfileResponse;
import org.example.domain.UserProfileUpdate;
import org.example.http.HttpUtil;
import org.example.service.FavoriteService;
import org.example.service.RatingService;
import org.example.service.UserService;
import org.example.service.RecommendationService;

import java.io.IOException;

// Handler für User-bezogene Endpunkte (Profil, Ratings, Favorites, Recommendations).
public class UserHandler implements HttpHandler {
    // Services für Business-Logik.
    private final UserService userService;
    private final RatingService ratingService;
    private final FavoriteService favoriteService;
    private final RecommendationService recommendationService;

    public UserHandler(UserService userService, RatingService ratingService, FavoriteService favoriteService,
                       RecommendationService recommendationService) {
        this.userService = userService;
        this.ratingService = ratingService;
        this.favoriteService = favoriteService;
        this.recommendationService = recommendationService;
    }

    // Routing basierend auf dem Pfadende (z. B. /profile).
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        int userId;
        try {
            userId = extractUserId(path);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, "Ungültige User-ID");
            return;
        }

        User authUser = (User) exchange.getAttribute("authUser");
        if (authUser == null) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }
        if (authUser.getId() != userId) {
            HttpUtil.sendError(exchange, 403, "Kein Zugriff auf fremdes Profil");
            return;
        }

        if (path.endsWith("/profile")) {
            handleProfile(exchange, method, userId);
            return;
        }

        if (path.endsWith("/ratings")) {
            handleRatings(exchange, method, userId);
            return;
        }

        if (path.endsWith("/favorites")) {
            handleFavorites(exchange, method, userId);
            return;
        }

        if (path.endsWith("/recommendations")) {
            handleRecommendations(exchange, method, userId);
            return;
        }

        HttpUtil.sendError(exchange, 404, "Not Found");
    }

    // GET/PUT /profile (Logik folgt spaeter im Service).
    private void handleProfile(HttpExchange exchange, String method, int userId) throws IOException {
        if (method.equals("GET")) {
            try {
                UserProfileResponse profile = userService.getProfile(userId);
                HttpUtil.sendJson(exchange, 200, profile);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 404, e.getMessage());
            }
            return;
        }
        if (method.equals("PUT")) {
            UserProfileUpdate update = HttpUtil.readJson(exchange, UserProfileUpdate.class);
            try {
                userService.updateProfile(userId, update);
                UserProfileResponse profile = userService.getProfile(userId);
                HttpUtil.sendJson(exchange, 200, profile);
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, e.getMessage());
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method Not Allowed");
    }

    // GET /ratings (Logik folgt spaeter im Service).
    private void handleRatings(HttpExchange exchange, String method, int userId) throws IOException {
        if (method.equals("GET")) {
            try {
                HttpUtil.sendJson(exchange, 200, ratingService.getRatingsByUser(userId));
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, e.getMessage());
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method Not Allowed");
    }

    // GET /favorites (Logik folgt spaeter im Service).
    private void handleFavorites(HttpExchange exchange, String method, int userId) throws IOException {
        if (method.equals("GET")) {
            try {
                HttpUtil.sendJson(exchange, 200, favoriteService.getFavoritesByUser(userId));
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, e.getMessage());
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method Not Allowed");
    }

    // GET /recommendations (Logik folgt spaeter im Service).
    private void handleRecommendations(HttpExchange exchange, String method, int userId) throws IOException {
        if (method.equals("GET")) {
            String query = exchange.getRequestURI().getQuery();
            var params = HttpUtil.parseQuery(query);
            String type = params.get("type");

            try {
                if ("content".equalsIgnoreCase(type)) {
                    HttpUtil.sendJson(exchange, 200, recommendationService.recommendByContent(userId));
                } else {
                    HttpUtil.sendJson(exchange, 200, recommendationService.recommendByGenre(userId));
                }
            } catch (Exception e) {
                HttpUtil.sendError(exchange, 400, e.getMessage());
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method Not Allowed");
    }

    // /api/users/{id}/... -> {id}
    private int extractUserId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 2]);
    }
}
