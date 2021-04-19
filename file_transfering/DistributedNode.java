package file_transfering;

import file_template.FileTemplate;
import query_handling.QueryHandler;

import java.util.*;
import java.io.IOException;

public class DistributedNode {
    public static void main(String[] args) {
        String MY_IP = "localhost";
        int MY_PORT = 55556;
        String MY_USERNAME = "node@" + (int) (Math.random() * 10000);
        String SERVER_IP = "localhost";
        int SERVER_PORT = 55555;
        ArrayList<node.Node> nodes = new ArrayList<>();
        ArrayList<FileTemplate> files = new ArrayList<>();

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-port=")) {
                MY_PORT = Integer.parseInt(arg.substring(6));
            } else if (arg.toLowerCase().startsWith("-server=")) {
                StringTokenizer stringTokenizer = new StringTokenizer(arg.substring(8), ":");
                SERVER_IP = stringTokenizer.nextToken();
                SERVER_PORT = Integer.parseInt(stringTokenizer.nextToken());
            } else if (arg.toLowerCase().equals("-help")) {
                System.out.println("Usage: java node [-port=<port>] [-server=<ip>:<port>] [-help]\n");
                System.out.println("Default port\t= 55556\nDefault server\t= localhost:55555");
            } else {
                System.out.println("Error: Invalid arguments.\nUse 'java node -help' command for help.");
                System.exit(0);
            }
        }

        QueryHandler queryHandler = new QueryHandler(nodes, files, MY_IP, MY_PORT, MY_USERNAME, SERVER_IP, SERVER_PORT);
        Thread thread1 = new Thread(queryHandler);
        thread1.start();

        try {
            FtpServer ftpServer = null;
            ftpServer = new FtpServer(MY_PORT + 100, MY_USERNAME);
            Thread thread2 = new Thread(ftpServer);
            thread2.start();
        } catch (IOException e) {
            System.out.println("Error: Couldn't start the FTP server.");
            System.exit(0);
        }

        System.out.println("Node " + MY_USERNAME + " created at " + MY_IP + ":" + MY_PORT + ".");

        ArrayList<String> file_names = new ArrayList<String>(Arrays.asList("Glee", "The Vampire Diarie", "King Arthur",
                "Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight", "Windows 8",
                "Mission Impossible", "Turn Up The Music", "Super Mario", "American Pickers", "Microsoft Office 2010",
                "Happy Feet", "Modern Family", "American Idol", "Hacking for Dummies", "Adventures of Tintin",
                "Jack and Jill"));
        Collections.shuffle(file_names);
        System.out.println("Assigned files:");
        for (int i = 0; i < Math.min(5, (int) (Math.random() * 10) + 3); i++) {
            files.add(new FileTemplate(file_names.get(i)));
            System.out.print("\"" + file_names.get(i) + "\" ");
        }

        System.out.println("\nWaiting for requests.....");
    }
}
