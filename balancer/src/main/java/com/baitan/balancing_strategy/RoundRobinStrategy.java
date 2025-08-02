package com.baitan.balancing_strategy;

import java.util.concurrent.ConcurrentMap;

public class RoundRobinStrategy extends BalancingStrategy {

    private int currentIndex = 0;

    public RoundRobinStrategy(ConcurrentMap<Integer, String> servers) {
        super(servers);
        this.currentIndex = 0; // Initialize the index to 0
    }

    @Override
    public String selectServer() {
        if (servers.isEmpty()) {
            return null;
        }
        String server = servers.get(currentIndex);
        currentIndex = (currentIndex + 1) % servers.size();
        return server;
    }

    @Override
    public void addServer(String server) {
        // Implementation for adding a server
    }

    @Override
    public void removeServer(String server) {
        // Implementation for removing a server
    }

    @Override
    public void clearServers() {
        servers.clear();
        currentIndex = 0;
    }

    @Override
    public int getServerCount() {
        return servers.size();
    }
}
