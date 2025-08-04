package com.baitan.balancer.health;

import com.baitan.balancer.Service;
import com.baitan.balancer.strategy.BalancingStrategy;

public class HealthCheckThread extends Thread {

    private final HealthChecker healthChecker;
    private final BalancingStrategy balancingStrategy;
    private static final int HEALTH_CHECK_INTERVAL = 60 * 1000; // 60 seconds

    public HealthCheckThread(HealthChecker healthChecker, BalancingStrategy balacingStategy) {
        this.healthChecker = healthChecker;
        this.balancingStrategy = balacingStategy;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Service[] healthyServers = healthChecker.getHealthyServers();
                System.out.println("Healthy servers: " + healthyServers.length);
                balancingStrategy.synchronizeWithHealthyServers(healthyServers);
                Thread.sleep(HEALTH_CHECK_INTERVAL); // Sleep for 60 seconds before the next health check
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Health check thread interrupted: " + e.getMessage());
            }
        }
    }

}
