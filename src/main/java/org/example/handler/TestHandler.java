package org.example.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class TestHandler implements HttpHandler {

    // Diese Methode wird aufgerufen wenn jemand den Server anfragt
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // 1. Unsere Antwort als Text
        String response = "Hallo! Der Server funktioniert!";

        // 2. Dem Browser sagen: "Antwort kommt, sie hat X Zeichen"
        exchange.sendResponseHeaders(200, response.length());

        // 3. Antwort senden
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
