package file_transfering;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import java.io.IOException;

public class FtpServer implements Runnable {
    private ServerSocket srvr_sckt;
    private Socket clk_sckt;
    private String name;
    private final Logger LOG = Logger.getLogger(FtpServer.class.getName());

    public FtpServer(int port, String name) throws IOException {
        this.srvr_sckt = new ServerSocket(port);
        this.name = name;
    }

    public int getPort() {
        return srvr_sckt.getLocalPort();
    }

    @Override
    public void run() {
        while (true) {
            try {
                clk_sckt = srvr_sckt.accept();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Thread thread = new Thread(new TransferData(clk_sckt, name));
            thread.start();
        }
    }
}
