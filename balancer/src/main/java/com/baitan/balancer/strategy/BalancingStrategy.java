package com.baitan.balancer.strategy;

import com.baitan.balancer.Service;

public interface BalancingStrategy {

    void addServer(Service server);

    void removeServer(Service server);

    void clearServers();

    int getServerCount();

    Service getNextServer();

    boolean containsServer(Service server);

    Service[] getServers();

    void synchronizeWithHealthyServers(Service[] healthyServers);

}
