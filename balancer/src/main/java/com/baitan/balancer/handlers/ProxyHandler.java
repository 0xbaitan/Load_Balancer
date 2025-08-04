package com.baitan.balancer.handlers;

import java.io.IOException;

import org.apache.http.HttpStatus;

import com.baitan.balancer.ConcurrentLoadBalancer;
import com.baitan.balancer.Service;
import com.baitan.balancer.strategy.BalancingStrategy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * ProxyHandler is responsible for handling incoming HTTP requests and routing
 * them to the appropriate backend service based on the current balancing
 * strategy. It uses a singleton pattern to ensure only one instance exists.
 * 
 * @author Tanish Baidya
 */
public class ProxyHandler implements HttpHandler {

    private static volatile ProxyHandler instance;

    private ProxyHandler() {
    }

    /**
     * Returns the singleton instance of ProxyHandler.
     * <p>
     * Uses double-checked locking to ensure thread safety and lazy initialization.
     * </p>
     *
     * @return the singleton instance of ProxyHandler
     */
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

    /**
     * Handles incoming HTTP requests by routing them to the next available backend
     * service using the current balancing strategy.
     * <p>
     * If no healthy backend servers are available, it responds with a 503 Service
     * Unavailable status.
     * </p>
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Using thread:" + Thread.currentThread().getName());
        BalancingStrategy strategy = ConcurrentLoadBalancer.getInstance().getBalancingStrategy();
        Service currentBackend = strategy.getNextService();

        if (currentBackend == null) {
            try (exchange) {
                String response = "No healthy backend servers available";
                exchange.sendResponseHeaders(HttpStatus.SC_SERVICE_UNAVAILABLE, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            return;
        }

        currentBackend.routeRequest(exchange);

    }

}
