package com.baitan.balancer.handlers;

import java.io.IOException;

import com.baitan.balancer.ConcurrentLoadBalancer;
import com.baitan.balancer.Service;
import com.baitan.balancer.strategy.BalancingStrategy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ProxyHandler implements HttpHandler {

    private static volatile ProxyHandler instance;

    private ProxyHandler() {
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
        Service currentBackend = strategy.getNextServer();

        if (currentBackend == null) {
            try (exchange) {
                String response = "No healthy backend servers available";
                exchange.sendResponseHeaders(503, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            return;
        }

        currentBackend.routeRequest(exchange);

    }

}
