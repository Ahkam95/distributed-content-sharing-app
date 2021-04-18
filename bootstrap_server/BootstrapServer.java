package bootstrapServer;

import constants.Constants;
import node.Node;
import client.MessageBroker;
import common.AlreadyAssignedException;
import common.AlreadyRegisteredException;
import common.CommandErrorException;
import common.ServerFullException;
    
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
    
public class BootstrapServer {

    private void executeCommand(String command, ArrayList<Node> nodes, StringBuilder response, StringTokenizer st){
        String up_command = command.toUpperCase()
        String ipAddress;
        String username;
        int port;

        if ( up_command.equals("REG")) {
            
            response.append("REGOK ");
            if (nodes.size() == Constants.MAX_NODES) {
                throw new ServerFullException("9996");
            }
            try {
                ipAddress = st.nextToken();
                port = Integer.parseInt(st.nextToken());
                username = st.nextToken();
            } catch (NoSuchElementException e) {
                throw new CommandErrorException("9999");
            }
            for (Node node : nodes) {
                if (!node.getIpAddress().equals(ipAddress)) {
                    continue;
                } else if (node.getPort() != port) {
                    continue;
                } else if (!node.getUsername().equals(username)) {
                    throw new AlreadyAssignedException("9997");
                } else {
                    throw new AlreadyRegisteredException("9998");
                }
            }
            response.append(nodes.size());
            for (Node node : nodes) {
                response.append(" ").append(node.getIpAddress()).append(" ").append(node.getPort());
            }
            nodes.add(new Node(ipAddress, port, username));
            break;
        }
        else if ( up_command.equals("UNREG")){
            response.append("UNROK ");
            try {
                ipAddress = st.nextToken();
                port = Integer.parseInt(st.nextToken());
                username = st.nextToken();
            } catch (NoSuchElementException e) {
                throw new CommandErrorException("9999");
            }
            boolean unregistered = false;
            for (Node node : nodes) {
                if (node.getIpAddress().equals(ipAddress) && node.getPort() == port) {
                    nodes.remove(node);
                    unregistered = true;
                    break;
                }
            }
            if (unregistered) {
                response.append("0");
            } else {
                response.append("9999");
            }
            break;
        }
        else if ( up_command.equals("PRINT")){    
            response.append("PRINTOK ").append(nodes.size());
            for (Node node : nodes) {
                response.append(" ").append(node.getIpAddress()).append(" ").append(node.getPort()).append(" ").append(node.getUsername());
            }
            break;
        }
        else if ( up_command.equals("RESET")){
            response.append("RESETOK 0");
            nodes.clear();
            break;
        }
        else{
            throw new IOException();       
        }
    }


    public static void main(String[] args) {
        ArrayList<Node> all_nodes = new ArrayList<>();

        // initializing datagram socket
        DatagramSocket sock = null;
        int MY_PORT = 55555;

        try {
            sock = new DatagramSocket(MY_PORT);
        } catch (SocketException e) {
            System.out.println("Failed to initialize the socket.");
            System.exit(0);
        }

        // initializing message broker
        MessageBroker messageBroker = new MessageBroker(sock);

        System.out.println("Bootstrap Server is created at " + MY_PORT + ". Waiting for incoming requests...");

        while (true) {

            StringBuilder response = new StringBuilder();
            DatagramPacket incoming  = null;

            try {
                incoming  = messageBroker.receive(Constants.SERVER_REQUEST_TIMEOUT);
            } catch (IOException e) {
                continue;
            }
            try {
                String request = new String(incoming .getData(), 0,
                incoming .getLength());
                System.out.println(incoming .getAddress() + ":" + incoming .getPort() +
                        " - " + request.replace("\n", ""));
                StringTokenizer st = new StringTokenizer(request.trim(), " ");
                String length;
                String command;
                
                try {
                    length = st.nextToken();
                    command = st.nextToken();
                } catch (NoSuchElementException e) {
                    throw new IOException();
                }
                
                BootstrapServer ser = new BootstrapServer();
                ser.executeCommand(command, all_nodes, response, st);

            } catch (IOException | AssertionError e) {
                response = new StringBuilder("ERROR");
            } catch (CommandErrorException | AlreadyAssignedException | AlreadyRegisteredException |
                    ServerFullException e) {
                response.append(e.getMessage());
            } finally {
                response = new StringBuilder(String.format("%04d", response.length() + 5) + " " + response + "\n");
                try {
                    DatagramPacket outgoing = new DatagramPacket(
                            response.toString().getBytes(),
                            response.toString().getBytes().length,
                            incoming .getAddress(),
                            incoming .getPort()
                    );
                    messageBroker.send(outgoing, Constants.SERVER_RESPONSE_TIMEOUT);
                } catch (IOException e) {
                    System.out.println("Failed to send response.");
                }
            }
        }
    }
}