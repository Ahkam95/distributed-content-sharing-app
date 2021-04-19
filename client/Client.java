package client;

import java.util.*;
import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    // constant variables
    public static final int NODE_REQUEST_TIMEOUT = 1000;
    public static final int NODE_SEARCH_TIMEOUT = 10000;

    public static void main(String[] args) {
        // extract information from cmd
        Map<String, String> config = extract(args);
        String NODE_IP = config.get("IP");
        int NODE_PORT = Integer.parseInt(config.get("PORT"));

        MessageBroker messageBroker = null;

        try {
            messageBroker = new MessageBroker();
        } catch (SocketException e) {
            System.out.println("Error: Couldn't initiate the message broker.");
            System.exit(0);
        }

        // to read input from cli
        Scanner scanner = new Scanner(System.in);

        boolean terminate = false;
        while (!terminate) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Start a node");
            System.out.println("2. Quite");
            System.out.print("\nEnter a option number: ");

            // reading input from cli
            String option = scanner.nextLine();

            switch (option) {
            case "1":
                startNode(messageBroker, scanner, NODE_IP, NODE_PORT);
                break;
            case "2":
                terminate = true;
                System.out.println("Exited from menu.");
                continue;
            default:
                System.out.println("\nInvalid input. Try again.\n");
            }
        }
    }

    // extract inputs from commond line
    // return a map which contains ip and port
    // if the command line input is valid.
    public static Map<String, String> extract(String[] args) {
        // default ip
        String NODE_IP = "localhost";
        String NODE_PORT = "55556";
        System.out.println(args);
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("--node=")) {
                StringTokenizer stringTokenizer = new StringTokenizer(arg.substring(6), ":");
                NODE_IP = stringTokenizer.nextToken();
                NODE_PORT = stringTokenizer.nextToken();
            } else if (arg.toLowerCase().equals("--info")) {
                System.out.println("Usage: java Starter [--node=<ip>:<port>] [--info]");
                System.out.println("Default node\t= localhost:55556");
                System.exit(0);
            } else {
                System.out.println("Error: Invalid arguments.\nType 'java node --info' command for usage.");
                System.exit(0);
            }
        }

        Map<String, String> map = new HashMap<String, String>();

        map.put("IP", NODE_IP);
        map.put("PORT", NODE_PORT);

        return map;
    }

    public static void startNode(MessageBroker messageBroker, Scanner scanner, String nodeIP, int nodePORT) {
        // creating request string
        String request = "START";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        String response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, nodeIP, nodePORT, NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Cannot start the node.");
        }

        if (response.equals("0014 STARTOK 0")) {
            System.out.println("Node has started.");

            boolean terminate = false;
            while (!terminate) {
                System.out.println("\nSelect an option:");
                System.out.println("1. Search a file");
                System.out.println("2. Print routing table");
                System.out.println("3. Print available files");
                System.out.println("4. Stop the node");
                System.out.print("\nEnter a option number: ");

                String option = scanner.nextLine();

                switch (option) {
                case "1":
                    searchFile(messageBroker, scanner, nodeIP, nodePORT);
                    break;
                case "2":
                    printRoutingTable(messageBroker, nodeIP, nodePORT);
                    break;
                case "3":
                    printAvailableFiles(messageBroker, nodeIP, nodePORT);
                    break;
                case "4":
                    stopNode(messageBroker, nodeIP, nodePORT);
                    terminate = true;
                    continue;
                default:
                    System.out.println("Invalid option number. Try again.\n");
                }
            }
        } else if (response.equals("0017 STARTOK 9999")) {
            System.out.println("Error: An error occurred.");
            return;
        } else {
            System.out.println("Error: An unknown error occurred.");
            return;
        }
    }

    public static void searchFile(MessageBroker messageBroker, Scanner scanner, String nodeIP, int nodePORT) {
        // getting filename input
        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine();

        // creating request string
        String request = "SER " + nodeIP + " " + nodePORT + " \"" + fileName + "\" 0";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        String response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, nodeIP, nodePORT, NODE_SEARCH_TIMEOUT).trim();

            StringTokenizer stringTokenizer = new StringTokenizer(response);
            String length = stringTokenizer.nextToken();
            String command = stringTokenizer.nextToken();
            int fileCount = Integer.parseInt(stringTokenizer.nextToken());
            if (fileCount == 9999 || fileCount == 9998) {
                System.out.println("\nFiles not found for :'" + fileName + "'");
                return;
            }

            String ipAddress = stringTokenizer.nextToken();
            int port = Integer.parseInt(stringTokenizer.nextToken());
            int hops = Integer.parseInt(stringTokenizer.nextToken());

            // create array for store matching file names
            ArrayList<String> fileNames = new ArrayList<>();

            // searching file names
            for (int i = 0; i < fileCount; i++) {
                String tempToken = stringTokenizer.nextToken();
                if (tempToken.startsWith("\"")) {
                    if (tempToken.endsWith("\"")) {
                        fileNames.add(tempToken.substring(1, tempToken.lastIndexOf("\"")));
                    } else {
                        String tempFileName = tempToken.substring(1);
                        while (!(tempToken = stringTokenizer.nextToken()).endsWith("\"")) {
                            tempFileName += " " + tempToken;
                        }
                        tempFileName += " " + tempToken.substring(0, tempToken.length() - 1);
                        fileNames.add(tempFileName);
                    }
                } else {
                    System.out.println("Error: Wrong search result.");
                    return;
                }
            }

            // if files availabele
            // then fo this
            if (fileNames.size() > 0) {
                System.out.println("\nAvailable files for the search term '" + fileName + "':");
                for (int i = 0; i < fileNames.size(); i++) {
                    System.out.println((i + 1) + ". " + fileNames.get(i));
                }
                return;
            } else {
                System.out.println("\nFile not found for: '" + fileName + "':");
                return;
            }
        } catch (IOException e) {
            System.out.println("Error: Cound not search.");
            return;
        }
    }

    public static void printRoutingTable(MessageBroker messageBroker, String nodeIP, int nodePORT) {
        // creating request string
        String request = "PRINT";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        String response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, nodeIP, nodePORT, NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Could not print the routing table.");
            return;
        }

        System.out.println(response);
        return;
    }

    public static void printAvailableFiles(MessageBroker messageBroker, String nodeIP, int nodePORT) {
        // creating request string
        String request = "PRINTF";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        String response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, nodeIP, nodePORT, NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Could not print available files.");
            return;
        }

        System.out.println(response);
        return;
    }

    public static void stopNode(MessageBroker messageBroker, String nodeIP, int nodePORT) {
        // creating request string
        String request = "STOP";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        String response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, nodeIP, nodePORT, NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Unable to stop the server.");
        }

        // stop according to response
        if (response.equals("0013 STOPOK 0")) {
            System.out.println("Node has stopped.");
            return;
        } else if (response.equals("0016 STOPOK 9999")) {
            System.out.println("Error: An error occurred and node stopped.");
            return;
        } else {
            System.out.println("Error: An unknown error occurred and node stopped.");
            return;
        }
    }

}