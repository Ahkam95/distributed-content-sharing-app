import java.util.*;  
import java.io.IOException;
import java.net.SocketException;

public class Starter {
    // constant variables
    public static final int NODE_REQUEST_TIMEOUT = 1000;
    public static final int NODE_SEARCH_TIMEOUT = 10000;

    // varibales
    public static String NODE_IP;
    public static int NODE_PORT;

    public static void main(String[] args) {
        // extract information from cmd
        extract(args);

        MessageBroker messageBroker = null;

        try {
            messageBroker = new MessageBroker();
        } catch (SocketException e){
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
                    startNode(messageBroker, scanner);
                    continue;
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
    // map to class variable NODE_IP and NODE_PORT 
    // if the command line input is valid.
    public static void extract(String[] args) {
        // default ip and port 
        String IP = "localhost";
        String PORT = "55556";

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("--node=")) {
                StringTokenizer stringTokenizer = new StringTokenizer(arg.substring(6), ":");
                IP = stringTokenizer.nextToken();
                PORT = stringTokenizer.nextToken();
            } else if (arg.toLowerCase().equals("--info")) {
                System.out.println("Usage: java Starter [--node=<ip>:<port>] [--info]");
                System.out.println("Default node\t= localhost:55556");
                System.exit(0);
            } else {
                System.out.println("Error: Invalid arguments.\nType 'java node --info' command for usage.");
                System.exit(0);
            }
        }

        NODE_IP = IP;
        NODE_PORT = Integer.parseInt(PORT);
    }

    public static void startNode(MessageBroker messageBroker, Scanner scanner){
        // create request format
        String request = "START";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        String response = null;
        
        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, NODE_IP, NODE_PORT, NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Cannot start the node.");
        }
        
        if (response.equals("0014 STARTOK 0")) {
            System.out.println("Node has started.");

            boolean terminate = false;
            while (true) {
                System.out.println("\nSelect an option:");
                System.out.println("1. Search a file");
                System.out.println("2. Print routing table");
                System.out.println("3. Print available files");
                System.out.println("4. Stop the node");
                System.out.print("\nEnter a option number: ");

                String option = scanner.nextLine();

                switch (option) {
                    case "1":
                        
                        break;
                    case "2":
                        
                        break;
                    case "3":
                        
                        break;
                    case "4":
                        
                    default:
                        System.out.println("Invalid option number. Try again.\n");
                }
            }
        } else if (response.equals("0017 STARTOK 9999")) {
            System.out.println("Error: An error occurred while starting the node.");
        } else {
            System.out.println("Error: An unknown error occurred while starting the node.");
        }  
    }

    public static void searchFile(){
        
    }

    public static void printRoutingTable(){
        
    }

    public static void printAvailableFiles(){
        
    }

}