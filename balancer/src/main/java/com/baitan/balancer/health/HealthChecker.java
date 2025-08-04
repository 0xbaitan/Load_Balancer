package com.baitan.balancer.health;

import java.io.IOException;
import java.util.Arrays;

import com.baitan.balancer.Service;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * Singleton class responsible for checking the health and status of Docker
 * containers.
 * <p>
 * Provides methods to retrieve running, exited, and healthy containers, as well
 * as to filter out the load balancer container from results. Uses a
 * DockerClient to interact with the Docker API.
 * </p>
 *
 * <ul>
 * <li>{@link #getInstance()} - Returns the singleton instance of
 * HealthChecker.</li>
 * <li>{@link #getRunningContainers()} - Retrieves all running containers except
 * the load balancer.</li>
 * <li>{@link #getExitedContainers()} - Retrieves all exited containers except
 * the load balancer.</li>
 * <li>{@link #getHealthyServices()} - Returns only healthy services from
 * running containers.</li>
 * <li>{@link #getHealthyContainers()} - Returns names of all healthy (running)
 * containers.</li>
 * <li>{@link #close()} - Closes the Docker client and resets the singleton
 * instance.</li>
 * </ul>
 *
 * Thread-safe implementation using double-checked locking for singleton
 * instantiation.
 * 
 * @author Tanish Baidya
 */
public class HealthChecker {

    private static volatile HealthChecker instance;
    private final DockerClient dockerClient;

    private HealthChecker() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    /**
     * Returns the singleton instance of HealthChecker.
     * <p>
     * Uses double-checked locking to ensure thread safety and lazy initialization.
     * </p>
     *
     * @return the singleton instance of HealthChecker
     */

    public static HealthChecker getInstance() {
        HealthChecker localInstance = instance;
        if (localInstance == null) {
            synchronized (HealthChecker.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new HealthChecker();
                }
            }
        }
        return localInstance;
    }

    /**
     * Retrieves all running Docker containers, excluding the load balancer
     * container.
     *
     * @return an array of Service objects representing running containers
     */

    public Service[] getRunningContainers() {

        // Get all running containers, excluding the load balancer container
        var containers = dockerClient.listContainersCmd().withStatusFilter(Arrays.asList("running")).exec();

        // Filter out the load balancer container
        var filteredContainers = containers.stream()
                .filter(container -> !Arrays.asList(container.getNames()).contains("/load_balancer"));

        // Map each container to a Service object
        var services = filteredContainers.map(container -> {
            String name = container.getNames()[0].replace("/", ""); // Remove leading slash
            int port = container.getPorts()[0].getPrivatePort(); // Using private port because it is the port exposed by
                                                                 // the container within the same network as the load
                                                                 // balancer
            return new Service(name, port);
        }).toArray(Service[]::new);

        return services;
    }

    /**
     * Retrieves all exited Docker containers, excluding the load balancer
     * container.
     *
     * @return an array of Service objects representing exited containers
     */
    public Service[] getExitedContainers() {
        // Get all containers with status "exited"
        var exitedContainers = dockerClient.listContainersCmd().withStatusFilter(Arrays.asList("exited")).exec();

        // Filter out the load balancer container
        var filteredContainers = exitedContainers.stream()
                .filter(container -> !Arrays.asList(container.getNames()).contains("/load_balancer"));

        // Map each container to a Service object
        var services = filteredContainers.map(container -> {
            String name = container.getNames()[0].replace("/", "");
            int port = container.getPorts()[0].getPrivatePort(); // Using private port because it is the port exposed by
                                                                 // the container within the same network as the load
                                                                 // balancer
            return new Service(name, port);
        }).toArray(Service[]::new);

        return services;
    }

    /**
     * Returns an array of healthy services from the running containers.
     * <p>
     * A service is considered healthy if it responds to a health check request.
     * </p>
     *
     * @return an array of healthy Service objects
     */
    public Service[] getHealthyServices() {
        var healthyContainers = getRunningContainers();

        // Mark unhealthy services as null then filter them out to only keep healthy
        // ones and return it as an array
        return Arrays.stream(healthyContainers).map(service -> service.isHealthy() ? service : null)
                .filter(service -> service != null).toArray(Service[]::new);
    }

    /**
     * Returns the names of all healthy (running) Docker containers.
     * <p>
     * Uses the getHealthyServices method to filter out unhealthy services.
     * </p>
     *
     * @return an array of strings representing the names of healthy containers
     */
    public String[] getHealthyContainers() {

        Service[] healthyServices = getHealthyServices();

        // Map each healthy service to its name
        String[] healthyContainerNames = Arrays.stream(healthyServices).map(Service::getHost).toArray(String[]::new);

        // Step 3: Return the array of healthy container names
        return healthyContainerNames;
    }

    /**
     * Closes the Docker client and resets the singleton instance.
     * <p>
     * This method should be called when the application is shutting down to release
     * resources.
     * </p>
     *
     * @throws IOException if an error occurs while closing the Docker client
     */
    public void close() throws IOException {
        dockerClient.close();
        instance = null;
    }
}
