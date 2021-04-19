package query_handling;

import node.Node;
import file_template.FileTemplate;
import file_transfering.FtpClient;
import client.MessageBroker;
import exception.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class QueryHandler implements Runnable {
    private DatagramSocket datagram_sckt;
    private MessageBroker messageBroker;
    private ArrayList<Node> nodes;
    private ArrayList<FileTemplate> files;
    private final String node_ip;
    private final int NODE_PORT;
    private final String name;
    private final String server_ip;
    private final int server_port;

    public QueryHandler(ArrayList<Node> nodes, ArrayList<FileTemplate> files, String node_ip, int NODE_PORT,
            String name, String server_ip, int server_port) {
        this.nodes = nodes;
        this.files = files;
        this.node_ip = node_ip;
        this.NODE_PORT = NODE_PORT;
        this.name = name;
        this.server_ip = server_ip;
        this.server_port = server_port;

        try {
            this.datagram_sckt = new DatagramSocket(NODE_PORT);
        } catch (SocketException e) {
            System.out.println("datagram socket initiation error");
        }
        this.messageBroker = new MessageBroker(datagram_sckt);
    }

    @Override
    public void run() {
        while (true) {
            StringBuilder response = new StringBuilder();
            DatagramPacket incomingDatagramPacket = null;
            try {
                incomingDatagramPacket = messageBroker.receive(1000);
            } catch (IOException e) {
                continue;
            }
            try {
                String request = new String(incomingDatagramPacket.getData(), 0, incomingDatagramPacket.getLength());
                System.out.println(incomingDatagramPacket.getAddress() + ":" + incomingDatagramPacket.getPort() + " - "
                        + request.replace("\n", ""));
                StringTokenizer stringTokenizer = new StringTokenizer(request.trim(), " ");
                String length = stringTokenizer.nextToken();
                String command = stringTokenizer.nextToken();
                String ipAddress;
                int port;
                ArrayList<String> searchTerms;
                int hops;
                switch (command.toUpperCase()) {
                case "JOIN":
                    response.append("JOINOK ");
                    ipAddress = stringTokenizer.nextToken();
                    port = Integer.parseInt(stringTokenizer.nextToken());
                    try {
                        nodes.add(new node.Node(ipAddress, port));
                        response.append("0");
                    } catch (Exception e) {
                        response.append("9999");
                    }
                    break;
                case "LEAVE":
                    response.append("LEAVEOK ");
                    ipAddress = stringTokenizer.nextToken();
                    port = Integer.parseInt(stringTokenizer.nextToken());
                    boolean left = false;
                    for (node.Node node : nodes) {
                        if (node.getIp().equals(ipAddress) && node.getPort() == port) {
                            nodes.remove(node);
                            left = true;
                            break;
                        }
                    }
                    if (left) {
                        response.append("0");
                    } else {
                        response.append("9999");
                    }
                    break;
                case "SER":
                    response.append("SEROK ");
                    ipAddress = stringTokenizer.nextToken();
                    port = Integer.parseInt(stringTokenizer.nextToken());

                    searchTerms = new ArrayList<>();
                    String temp = stringTokenizer.nextToken();
                    if (temp.startsWith("\"")) {
                        if (temp.endsWith("\"")) {
                            searchTerms.add(temp.substring(1, temp.lastIndexOf("\"")));
                        } else {
                            searchTerms.add(temp.substring(1));
                            while (!(temp = stringTokenizer.nextToken()).endsWith("\"")) {
                                searchTerms.add(temp);
                            }
                            searchTerms.add(temp.substring(0, temp.length() - 1));
                        }
                    } else {
                        response.append("9998");
                        break;
                    }

                    hops = Integer.parseInt(stringTokenizer.nextToken());

                    ArrayList<FileTemplate> foundFiles = new ArrayList<>();
                    for (FileTemplate file : files) {
                        boolean isMatching = true;
                        ArrayList<String> fileNameSlices = new ArrayList<>(
                                Arrays.asList(file.getName().toLowerCase().split("\\s+")));
                        for (String searchTerm : searchTerms) {
                            if (!fileNameSlices.contains(searchTerm.toLowerCase())) {
                                isMatching = false;
                                break;
                            }
                        }
                        if (isMatching) {
                            foundFiles.add(file);
                        }
                    }

                    if (foundFiles.size() > 0) {
                        response.append(foundFiles.size() + " " + node_ip + " " + NODE_PORT + " " + hops);
                        for (FileTemplate file : foundFiles) {
                            response.append(" \"" + file.getName() + "\"");
                        }
                    } else if (hops < 5 && nodes.size() > 0) {
                        request = "SER " + node_ip + " " + NODE_PORT + " \"" + String.join(" ", searchTerms) + "\" "
                                + (hops + 1);
                        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
                        Collections.shuffle(nodes);
                        int i = 0;
                        while (i < Math.min(2, nodes.size())) {
                            Node node = nodes.get(i);
                            i++;
                            if (node.getIp().equals(ipAddress) && node.getPort() == port) {
                                continue;
                            }
                            try {
                                String mbResponse = messageBroker.sendAndReceive(request, node.getIp(), node.getPort(),
                                        10000);
                                response = new StringBuilder(mbResponse.substring(5));
                                break;
                            } catch (IOException e) {
                                System.out.println(
                                        "Error: Couldn't connect the node at " + node.getIp() + ":" + node.getPort());
                            }
                        }
                        if (response.toString().equals("SEROK ")) {
                            response.append("9999");
                        }
                    } else {
                        response.append("0");
                    }
                    break;
                case "START":
                    response.append("STARTOK ");
                    try {
                        request = "REG " + node_ip + " " + NODE_PORT + " " + name;
                        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
                        String mbResponse = messageBroker.sendAndReceive(request, server_ip, server_port, 10000);
                        System.out.println(mbResponse.trim());
                        stringTokenizer = new StringTokenizer(mbResponse.trim(), " ");
                        length = stringTokenizer.nextToken();
                        command = stringTokenizer.nextToken();
                        String nodesCount = stringTokenizer.nextToken();
                        switch (nodesCount) {
                        case "0":
                            System.out.println("Registration success. No nodes in the system yet.");
                            break;
                        case "9999":
                            throw new DistributedProjException("Registration failed. Invalid command");
                        case "9998":
                            throw new AlreadyRegisteredException("Already registered to you");
                        case "9997":
                            throw new AlreadyAssignedException("Try different IP and port");
                        case "9996":
                            throw new ServerFullException("Server is full.");
                        default:
                            while (stringTokenizer.hasMoreTokens()) {
                                ipAddress = stringTokenizer.nextToken();
                                port = Integer.parseInt(stringTokenizer.nextToken());
                                nodes.add(new node.Node(ipAddress, port));
                            }
                            System.out.println("Registered successfully.");
                        }
                    } catch (DistributedProjException | AlreadyRegisteredException | AlreadyAssignedException
                            | ServerFullException e) {
                        System.out.println(e.getMessage());
                        response.append("9999");
                    } catch (IOException e) {
                        System.out.println("Server registration error");
                        response.append("9999");
                    }
                    for (node.Node node : nodes) {
                        try {
                            request = "JOIN " + node_ip + " " + NODE_PORT;
                            request = String.format("%04d", request.length() + 5) + " " + request + "\n";
                            String mbResponse = messageBroker.sendAndReceive(request, node.getIp(), node.getPort(),
                                    10000);
                            System.out.println(mbResponse.trim());
                        } catch (IOException e) {
                            System.out.println("Couldn't join the node at " + node.getIp() + ":" + node.getPort());
                            if (!response.toString().contains("9999")) {
                                response.append("9999");
                            }
                        }
                    }
                    if (!response.toString().contains("9999")) {
                        response.append("0");
                    }
                    break;
                case "STOP":
                    response.append("STOPOK ");
                    for (node.Node node : nodes) {
                        try {
                            request = "LEAVE " + node_ip + " " + NODE_PORT;
                            request = String.format("%04d", request.length() + 5) + " " + request + "\n";
                            String mbResponse = messageBroker.sendAndReceive(request, node.getIp(), node.getPort(),
                                    10000);
                            System.out.println(mbResponse.trim());
                        } catch (IOException e) {
                            System.out.println("Couldn't leave the node at " + node.getIp() + ":" + node.getPort());
                            response.append("9999");
                        }
                    }
                    try {
                        request = "UNREG " + node_ip + " " + NODE_PORT + " " + name;
                        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
                        String mbResponse = messageBroker.sendAndReceive(request, server_ip, server_port, 10000);
                        System.out.println(mbResponse.trim());
                        stringTokenizer = new StringTokenizer(mbResponse);
                        length = stringTokenizer.nextToken();
                        command = stringTokenizer.nextToken();
                        String value = stringTokenizer.nextToken();
                        switch (value) {
                        case "0":
                            System.out.println("Unregistered successfully.");
                            break;
                        case "9999":
                            throw new DistributedProjException("Unable to unregister.");
                        }
                    } catch (DistributedProjException e) {
                        System.out.println(e.getMessage());
                        if (!response.toString().contains("9999")) {
                            response.append("9999");
                        }
                    } catch (IOException e) {
                        System.out.println("Couldn't unregister from the server.");
                        if (!response.toString().contains("9999")) {
                            response.append("9999");
                        }
                    }
                    if (!response.toString().contains("9999")) {
                        response.append("0");
                        nodes.clear();
                    }
                    break;
                case "DOWNLOAD":
                    response.append("DOWNLOADOK ");
                    ipAddress = stringTokenizer.nextToken();
                    port = Integer.parseInt(stringTokenizer.nextToken());

                    String fileName = "";
                    String tempNextToken = stringTokenizer.nextToken();
                    if (tempNextToken.startsWith("\"")) {
                        if (tempNextToken.endsWith("\"")) {
                            fileName += tempNextToken.substring(1, tempNextToken.lastIndexOf("\""));
                        } else {
                            fileName += tempNextToken.substring(1);
                            while (!(tempNextToken = stringTokenizer.nextToken()).endsWith("\"")) {
                                fileName += " " + tempNextToken;
                            }
                            fileName += " " + tempNextToken.substring(0, tempNextToken.lastIndexOf("\""));
                        }
                    } else {
                        response.append("9999");
                        break;
                    }

                    try {
                        FtpClient ftpClient = new FtpClient(ipAddress, port + 100, fileName);
                        String current = new java.io.File(".").getCanonicalPath();
                        System.out.println("Current dir:" + current);
                    } catch (IOException e) {
                        System.out.println("Error: Couldn't download the file.");
                        response.append("9999");
                    }
                    response.append("0");
                    break;
                case "PRINT":
                    response.append("PRINTOK ").append(nodes.size());
                    for (node.Node node : nodes) {
                        response.append(" ").append(node.getIp()).append(" ").append(node.getPort());
                    }
                    break;
                case "PRINTF":
                    response.append("PRINTFOK ").append(files.size());
                    for (FileTemplate file : files) {
                        response.append(" \"").append(file.getName()).append("\"");
                    }
                    break;
                default:
                    throw new IOException();
                }
            } catch (IOException | NoSuchElementException e) {
                response = new StringBuilder("ERROR");
            } finally {
                response = new StringBuilder(String.format("%04d", response.length() + 5) + " " + response + "\n");
                try {
                    DatagramPacket responseDatagramPacket = new DatagramPacket(response.toString().getBytes(),
                            response.length(), incomingDatagramPacket.getAddress(), incomingDatagramPacket.getPort());
                    messageBroker.send(responseDatagramPacket, 10000);
                } catch (IOException e) {
                    System.out.println("Error: Unable to send response.");
                }
            }
        }
    }
}
