package com.baitan.server;

import com.baitan.balancing.BalancingStrategy;

public class HealthCheckThread extends Thread {

    private final HealthChecker healthChecker;
    private final BalancingStrategy balancingStrategy;

    public HealthCheckThread(HealthChecker healthChecker, BalancingStrategy balacingStategy) {
        this.healthChecker = healthChecker;
        this.balancingStrategy = balacingStategy;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Server[] healthyServers = healthChecker.getHealthyServers();
                System.out.println("Healthy servers: " + healthyServers.length);
                balancingStrategy.manageListOfServers(healthyServers);
                Thread.sleep(60 * 1000); // Sleep for 60 seconds before the next health check
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Health check thread interrupted: " + e.getMessage());
            }
        }
    }

}
