package bootstrap_server;

import node.Node;
import client.MessageBroker;
import exception.DistributedProjException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class BootstrapServer {

    public static void main(String[] args) {
        ArrayList<Node> all_nodes = new ArrayList<>();

        DatagramSocket sock = null;
        int MY_PORT = 55555;

        try {
            sock = new DatagramSocket(MY_PORT);
        } catch (SocketException e) {
            System.out.println("Failed to initialize the socket.");
            System.exit(0);
        }

        MessageBroker messageBroker = new MessageBroker(sock);

        System.out.println("Bootstrap Server is created at " + MY_PORT + ". Waiting for incoming requests...");

        while (true) {

            StringBuilder response = new StringBuilder();
            DatagramPacket incoming = null;

            try {
                incoming = messageBroker.receive(10000);
            } catch (IOException e) {
                continue;
            }
            try {
                String request = new String(incoming.getData(), 0, incoming.getLength());
                System.out
                        .println(incoming.getAddress() + ":" + incoming.getPort() + " - " + request.replace("\n", ""));
                StringTokenizer st = new StringTokenizer(request.trim(), " ");
                String length;
                String command;

                try {
                    length = st.nextToken();
                    command = st.nextToken();
                } catch (NoSuchElementException e) {
                    throw new IOException();
                }

                String up_command = command.toUpperCase();
                String ipAddress;
                String username;
                int port;

                if (up_command.equals("REG")) {

                    response.append("REGOK ");
                    if (all_nodes.size() == 10) {
                        throw new DistributedProjException("9996");
                    }
                    try {
                        ipAddress = st.nextToken();
                        port = Integer.parseInt(st.nextToken());
                        username = st.nextToken();
                    } catch (NoSuchElementException e) {
                        throw new DistributedProjException("9999");
                    }
                    for (Node node : all_nodes) {
                        if (!node.getIp().equals(ipAddress)) {
                            continue;
                        } else if (node.getPort() != port) {
                            continue;
                        } else if (!node.getName().equals(username)) {
                            throw new DistributedProjException("9997");
                        } else {
                            throw new DistributedProjException("9998");
                        }
                    }
                    response.append(all_nodes.size());
                    for (Node node : all_nodes) {
                        response.append(" ").append(node.getIp()).append(" ").append(node.getPort());
                    }
                    all_nodes.add(new Node(ipAddress, port, username));

                } else if (up_command.equals("UNREG")) {
                    response.append("UNROK ");
                    try {
                        ipAddress = st.nextToken();
                        port = Integer.parseInt(st.nextToken());
                        username = st.nextToken();
                    } catch (NoSuchElementException e) {
                        throw new DistributedProjException("9999");
                    }
                    boolean unregistered = false;
                    for (Node node : all_nodes) {
                        if (node.getIp().equals(ipAddress) && node.getPort() == port) {
                            all_nodes.remove(node);
                            unregistered = true;
                            break;
                        }
                    }
                    if (unregistered) {
                        response.append("0");
                    } else {
                        response.append("9999");
                    }

                } else if (up_command.equals("PRINT")) {
                    response.append("PRINTOK ").append(all_nodes.size());
                    for (Node node : all_nodes) {
                        response.append(" ").append(node.getIp()).append(" ").append(node.getPort()).append(" ")
                                .append(node.getName());
                    }

                } else if (up_command.equals("RESET")) {
                    response.append("RESETOK 0");
                    all_nodes.clear();

                } else {
                    throw new IOException();
                }

            } catch (IOException | AssertionError e) {
                response = new StringBuilder("ERROR");
            } catch (DistributedProjException e) {
                response.append(e.getMessage());
            } finally {
                response = new StringBuilder(String.format("%04d", response.length() + 5) + " " + response + "\n");
                try {
                    DatagramPacket outgoing = new DatagramPacket(response.toString().getBytes(),
                            response.toString().getBytes().length, incoming.getAddress(), incoming.getPort());
                    messageBroker.send(outgoing, 10000);
                } catch (IOException e) {
                    System.out.println("Failed to send response.");
                }
            }
        }
    }
}