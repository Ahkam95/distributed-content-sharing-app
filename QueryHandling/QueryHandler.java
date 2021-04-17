package QueryHandling;

import node.Node;
import file_template.FileTemplate;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class QueryHandler {
    private DatagramSocket datagramSocket;
    private ArrayList<Node> nodes;
    private ArrayList<FileTemplate> files;
    private final String node_ip;
    private final int port;
    private final String name;
    private final String server_ip;
    private final int server_port;

    public QueryHandler(ArrayList<Node> nodes, ArrayList<FileTemplate> files, String node_ip, int port, String name,
            String server_ip, int server_port) {
        this.nodes = nodes;
        this.files = files;
        this.node_ip = node_ip;
        this.port = port;
        this.name = name;
        this.server_ip = server_ip;
        this.server_port = server_port;

        // initializing datagram socket
        try {
            this.datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("datagram socket initiation error");
        }

    }

}
