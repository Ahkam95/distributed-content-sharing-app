package file_transfering;

import java.io.*;
import java.net.Socket;

public class ReceiveData implements Runnable {
    private Socket srvr_sckt;
    private BufferedReader input = null;
    private String fileName;

    public ReceiveData(Socket socket, String fileName) {
        this.fileName = fileName;
        this.srvr_sckt = socket;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(srvr_sckt.getInputStream()));
            DataOutputStream data_out_str = new DataOutputStream(srvr_sckt.getOutputStream());
            data_out_str.writeUTF(fileName);
            data_out_str.flush();
            receiveFile();
            input.close();
        } catch (IOException e) {
            System.out.println("Connection closed due to server error");
        }
    }

    public void receiveFile() {
        try {
            int bytesRead;
            DataInputStream server_data = new DataInputStream(srvr_sckt.getInputStream());
            OutputStream output = new FileOutputStream(server_data.readUTF());
            long size = server_data.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = server_data.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            output.close();
            server_data.close();
        } catch (IOException e) {
            System.out.println("Connection closed due to server error");
        }
    }
}
