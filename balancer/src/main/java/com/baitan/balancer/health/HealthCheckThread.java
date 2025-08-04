package com.baitan.balancer.health;

import com.baitan.balancer.Service;
import com.baitan.balancer.strategy.BalancingStrategy;

/**
 * A thread that periodically performs health checks on services and updates the
 * balancing strategy with the list of healthy servers. The health check
 * interval is set to 60 seconds.
 *
 * <p>
 * This thread continuously runs, invoking the
 * {@link HealthChecker#getHealthyServices()} method to retrieve the current
 * healthy services, and then synchronizes the {@link BalancingStrategy} with
 * the updated list. If interrupted, the thread will log the interruption and
 * terminate gracefully.
 * </p>
 *
 * @see HealthChecker
 * @see BalancingStrategy
 * 
 * @author Tanish Baidya
 */
public class HealthCheckThread extends Thread {

    private final HealthChecker healthChecker;
    private final BalancingStrategy balancingStrategy;
    private static final int HEALTH_CHECK_INTERVAL = 60 * 1000; // 60 seconds

    /**
     * Constructor for HealthCheckThread.
     * <p>
     * Initializes the thread with a HealthChecker instance and a BalancingStrategy
     * instance.
     * </p>
     * 
     * @param healthChecker   the HealthChecker instance to use for health checks
     * @param balacingStategy the BalancingStrategy instance to synchronize with
     *                        healthy services
     */
    public HealthCheckThread(HealthChecker healthChecker, BalancingStrategy balacingStategy) {
        this.healthChecker = healthChecker;
        this.balancingStrategy = balacingStategy;
    }

    /**
     * Starts the health check thread.
     * <p>
     * This method overrides the {@link Thread#run()} method to perform health
     * checks at regular intervals. It retrieves the list of healthy services and
     * updates the list of services in the balancing strategy accordingly.
     * </p>
     */
    @Override
    public void run() {
        while (true) {
            try {
                Service[] services = healthChecker.getHealthyServices();
                System.out.println("Healthy services: " + services.length);
                balancingStrategy.synchronizeWithHealthyServices(services);
                Thread.sleep(HEALTH_CHECK_INTERVAL); // Sleep for 60 seconds before the next health check
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Health check thread interrupted: " + e.getMessage());
            }
        }
    }

}
