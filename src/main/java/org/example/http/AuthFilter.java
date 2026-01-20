package org.example.http;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.example.domain.User;
import org.example.service.UserService;

import java.io.IOException;

// Globaler Filter: prueft den Bearer-Token fuer geschuetzte Endpunkte.
public class AuthFilter extends Filter {
    // Service fuer Token->User Aufloesung.
    private final UserService userService;

    // Service per Konstruktor injizieren.
    public AuthFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    // Kurze Beschreibung fuer Debugging.
    public String description() {
        return "Prueft den Authorization Bearer Token.";
    }

    @Override
    // Token pruefen und User ins Exchange-Objekt legen.
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Registration und Login brauchen keinen Token.
        if (isPublicEndpoint(path, method)) {
            chain.doFilter(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpUtil.sendError(exchange, 401, "Nicht eingeloggt");
            return;
        }

        String token = authHeader.substring(7);
        User user = userService.findByToken(token);
        if (user == null) {
            HttpUtil.sendError(exchange, 401, "Ungueltiger Token");
            return;
        }

        // User im Request speichern, damit Handler ihn nutzen koennen.
        exchange.setAttribute("authUser", user);
        chain.doFilter(exchange);
    }

    // Nur diese Endpoints sind ohne Token erlaubt.
    private boolean isPublicEndpoint(String path, String method) {
        if (!method.equals("POST")) {
            return false;
        }
        return path.equals("/api/users/register") || path.equals("/api/users/login");
    }
}
