package com.baitan.balancing_strategy;

import java.util.concurrent.ConcurrentMap;

public abstract class BalancingStrategy {

    protected ConcurrentMap<Integer, String> servers;

    public BalancingStrategy(ConcurrentMap<Integer, String> servers) {

    }

    public abstract String selectServer();

    public abstract void addServer(String server);

    public abstract void removeServer(String server);

    public abstract void clearServers();

    public abstract int getServerCount();

}
