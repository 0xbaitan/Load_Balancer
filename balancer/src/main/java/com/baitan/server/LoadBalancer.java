package com.baitan.server;

import java.net.InetSocketAddress;

import com.baitan.balancing.BalancingStrategy;
import com.baitan.balancing.ConcurrentRoundRobinStrategy;
import com.baitan.server.handlers.ProxyHandler;
import com.sun.net.httpserver.HttpServer;

public class LoadBalancer {

    private static LoadBalancer instance;
    private BalancingStrategy balancingStrategy;
    private HealthChecker healthChecker;
    private HttpServer loadBalancerServer;

    private LoadBalancer() {
        this.balancingStrategy = ConcurrentRoundRobinStrategy.getInstance();
        this.healthChecker = HealthChecker.getInstance();
        Server[] healthyServers = healthChecker.getHealthyServers();
        for (Server server : healthyServers) {
            balancingStrategy.addServer(server);
        }
        try {
            this.loadBalancerServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.loadBalancerServer.createContext("/", ProxyHandler.getInstance());

        this.loadBalancerServer.createContext("/containers", exchange -> {
            String response = "Healthy containers: " + String.join(", ", healthChecker.getHealthyContainers());
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        this.loadBalancerServer.setExecutor(null); // creates a default executor
    }

    public static synchronized LoadBalancer getInstance() {
        if (instance == null) {
            instance = new LoadBalancer();
        }
        return instance;
    }

    public BalancingStrategy getBalancingStrategy() {
        return balancingStrategy;
    }

    public void setBalancingStrategy(BalancingStrategy strategy) {
        this.balancingStrategy = strategy;
    }

    public void start() {
        if (loadBalancerServer != null) {
            loadBalancerServer.start();
            System.out.println("Load Balancer started on port 8080");
        } else {
            System.out.println("Failed to start Load Balancer: Server is null");
        }
    }

}
