import java.util.*;  
import java.io.IOException;
import java.net.SocketException;

public class Client {
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
         // creating request string 
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

    public static void searchFile(MessageBroker messageBroker, Scanner scanner){
        // getting filename input
        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine();

        // creating request string 
        request = "SER " + NODE_IP + " " + NODE_PORT + " \"" + fileName + "\" 0";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, NODE_IP, NODE_PORT, NODE_SEARCH_TIMEOUT).trim();

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

    public static void printRoutingTable(MessageBroker messageBroker){
        // creating request string 
        request = "PRINT";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, NODE_IP, NODE_PORT, NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Could not print the routing table.");
            return;
        }
        
        System.out.println(response);
        return;
    }

    public static void printAvailableFiles(MessageBroker messageBroker){
        // creating request string 
        request = "PRINTF";
        request = String.format("%04d", request.length() + 5) + " " + request + "\n";
        response = null;

        try {
            // send request and recive respose from message broker
            response = messageBroker.sendAndReceive(request, NODE_IP, NODE_PORT, Constants.NODE_REQUEST_TIMEOUT).trim();
        } catch (IOException e) {
            System.out.println("Error: Could not print available files.");
            return;
        }

        System.out.println(response);
        return;
    }

}