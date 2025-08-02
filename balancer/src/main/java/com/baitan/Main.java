package com.baitan;

import java.io.IOException;
import com.baitan.server.LoadBalancer;

public class Main {
    public static void main(String[] args) throws IOException {
        LoadBalancer loadBalancer = LoadBalancer.getInstance();
        loadBalancer.start();
    }
}