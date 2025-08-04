package com.baitan;

import java.io.IOException;

import com.baitan.balancer.ConcurrentLoadBalancer;

public class Main {
    public static void main(String[] args) throws IOException {
        ConcurrentLoadBalancer.getInstance().start();
    }

}