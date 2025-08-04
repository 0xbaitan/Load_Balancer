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

/**
 * ConcurrentLoadBalancer is a singleton class that manages the load balancing
 * strategy and health checks for backend services. It initializes the load
 * balancer server and starts the health check thread to ensure that only
 * healthy services are used for routing requests.
 * 
 * <p>
 * The load balancer uses a concurrent round-robin strategy to distribute
 * requests among available backend services. It also performs periodic health
 * checks to update the list of healthy services.
 * </p>
 *
 * @see BalancingStrategy
 * @see HealthChecker
 * @see HealthCheckThread
 * 
 * @author Tanish Baidya
 */
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

    /**
     * Returns the singleton instance of ConcurrentLoadBalancer.
     * <p>
     * Uses double-checked locking to ensure thread safety and lazy initialization.
     * </p>
     *
     * @return the singleton instance of ConcurrentLoadBalancer
     */
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

    /**
     * Returns the current balancing strategy used by the load balancer.
     * 
     * @return the BalancingStrategy instance
     */
    public BalancingStrategy getBalancingStrategy() {
        return balancingStrategy;
    }

    /**
     * Sets the balancing strategy for the load balancer.
     * <p>
     * This method updates the current balancing strategy and loads it with healthy
     * services from the HealthChecker.
     * 
     * @param balancingStrategy the BalancingStrategy instance to set
     */
    public void setBalancingStrategy(BalancingStrategy balancingStrategy) {
        this.balancingStrategy = balancingStrategy;
        loadStrategyWithHealthyServices();
    }

    /**
     * Runs the health check thread to periodically check the health of backend
     * services and update the balancing strategy with the list of healthy services.
     * 
     * <p>
     * This method initializes a new HealthCheckThread and starts it. The thread
     * will run indefinitely, checking the health of services every 60 seconds. It
     * also synchronizes the balancing strategy with the healthy services retrieved
     * from the HealthChecker.
     * </p>
     * 
     */
    private void runHealthCheck() {
        try {
            HealthCheckThread healthCheckThread = new HealthCheckThread(healthChecker, balancingStrategy);
            healthCheckThread.start();
        } catch (Exception e) {
            System.err.println("Failed to start health check thread: " + e.getMessage());
        }

    }

    /**
     * Loads the balancing strategy with healthy services.
     * <p>
     * This method clears the current services in the balancing strategy and adds
     * only the healthy services retrieved from the HealthChecker. It ensures that
     * the balancing strategy is always up-to-date with the current state of the
     * backend services.
     * </p>
     * 
     */
    private void loadStrategyWithHealthyServices() {
        balancingStrategy.clearServices();
        Service[] healthyServers = healthChecker.getHealthyServices();
        for (Service server : healthyServers) {
            balancingStrategy.addServices(server);
        }
    }

    /**
     * Initializes the load balancer server and sets up the HTTP handler for
     * processing incoming requests.
     * 
     * <p>
     * This method creates an HTTP server that listens on a specified port and
     * routes incoming requests to the ProxyHandler, which uses the current
     * balancing strategy to forward requests to backend services.
     * </p>
     */
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

    /**
     * Starts the load balancer by loading the balancing strategy with healthy
     * services, running the health check thread, and initializing the load balancer
     * server.
     * 
     */
    public void start() {
        loadStrategyWithHealthyServices();

        runHealthCheck();

        initializeLoadBalancerServer();

    }
}
