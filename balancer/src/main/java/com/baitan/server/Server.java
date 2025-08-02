package com.baitan.server;

public class Server {

    private String host;
    private int port;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void routeRequest(String request) {
        // Logic to route the request to this server
        System.out.println("Routing request to server: " + host + ":" + port + " with request: " + request);
    }

    @Override
    public String toString() {
        return "Server{" + "host='" + host + '\'' + ", port=" + port + '}';
    }

}
