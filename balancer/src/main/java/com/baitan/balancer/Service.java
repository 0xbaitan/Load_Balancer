package com.baitan.balancer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import io.netty.handler.codec.http.HttpResponseStatus;

public class Service {

    private final String host;
    private final int port;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final int DEFAULT_TIMEOUT = 2000; // Default timeout in milliseconds

    public Service(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static boolean isInvalid(Service server) {
        return server == null || server.getHost() == null || server.getHost().isEmpty() || server.getPort() <= 0;
    }

    public boolean isHealthy() {

        if (isInvalid(this)) {
            return false;
        }

        try {
            var request = HttpRequest.newBuilder().uri(java.net.URI.create("http://" + host + ":" + port + "/health"))
                    .timeout(java.time.Duration.ofSeconds(DEFAULT_TIMEOUT)).GET().build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == HttpResponseStatus.OK.code();
        } catch (InterruptedException | IllegalArgumentException | SecurityException | IOException e) {
            return false;
        }
    }

    public void routeRequest(HttpExchange exchange) throws IOException {
        if (isInvalid(this)) {
            throw new IllegalArgumentException("Invalid server: " + this);
        }

        URI uri = URI.create("http://" + this.getHost() + ":" + this.getPort() + exchange.getRequestURI());

        List<String> restrictedHeaders = List.of("host", "content-length", "transfer-encoding", "connection");

        HttpRequest request = HttpRequest.newBuilder().uri(uri).method(exchange.getRequestMethod(),
                HttpRequest.BodyPublishers.ofInputStream(() -> exchange.getRequestBody())).build();

        try (exchange) {
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
            String errorResponse = "Error processing request: " + e.getMessage();
            exchange.sendResponseHeaders(500, errorResponse.length());
            exchange.getResponseBody().write(errorResponse.getBytes());
        }
    }

    @Override
    public String toString() {
        return "Server{" +

                "host='" + host + '\'' + ", port=" + port + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Service))
            return false;
        Service server = (Service) o;
        return port == server.port && host.equals(server.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

}
