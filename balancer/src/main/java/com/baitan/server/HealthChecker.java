package com.baitan.server;

import java.io.IOException;
import java.util.Arrays;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;

public class HealthChecker {

    private static HealthChecker instance;
    private DockerClient dockerClient;

    private HealthChecker() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    public static synchronized HealthChecker getInstance() {
        if (instance == null) {
            instance = new HealthChecker();
        }
        return instance;
    }

    public Server[] getHealthyServers() {
        return dockerClient.listContainersCmd().withStatusFilter(Arrays.asList("running"))

                .exec().stream().filter(container -> !Arrays.asList(container.getNames()).contains(("/load_balancer")))
                .map(container -> new Server(container.getNames()[0].replace("/", ""),
                        container.getPorts()[0].getPublicPort()))
                .toArray(Server[]::new);
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
