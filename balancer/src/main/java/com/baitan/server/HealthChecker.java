package com.baitan.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;

public class HealthChecker {

    private DockerClient dockerClient;

    public HealthChecker() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    public String[] getHealthyContainers() {
        return dockerClient.listContainersCmd()
                .withStatusFilter("running")
                .exec()
                .stream()
                .map(Container::getNames)
                .flatMap(names -> java.util.Arrays.stream(names))
                .toArray(String[]::new);
    }

    public void close() {
        dockerClient.close();
    }

}
