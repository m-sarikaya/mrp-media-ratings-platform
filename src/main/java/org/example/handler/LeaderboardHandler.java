package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.http.HttpUtil;
import org.example.service.RatingService;

import java.io.IOException;

// Handler fuer /api/leaderboard.
public class LeaderboardHandler implements HttpHandler {
    // Service fuer Leaderboard-Daten.
    private final RatingService ratingService;

    public LeaderboardHandler(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (!method.equals("GET")) {
            HttpUtil.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            HttpUtil.sendJson(exchange, 200, ratingService.getLeaderboard());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        }
    }
}
