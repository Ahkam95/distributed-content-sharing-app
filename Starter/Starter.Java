import java.util.*;  
import java.io.IOException;
import java.net.SocketException;

public class Starter {

    public static void main(String[] args) {

        Map<String, String> config = extract(args);
        String NODE_IP = config.get("IP");
        int NODE_PORT = Integer.parseInt(config.get("PORT"));

        String option, request, response;

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
            System.out.println("\nChoose an option to continue:");
            System.out.println("1. Start node");
            System.out.println("2. Exit");
            System.out.print("\nPlease enter the option number: ");

            // reading input from cli
            option = scanner.nextLine();

            switch (option) {
                case "1":
                    pass;

                case "2":
                    terminate = true;

                default:
                    System.out.println("\nInvalid input. Try again.\n");
            }
        }
    }

    // extract inputs from commond line
    // return a map which contains ip and port 
    // if the command line input is valid.
    public static Map<String,String> extract(String[] args) {
        // default ip
        String NODE_IP = "localhost";
        String NODE_PORT = "55556";

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

        Map<String,String> map = new HashMap<String,String>();

        map.put("IP", NODE_IP);  
        map.put("PORT", NODE_PORT); 

        return map;
    }
}