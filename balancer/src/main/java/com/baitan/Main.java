package com.baitan;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.baitan.server.HealthChecker;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HealthChecker healthChecker = new HealthChecker();
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "Hello from Java HTTP server!";
                String[] healthyContainers = healthChecker.getHealthyContainers();
                if (healthyContainers.length > 0) {
                    response += "\nHealthy containers: " + String.join(", ", healthyContainers);
                } else {
                    response += "\nNo healthy containers found.";
                }
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.setExecutor(null); // creates a default executor
        System.out.println("Server started at http://localhost:8080/");
        server.start();
    }
}