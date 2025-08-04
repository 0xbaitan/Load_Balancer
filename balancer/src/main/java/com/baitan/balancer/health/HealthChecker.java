package com.baitan.balancer.health;

import java.io.IOException;
import java.util.Arrays;

import com.baitan.balancer.Service;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;

public class HealthChecker {

    private static volatile HealthChecker instance;
    private DockerClient dockerClient;

    private HealthChecker() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

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

    public Service[] getRunningContainers() {
        return dockerClient.listContainersCmd().withStatusFilter(Arrays.asList("running")).exec().stream()
                .filter(container -> !Arrays.asList(container.getNames()).contains(("/load_balancer")))
                .map(container -> new Service(container.getNames()[0].replace("/", ""), 3000)).toArray(Service[]::new);
    }

    public Service[] getExitedContainers() {
        return dockerClient.listContainersCmd().withStatusFilter(Arrays.asList("exited")).exec().stream()
                .filter(container -> !Arrays.asList(container.getNames()).contains(("/load_balancer")))
                .map(container -> new Service(container.getNames()[0].replace("/", ""), 3000)).toArray(Service[]::new);
    }

    public Service[] getHealthyServers() {
        var healthyContainers = getRunningContainers();
        return Arrays.stream(healthyContainers).map(server -> server.isHealthy() ? server : null)
                .filter(server -> server != null).toArray(Service[]::new);
    }

    public String[] getHealthyContainers() {
        return dockerClient.listContainersCmd().withStatusFilter(Arrays.asList("running")).exec().stream()
                .map(Container::getNames).flatMap(names -> java.util.Arrays.stream(names)).toArray(String[]::new);
    }

    public void close() throws IOException {
        dockerClient.close();
        instance = null;
    }
}
