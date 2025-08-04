package com.baitan.balancer.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.baitan.balancer.Service;

public class ConcurrentRoundRobinStrategy implements BalancingStrategy {

    private static volatile ConcurrentRoundRobinStrategy instance;

    private final AtomicInteger currentIndex;
    private final Lock lock;
    private final List<Service> servers;

    private ConcurrentRoundRobinStrategy() {
        this.servers = new CopyOnWriteArrayList<>();
        this.currentIndex = new AtomicInteger();
        this.lock = new ReentrantLock(true);
    }

    public static ConcurrentRoundRobinStrategy getInstance() {
        ConcurrentRoundRobinStrategy localInstance = ConcurrentRoundRobinStrategy.instance;
        if (localInstance == null) {
            synchronized (ConcurrentRoundRobinStrategy.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = new ConcurrentRoundRobinStrategy();
                }
            }
        }
        return instance;
    }

    public boolean isServerPresent(Service server) {
        return servers.stream().anyMatch(s -> s.getHost().equals(server.getHost()) && s.getPort() == server.getPort());
    }

    @Override
    public void addServer(Service server) {
        lock.lock();
        try {
            if (Service.isInvalid(server)) {
                System.err.println("Invalid server: " + server);
                return;
            }

            if (isServerPresent(server)) {
                System.out.println("Server already exists: " + server);
                return;
            }

            servers.add(server);
            System.out.println("Added server: " + server + " at index: " + (servers.size() - 1));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeServer(Service server) {
        lock.lock();
        try {
            if (Service.isInvalid(server)) {
                System.err.println("Invalid server: " + server);
                return;
            }
            int index = servers.indexOf(server);
            if (index != -1) {
                servers.remove(index);
                System.out.println("Removed server: " + server + " at index: " + index);
            } else {
                System.out.println("Server not found: " + server);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Service getNextServer() {
        if (servers.isEmpty()) {
            System.out.println("No servers available.");
            return null;
        }
        int size = servers.size();
        int index = currentIndex.getAndUpdate(i -> (i + 1) % size) % size;
        var server = servers.get(index);
        System.out.println("Next server: " + server + " at index: " + index);
        return server;
    }

    @Override
    public void clearServers() {
        lock.lock();
        try {
            servers.clear();
            currentIndex.set(0);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getServerCount() {
        return servers.size();
    }

    @Override
    public boolean containsServer(Service server) {
        if (Service.isInvalid(server)) {
            return false;
        }
        return servers.stream().anyMatch(s -> s.getHost().equals(server.getHost()) && s.getPort() == server.getPort());
    }

    @Override
    public Service[] getServers() {
        return servers.stream().toArray(Service[]::new);
    }

    @Override
    public void synchronizeWithHealthyServers(Service[] healthyServers) {
        lock.lock();
        try {
            if (healthyServers == null || healthyServers.length == 0) {
                clearServers();
                return;
            }

            for (Service server : healthyServers) {
                if (!containsServer(server)) {
                    addServer(server);

                }
            }

            Set<Service> healthySet = Arrays.stream(healthyServers).collect(Collectors.toSet());

            // Remove servers that are not in the healthy list
            var removed = servers.removeIf(server -> !healthySet.contains(server));

            System.out.println("Removed " + (removed ? "some" : "no") + " servers that are not healthy.");
        } finally {
            lock.unlock();
        }
    }
}