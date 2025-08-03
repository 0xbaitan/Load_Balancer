package com.baitan.server.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;

import com.baitan.balancing.BalancingStrategy;
import com.baitan.server.ConcurrentLoadBalancer;
import com.baitan.server.Server;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ProxyHandler implements HttpHandler {

    private static volatile ProxyHandler instance;
    private final HttpClient httpClient;

    private ProxyHandler() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public static ProxyHandler getInstance() {
        ProxyHandler localInstance = ProxyHandler.instance;
        if (localInstance == null) {
            synchronized (ProxyHandler.class) {
                localInstance = ProxyHandler.instance;
                if (localInstance == null) {
                    ProxyHandler.instance = localInstance = new ProxyHandler();
                }
            }
        }
        return localInstance;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Using thread:" + Thread.currentThread().getName());
        BalancingStrategy strategy = ConcurrentLoadBalancer.getInstance().getBalancingStrategy();
        Server currentBackend = strategy.getNextServer();

        if (currentBackend == null) {
            String response = "No healthy backend servers available";
            exchange.sendResponseHeaders(503, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }

        URI uri = URI.create("http://" + currentBackend.getHost() + ":" + 3000 + exchange.getRequestURI());

        List<String> restrictedHeaders = List.of("host", "content-length", "transfer-encoding", "connection");

        HttpRequest request = HttpRequest.newBuilder().uri(uri).method(exchange.getRequestMethod(),
                HttpRequest.BodyPublishers.ofInputStream(() -> exchange.getRequestBody())).build();

        try {
            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());

            Headers headers = exchange.getResponseHeaders();
            response.headers().map().forEach((key, values) -> {
                if (!restrictedHeaders.contains(key)) {
                    for (String value : values) {
                        headers.add(key, value);
                    }
                }
            });

            byte[] responseBytes = response.body().readAllBytes();
            exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            String errorResponse = "Error processing request: " + e.getMessage();
            exchange.sendResponseHeaders(500, errorResponse.length());
            exchange.getResponseBody().write(errorResponse.getBytes());
        } finally {
            exchange.close();
        }
    }

}
