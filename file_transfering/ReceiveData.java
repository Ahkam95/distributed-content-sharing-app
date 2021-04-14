package file_transfering;

import java.io.*;
import java.net.Socket;

public class ReceiveData implements Runnable {
    private Socket srvr_sckt;
    private BufferedReader in = null;
    private String fileName;

    public ReceiveData(Socket socket, String fileName) {
        this.srvr_sckt = socket;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(srvr_sckt.getInputStream()));
            DataOutputStream dOut = new DataOutputStream(srvr_sckt.getOutputStream());
            dOut.writeUTF(fileName);
            dOut.flush();

            String canonicalPath = new File(".").getCanonicalPath();
            System.out.println("Current directory path using canonical path method: " + canonicalPath);

            receiveFile();
            in.close();
        } catch (IOException e) {
            System.out.println("Error: Server error. Connection closed.");
        }
    }

    public void receiveFile() {
        try {
            int bytesRead;
            DataInputStream serverData = new DataInputStream(srvr_sckt.getInputStream());
            String fileName = serverData.readUTF();
            OutputStream output = new FileOutputStream(fileName);
            long size = serverData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = serverData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            output.close();
            serverData.close();
        } catch (IOException e) {
            System.out.println("Error: Server error. Connection closed.");
        }
    }
}
