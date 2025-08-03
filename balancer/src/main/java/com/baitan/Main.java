package com.baitan;

import java.io.IOException;

import com.baitan.server.ConcurrentLoadBalancer;

public class Main {
    public static void main(String[] args) throws IOException {
        ConcurrentLoadBalancer.getInstance().start();

        // HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // HealthChecker healthChecker = HealthChecker.getInstance();
        // BalancingStrategy balancingStrategy = ConcurrentRoundRobinStrategy.getInstance();

        // Server[] healthyServers = healthChecker.getHealthyServers();
        // for (Server s : healthyServers) {
        //     balancingStrategy.addServer(s);
        // }


        
        // server.createContext("/", ProxyHandler.getInstance());

        // server.setExecutor(Executors.newFixedThreadPool(10));
        // server.start();
        }
        
        

}