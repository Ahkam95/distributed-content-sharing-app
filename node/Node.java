package node;

public class Node {
    private final String ip;
    private final int port;
    private String name;

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Node(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }
}
