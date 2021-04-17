package file_transfering;

import java.net.Socket;
import java.io.IOException;

public class FtpClient {
    public FtpClient(String ip, int port, String file) throws IOException {
        Socket srvr_sckt = new Socket(ip, port);
        Thread thread = new Thread(new ReceiveData(srvr_sckt, file));
        thread.start();
    }
}
