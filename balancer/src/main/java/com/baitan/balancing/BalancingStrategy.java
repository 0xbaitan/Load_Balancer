package com.baitan.balancing;

import com.baitan.server.Server;

public interface BalancingStrategy {

    void addServer(Server server);

    void removeServer(Server server);

    void removeServer(String host);

    void clearServers();

    int getServerCount();

    Server getNextServer();
}
