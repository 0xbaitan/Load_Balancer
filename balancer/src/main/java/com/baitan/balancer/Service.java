package com.baitan.balancer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.apache.http.HttpStatus;

import com.sun.net.httpserver.HttpExchange;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Represents a backend service with host and port information. Provides methods
 * to check health status and route HTTP requests.
 *
 * <p>
 * This class uses Java's {@link java.net.http.HttpClient} to perform health
 * checks and to forward HTTP requests to the backend service.
 * </p>
 *
 * <ul>
 * <li>{@code host}: The hostname or IP address of the service.</li>
 * <li>{@code port}: The port number on which the service is running.</li>
 * </ul>
 *
 * <h2>Key Methods</h2>
 * <ul>
 * <li>{@link #isHealthy()}: Checks if the service is healthy by sending a GET
 * request to the /health endpoint.</li>
 * <li>{@link #routeRequest(com.sun.net.httpserver.HttpExchange)}: Routes an
 * incoming HTTP request to this service and returns the response.</li>
 * <li>{@link #isInvalid(Service)}: Static utility to validate a Service
 * instance.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * The class is immutable and thread-safe.
 * </p>
 *
 * @author Tanish Baidya
 */
public class Service {

    private final String host;
    private final int port;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final int DEFAULT_TIMEOUT = 2000; // Default timeout in milliseconds

    public Service(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the host of the service.
     * 
     * @return the host as a String
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port number associated with this service.
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Checks if the provided service is invalid. A service is considered invalid if
     * it is null, has an empty host, or has a non-positive port number.
     * 
     * @param service
     * @return true if the service is invalid, false otherwise
     */
    public static boolean isInvalid(Service service) {
        return service == null || service.getHost() == null || service.getHost().isEmpty() || service.getPort() <= 0;
    }

    /**
     * Checks if the service is healthy by sending a GET request to the /health
     * endpoint. Returns true if the response status code is 200 OK, false
     * otherwise.
     * 
     * @return true if the service is healthy, false otherwise
     * @throws IllegalArgumentException if the service is invalid
     * 
     */
    public boolean isHealthy() {

        if (isInvalid(this)) {
            throw new IllegalArgumentException("Invalid server: " + this);
        }

        try {
            var request = HttpRequest.newBuilder().uri(java.net.URI.create("http://" + host + ":" + port + "/health"))
                    .timeout(java.time.Duration.ofSeconds(DEFAULT_TIMEOUT)).GET().build();

            var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == HttpResponseStatus.OK.code();
        } catch (InterruptedException | IllegalArgumentException | SecurityException | IOException e) {
            return false;
        }
    }

    /**
     * Routes an incoming HTTP request to this service. It constructs a new URI
     * based on the service's host and port, and forwards the request to the backend
     * service.
     *
     * @param exchange the HttpExchange object containing the request and response
     *                 information
     * @throws IllegalArgumentException if the service is invalid
     */
    public void routeRequest(HttpExchange exchange) throws IOException {
        if (isInvalid(this)) {
            throw new IllegalArgumentException("Invalid server: " + this);
        }

        URI uri = URI.create("http://" + this.getHost() + ":" + this.getPort() + exchange.getRequestURI());

        HttpRequest request = HttpRequest.newBuilder().uri(uri).method(exchange.getRequestMethod(),
                HttpRequest.BodyPublishers.ofInputStream(() -> exchange.getRequestBody())).build();

        try (exchange) {

            var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            byte[] responseBytes = response.body().readAllBytes();
            exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);
            exchange.getResponseBody().write(responseBytes);

        } catch (IOException | InterruptedException e) {

            String errorResponse = "Error processing request: " + e.getMessage();
            exchange.sendResponseHeaders(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorResponse.length());
            exchange.getResponseBody().write(errorResponse.getBytes());

        }
    }

    /**
     * Returns a string representation of the Service object.
     *
     * @return a string in the format "Service{host='host', port=port}"
     */
    @Override
    public String toString() {
        return "Service{" + "host='" + host + '\'' + ", port=" + port + '}';
    }

    /**
     * Compares this Service object with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Service))
            return false;
        Service service = (Service) o;
        return port == service.port && host.equals(service.host);
    }

    /**
     * Returns a hash code value for this Service object.
     *
     * @return an integer hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

}
