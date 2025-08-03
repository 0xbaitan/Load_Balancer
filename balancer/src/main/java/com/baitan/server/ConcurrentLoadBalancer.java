package com.baitan.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.baitan.balancing.BalancingStrategy;
import com.baitan.balancing.ConcurrentRoundRobinStrategy;
import com.baitan.server.handlers.ProxyHandler;
import com.sun.net.httpserver.HttpServer;

public class ConcurrentLoadBalancer {

    private static volatile ConcurrentLoadBalancer instance;
    private BalancingStrategy balancingStrategy;
    private final HealthChecker healthChecker;

    private ConcurrentLoadBalancer() {
        this.balancingStrategy = ConcurrentRoundRobinStrategy.getInstance();
        this.healthChecker = HealthChecker.getInstance();

    }

    public static ConcurrentLoadBalancer getInstance() {

        ConcurrentLoadBalancer localInstance = instance;
        if (localInstance == null) {
            synchronized (ConcurrentLoadBalancer.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = new ConcurrentLoadBalancer();
                }
            }
        }
        return instance;
    }

    public BalancingStrategy getBalancingStrategy() {
        return balancingStrategy;
    }

    public void setBalancingStrategy(BalancingStrategy strategy) {
        this.balancingStrategy = strategy;
    }

    private void runHealthCheck() {
        try {
            HealthCheckThread healthCheckThread = new HealthCheckThread(healthChecker, balancingStrategy);
            healthCheckThread.start();
        } catch (Exception e) {
            System.err.println("Failed to start health check thread: " + e.getMessage());
        }

    }

    public void start() {
        Server[] healthyServers = healthChecker.getRunningContainers();
        for (Server server : healthyServers) {
            balancingStrategy.addServer(server);
        }

        HttpServer loadBalancerServer = null;
        try {

            loadBalancerServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loadBalancerServer == null) {
            throw new RuntimeException("Failed to create HTTP server");
        }

        loadBalancerServer.createContext("/", ProxyHandler.getInstance());

        loadBalancerServer.setExecutor(Executors.newFixedThreadPool(10)); // sets a custom executor

        runHealthCheck();

        loadBalancerServer.start();
    }
}
