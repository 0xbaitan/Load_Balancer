package com.baitan.balancing;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.baitan.server.Server;

public class ConcurrentRoundRobinStrategy implements BalancingStrategy {

    private static volatile ConcurrentRoundRobinStrategy instance;

    private AtomicInteger currentIndex;
    private final Lock lock;
    private final List<Server> servers;

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

    public boolean isServerPresent(Server server) {
        return servers.stream().anyMatch(s -> s.getHost().equals(server.getHost()) && s.getPort() == server.getPort());
    }

    @Override
    public void addServer(Server server) {
        lock.lock();
        try {
            if (Server.isInvalid(server)) {
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
    public void removeServer(Server server) {
        lock.lock();
        try {
            if (Server.isInvalid(server)) {
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
    public Server getNextServer() {
        if (servers.isEmpty()) {
            System.out.println("No servers available.");
            return null;
        }
        int index = currentIndex.getAndUpdate(i -> (i + 1) % servers.size());
        return servers.get(index);
    }

    @Override
    public void clearServers() {
        lock.lock();
        try {
            servers.clear();
            currentIndex = new AtomicInteger();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getServerCount() {
        return servers.size();
    }

    @Override
    public boolean containsServer(Server server) {
        if (Server.isInvalid(server)) {
            return false;
        }
        return servers.stream().anyMatch(s -> s.getHost().equals(server.getHost()) && s.getPort() == server.getPort());
    }

    @Override
    public Server[] getServers() {
        return servers.stream().toArray(Server[]::new);
    }

    @Override
    public void manageListOfServers(Server[] healthyServers) {
        lock.lock();
        try {
            if (healthyServers == null || healthyServers.length == 0) {
                clearServers();
                return;
            }

            for (Server server : healthyServers) {
                if (!containsServer(server)) {
                    addServer(server);
                }
            }

            // Remove servers that are not in the healthy list
            var removed = servers.removeIf(server -> Arrays.stream(healthyServers)
                    .noneMatch(s -> s.getHost().equals(server.getHost()) && s.getPort() == server.getPort()));

            System.out.println("Removed " + (removed ? "some" : "no") + " servers that are not healthy.");
        } finally {
            lock.unlock();
        }
    }
}