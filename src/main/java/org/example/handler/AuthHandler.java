package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.User;
import org.example.http.HttpUtil;
import org.example.service.UserService;

import java.io.IOException;
import java.util.Map;

// Verantwortlich fuer Registrierung und Login (nur Auth-Endpunkte).
public class AuthHandler implements HttpHandler {
    // Zugriff auf User-Business-Logik.
    private final UserService userService;

    // Service per Konstruktor, damit Handler klein bleibt.
    public AuthHandler(UserService userService) {
        this.userService = userService;
    }

    // Routing fuer /api/users/register und /api/users/login.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (!method.equals("POST")) {
            HttpUtil.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        if (path.equals("/api/users/register")) {
            handleRegister(exchange);
            return;
        }

        if (path.equals("/api/users/login")) {
            handleLogin(exchange);
            return;
        }

        HttpUtil.sendError(exchange, 404, "Not Found");
    }

    // Registrierung eines neuen Users.
    private void handleRegister(HttpExchange exchange) throws IOException {
        User user = HttpUtil.readJson(exchange, User.class);

        try {
            User saved = userService.register(user);
            HttpUtil.sendJson(exchange, 201, saved);
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // Login und Rueckgabe eines Tokens.
    private void handleLogin(HttpExchange exchange) throws IOException {
        User loginData = HttpUtil.readJson(exchange, User.class);

        try {
            String token = userService.login(loginData.getUsername(), loginData.getPassword());
            HttpUtil.sendJson(exchange, 200, Map.of("token", token));
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 401, e.getMessage());
        }
    }
}
