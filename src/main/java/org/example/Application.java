package org.example;

import org.example.server.MrpServer;

public class Application {

    public static void main(String[] args) {
        // Server erstellen
        MrpServer server = new MrpServer();

        try {
            // Server starten
            server.start();

        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }
}
