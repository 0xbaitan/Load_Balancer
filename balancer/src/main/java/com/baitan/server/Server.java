package com.baitan.server;

public class Server {

    private final String host;
    private final int port;

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

    public static boolean isInvalid(Server server) {
        return server == null || server.getHost() == null || server.getHost().isEmpty() || server.getPort() <= 0;
    }

    @Override
    public String toString() {
        return "Server{" + "host='" + host + '\'' + ", port=" + port + '}';
    }

}
