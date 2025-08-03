package com.baitan.server;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class Server {

    private final String host;
    private final int port;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static boolean isInvalid(Server server) {
        return server == null || server.getHost() == null || server.getHost().isEmpty() || server.getPort() <= 0;
    }

    public boolean isHealthy() {
        var request = HttpRequest.newBuilder().uri(java.net.URI.create("http://" + host + ":" + port + "/health"))
                .timeout(java.time.Duration.ofSeconds(2)).GET().build();

        try {
            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("Health check for " + this + " returned status: " + response.statusCode());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Server{" +

                "host='" + host + '\'' + ", port=" + port + '}';
    }

}
