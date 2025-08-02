package com.baitan.balancing_strategy;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import com.baitan.server.Server;

public class RoundRobinStrategy implements BalancingStrategy {

    private int currentIndex = 0;
    ConcurrentMap<Integer, Server> servers;

    public RoundRobinStrategy() {
        this.servers = new ConcurrentHashMap<Integer, Server>();
        this.currentIndex = 0; // Initialize the index to 0
    }

    @Override
    public void addServer(Server server) {
        int index = getServerCount();
        servers.put(index, server);
        System.out.println("Added server: " + server + " at index: " + index);
    }

    @Override
    public void removeServer(String host) {
        for (ConcurrentMap.Entry<Integer, Server> entry : servers.entrySet()) {
            if (entry.getValue().getHost().equals(host)) {
                servers.remove(entry.getKey());
                System.out.println("Removed server: " + entry.getValue() + " at index: " + entry.getKey());
                return;
            }
        }
        System.out.println("Server with host " + host + " not found.");
    }

    @Override
    public void removeServer(Server server) {
        int index = -1;
        for (ConcurrentMap.Entry<Integer, Server> entry : servers.entrySet()) {
            if (entry.getValue().equals(server)) {
                index = entry.getKey();
                break;
            }
        }

        if (index != -1) {
            servers.remove(index);
            System.out.println("Removed server: " + server + " at index: " + index);
        } else {
            System.out.println("Server not found: " + server);
        }

    }

    @Override
    public Server getNextServer() {
        if (servers.isEmpty()) {
            System.out.println("No servers available.");
            return null;
        }

        Server nextServer = servers.get(currentIndex);

        currentIndex = (currentIndex + 1) % servers.size(); // Increment index and wrap around

        return nextServer;
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
