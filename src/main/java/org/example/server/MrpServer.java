package org.example.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

import org.example.handler.AuthHandler;
import org.example.handler.FavoritesHandler;
import org.example.handler.LeaderboardHandler;
import org.example.handler.MediaHandler;
import org.example.handler.MediaRouterHandler;
import org.example.handler.RatingHandler;
import org.example.handler.TestHandler;
import org.example.handler.UserHandler;
import org.example.http.AuthFilter;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.FavoriteRepository;
import org.example.persistence.MediaRepository;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;
import org.example.service.FavoriteService;
import org.example.service.MediaService;
import org.example.service.RecommendationService;
import org.example.service.RatingService;
import org.example.service.UserService;

public class MrpServer {

    private HttpServer server;
    private static final int PORT = 8080;

    // Server starten
    public void start() throws Exception {
        // Server erstellen auf Port 8080
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Datenbankverbindung holen
        var connection = DatabaseConnection.getConnection();

        // Repositories erstellen
        UserRepository userRepository = new UserRepository(connection);
        MediaRepository mediaRepository = new MediaRepository(connection);

        // Services erstellen
        RatingRepository ratingRepository = new RatingRepository(connection);
        UserService userService = new UserService(userRepository, ratingRepository);
        MediaService mediaService = new MediaService(mediaRepository);
        RatingService ratingService = new RatingService(ratingRepository);
        FavoriteRepository favoriteRepository = new FavoriteRepository(connection);
        FavoriteService favoriteService = new FavoriteService(favoriteRepository);
        RecommendationService recommendationService = new RecommendationService(mediaRepository, ratingRepository);

        // Handler erstellen
        AuthHandler authHandler = new AuthHandler(userService);
        UserHandler userHandler = new UserHandler(userService, ratingService, favoriteService, recommendationService);
        MediaHandler mediaHandler = new MediaHandler(mediaService);
        FavoritesHandler favoritesHandler = new FavoritesHandler(favoriteService);
        RatingHandler ratingHandler = new RatingHandler(ratingService);
        LeaderboardHandler leaderboardHandler = new LeaderboardHandler(ratingService);
        MediaRouterHandler mediaRouter = new MediaRouterHandler(mediaHandler, favoritesHandler, ratingHandler);

        // Filter für Token-Authentifizierung
        AuthFilter authFilter = new AuthFilter(userService);

        // Handler registrieren (OpenAPI-konform)
        server.createContext("/test", new TestHandler());

        server.createContext("/api/users/register", authHandler).getFilters().add(authFilter);
        server.createContext("/api/users/login", authHandler).getFilters().add(authFilter);
        server.createContext("/api/users", userHandler).getFilters().add(authFilter);
        server.createContext("/api/media", mediaRouter).getFilters().add(authFilter);
        server.createContext("/api/ratings", ratingHandler).getFilters().add(authFilter);
        server.createContext("/api/leaderboard", leaderboardHandler).getFilters().add(authFilter);

        // Server starten
        server.start();

        System.out.println("Server läuft auf http://localhost:" + PORT);
        System.out.println("Endpoints:");
        System.out.println("  POST   /api/users/register");
        System.out.println("  POST   /api/users/login");
        System.out.println("  GET    /api/users/{id}/profile");
        System.out.println("  GET    /api/users/{id}/ratings");
        System.out.println("  GET    /api/users/{id}/favorites");
        System.out.println("  GET    /api/users/{id}/recommendations");
        System.out.println("  GET    /api/media");
        System.out.println("  POST   /api/media");
        System.out.println("  GET    /api/media/{id}");
        System.out.println("  PUT    /api/media/{id}");
        System.out.println("  DELETE /api/media/{id}");
        System.out.println("  POST   /api/media/{id}/rate");
        System.out.println("  POST   /api/media/{id}/favorite");
        System.out.println("  DELETE /api/media/{id}/favorite");
        System.out.println("  PUT    /api/ratings/{id}");
        System.out.println("  DELETE /api/ratings/{id}");
        System.out.println("  POST   /api/ratings/{id}/like");
        System.out.println("  POST   /api/ratings/{id}/confirm");
        System.out.println("  GET    /api/leaderboard");
    }

    // Server stoppen
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server gestoppt");
        }
    }

    // Server zurückgeben (brauchen wir später)
    public HttpServer getServer() {
        return server;
    }
}
