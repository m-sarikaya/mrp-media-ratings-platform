package org.example.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

import org.example.handler.MediaHandler;
import org.example.handler.TestHandler;
import org.example.handler.UserHandler;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.MediaRepository;
import org.example.persistence.UserRepository;
import org.example.service.MediaService;
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
        UserService userService = new UserService(userRepository);
        MediaService mediaService = new MediaService(mediaRepository);

        // Handler registrieren
        server.createContext("/test", new TestHandler());
        server.createContext("/api/users", new UserHandler(userService));
        server.createContext("/api/media", new MediaHandler(mediaService, userService));

        // Server starten
        server.start();

        System.out.println("Server läuft auf http://localhost:" + PORT);
        System.out.println("Endpoints:");
        System.out.println("  GET    /test           - Test ob Server läuft");
        System.out.println("  POST   /api/users      - User registrieren");
        System.out.println("  POST   /api/users/login - User einloggen");
        System.out.println("  GET    /api/media      - Alle Medien");
        System.out.println("  POST   /api/media      - Medium erstellen (Token nötig)");
        System.out.println("  PUT    /api/media/{id} - Medium bearbeiten (Token nötig)");
        System.out.println("  DELETE /api/media/{id} - Medium löschen (Token nötig)");
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
