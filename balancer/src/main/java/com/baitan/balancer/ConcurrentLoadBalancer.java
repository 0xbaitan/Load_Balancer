package com.baitan.balancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baitan.balancer.handlers.ProxyHandler;
import com.baitan.balancer.health.HealthCheckThread;
import com.baitan.balancer.health.HealthChecker;
import com.baitan.balancer.strategy.BalancingStrategy;
import com.baitan.balancer.strategy.ConcurrentRoundRobinStrategy;
import com.sun.net.httpserver.HttpServer;

public class ConcurrentLoadBalancer {

    private static volatile ConcurrentLoadBalancer instance;
    private BalancingStrategy balancingStrategy;
    private final HealthChecker healthChecker;
    private final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_PORT = 8080;

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

    private void initializeServers() {
        Service[] healthyServers = healthChecker.getHealthyServers();
        for (Service server : healthyServers) {
            balancingStrategy.addServer(server);
        }
    }

    private void initializeLoadBalancerServer() {
        try {
            HttpServer loadBalancerServer = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
            loadBalancerServer.createContext("/", ProxyHandler.getInstance());
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            loadBalancerServer.setExecutor(executor);
            loadBalancerServer.start();
            System.out.println("Load Balancer started on port" + DEFAULT_PORT);
        } catch (IOException e) {
            System.err.println("Failed to create HTTP server: " + e.getMessage());
        }
    }

    public void start() {
        initializeServers();

        runHealthCheck();

        initializeLoadBalancerServer();

    }
}
