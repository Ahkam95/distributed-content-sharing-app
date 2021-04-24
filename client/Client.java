package client;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    public static void main(String[] args) {
        String ip = "localhost";
        int PORT_ = 55556;
        MessageBroker messageBroker = null;
        Scanner scanner = new Scanner(System.in);
        String option;
        String req;
        String res;
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-node=")) {
                StringTokenizer stringTokenizer = new StringTokenizer(arg.substring(6), ":");
                ip = stringTokenizer.nextToken();
                PORT_ = Integer.parseInt(stringTokenizer.nextToken());
            } else if (arg.toLowerCase().equals("-help")) {
                System.out.println("Usage: java client.Client [-node=<ip>:<port>] [-help]\n");
                System.out.println("Default node\t= localhost:55556");
            } else {
                System.out.println("Error arguments");
                System.exit(0);
            }
        }
        // initializing message broker
        try {
            messageBroker = new MessageBroker();
        } catch (SocketException e) {
            System.out.println("message broker error");
            System.exit(0);
        }
        loop1: while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Start the node");
            System.out.println("2. Exit from DS");
            System.out.print("\nYour Option: ");
            option = scanner.nextLine();
            switch (option) {
            case "1":
                req = "START";
                req = String.format("%04d", req.length() + 5) + " " + req + "\n";
                res = null;
                try {
                    res = messageBroker.sendAndReceive(req, ip, PORT_, 10000).trim();
                } catch (IOException e) {
                    System.out.println("Unable to start the node.");
                }
                if (res.equals("0014 STARTOK 0")) {
                    System.out.println("Node started successfully.");
                    loop2: while (true) {
                        System.out.println("\nChoose an option:");
                        System.out.println("1. Search for a file");
                        System.out.println("2. Printing the routing table");
                        System.out.println("3. Printing files of the Node");
                        System.out.println("4. Stop the node");
                        System.out.print("\nYour option: ");
                        option = scanner.nextLine();
                        switch (option) {
                        case "1":
                            System.out.print("File name to search: ");
                            String fileName = scanner.nextLine();
                            req = "SER " + ip + " " + PORT_ + " \"" + fileName + "\" 0";
                            req = String.format("%04d", req.length() + 5) + " " + req + "\n";
                            res = null;
                            try {
                                res = messageBroker.sendAndReceive(req, ip, PORT_, 10000).trim();
                                StringTokenizer stringTokenizer = new StringTokenizer(res);
                                String length = stringTokenizer.nextToken();
                                String command = stringTokenizer.nextToken();
                                int fileCount = Integer.parseInt(stringTokenizer.nextToken());
                                if (fileCount == 9999 || fileCount == 9998) {
                                    System.out.println("\nNo files available for the search file'" + fileName + "'");
                                    break;
                                }
                                String ipAddress = stringTokenizer.nextToken();
                                int port = Integer.parseInt(stringTokenizer.nextToken());
                                int hops = Integer.parseInt(stringTokenizer.nextToken());
                                ArrayList<String> fileNames = new ArrayList<>();
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
                                        System.out.println("Error: Invalid search result.");
                                        break;
                                    }
                                }
                                if (fileNames.size() > 0) {
                                    System.out.println("\nAvailable files for the search term '" + fileName + "':");
                                    for (int i = 0; i < fileNames.size(); i++) {
                                        System.out.println((i + 1) + ". " + fileNames.get(i));
                                    }
                                    System.out.print("\nSelect a file to download: ");
                                    int downloadOption = Integer.parseInt(scanner.nextLine());
                                    req = "DOWNLOAD " + ipAddress + " " + port + " \""
                                            + fileNames.get(downloadOption - 1) + "\"";
                                    req = String.format("%04d", req.length() + 5) + " " + req + "\n";
                                    res = messageBroker.sendAndReceive(req, ip, PORT_, 10000).trim();
                                    if (res.equals("0017 DOWNLOADOK 0")) {
                                        System.out.println("File successfully downloaded.");
                                    } else {
                                        System.out.println("Unable to download the file.");
                                    }
                                } else {
                                    System.out.println("\nNo files available for the search term '" + fileName + "':");
                                }
                            } catch (IOException e) {
                                System.out.println("Error: Unable to search.");
                            }
                            break;
                        case "2":
                            req = "PRINT";
                            req = String.format("%04d", req.length() + 5) + " " + req + "\n";
                            res = null;
                            try {
                                res = messageBroker.sendAndReceive(req, ip, PORT_, 1000).trim();
                                StringTokenizer stringTokenizer = new StringTokenizer(res);
                                String length = stringTokenizer.nextToken();
                                String command = stringTokenizer.nextToken();
                                int nodesCount = Integer.parseInt(stringTokenizer.nextToken());
                                if (nodesCount == 0) {
                                    System.out.println("Routing table is empty.");
                                    break;
                                } else {
                                    int i = 1;
                                    while (stringTokenizer.hasMoreTokens()) {
                                        System.out.print(i + ". ");
                                        System.out.print(stringTokenizer.nextToken());
                                        System.out.print(":");
                                        System.out.println(Integer.parseInt(stringTokenizer.nextToken()));
                                        i++;
                                    }
                                }
                            } catch (IOException e) {
                                System.out.println("Printing routing table error");
                            }
                            break;
                        case "3":
                            req = "PRINTF";
                            req = String.format("%04d", req.length() + 5) + " " + req + "\n";
                            res = null;
                            try {
                                res = messageBroker.sendAndReceive(req, ip, PORT_, 1000).trim();
                            } catch (IOException e) {
                                System.out.println("Error printing available files.");
                            }
                            StringTokenizer stringTokenizer = new StringTokenizer(res);
                            String length = stringTokenizer.nextToken();
                            String command = stringTokenizer.nextToken();
                            int filesCount = Integer.parseInt(stringTokenizer.nextToken());
                            ArrayList<String> fileNames = new ArrayList<>();
                            for (int i = 0; i < filesCount; i++) {
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
                                    System.out.println("Error: Unable to print files list.");
                                    break;
                                }
                            }
                            System.out.println("Available files in the current node are listed below.");
                            for (int i = 0; i < fileNames.size(); i++) {
                                System.out.println((i + 1) + ". " + fileNames.get(i));
                            }
                            break;
                        case "4":
                            req = "STOP";
                            req = String.format("%04d", req.length() + 5) + " " + req + "\n";
                            res = null;
                            try {
                                res = messageBroker.sendAndReceive(req, ip, PORT_, 1000).trim();
                            } catch (IOException e) {
                                System.out.println("Unable to stop the server.");
                            }
                            if (res.equals("0013 STOPOK 0")) {
                                System.out.println("Node stopped successfully.");
                                break loop2;
                            } else if (res.equals("0016 STOPOK 9999")) {
                                System.out.println("Got an error while stopping the node");
                                break loop2;
                            } else {
                                System.out.println("Error occured");
                                break loop2;
                            }
                        default:
                            System.out.println("Invalid option");
                        }
                    }
                } else if (res.equals("0017 STARTOK 9999")) {
                    System.out.println("Node starting Error");
                } else {
                    System.out.println("Error occured");
                }
                break;
            case "2":
                break loop1;
            default:
                System.out.println("\nInvalid option\n");
            }
        }

    }
}
