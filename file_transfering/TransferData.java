package file_transfering;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class TransferData implements Runnable {
    private Socket clk_sckt;
    private BufferedReader input = null;
    private String name;
    private final Logger LOG = Logger.getLogger(TransferData.class.getName());

    public TransferData(Socket client, String name) {
        this.clk_sckt = client;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clk_sckt.getInputStream()));
            DataInputStream dataIn = new DataInputStream(clk_sckt.getInputStream());
            String fileName = dataIn.readUTF();

            if (fileName != null) {
                fileSending(new File("." + System.getProperty("file.separator") + "files"
                        + System.getProperty("file.separator") + fileName));
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileSending(File file) {
        try {
            File s_file = file;
            byte[] arr = new byte[(int) file.length()];

            FileInputStream file_inp_str = new FileInputStream(s_file);
            BufferedInputStream buff_inp_str = new BufferedInputStream(file_inp_str);
            DataInputStream data_inp_str = new DataInputStream(buff_inp_str);
            data_inp_str.readFully(arr, 0, arr.length);

            DataOutputStream data_out_str = new DataOutputStream(clk_sckt.getOutputStream());
            data_out_str.writeUTF(s_file.getName());
            data_out_str.writeLong(arr.length);
            data_out_str.write(arr, 0, arr.length);
            data_out_str.flush();
            file_inp_str.close();
            LOG.fine(s_file.getName() + "File has sent");
        } catch (Exception e) {
            LOG.severe("file error");
            e.printStackTrace();
        }
    }
}
