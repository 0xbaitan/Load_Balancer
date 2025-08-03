package com.baitan.balancing;

import com.baitan.server.Server;

public interface BalancingStrategy {

    void addServer(Server server);

    void removeServer(Server server);

    void clearServers();

    int getServerCount();

    Server getNextServer();

    boolean containsServer(Server server);

    Server[] getServers();

    void manageListOfServers(Server[] healthyServers);
    
}
