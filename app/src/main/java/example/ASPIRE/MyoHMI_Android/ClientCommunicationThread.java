package example.ASPIRE.MyoHMI_Android;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientCommunicationThread extends Thread {

    public final static int TCP_SERVER_PORT = 9941;

    private ArrayList<byte[]> mMessages = new ArrayList<>();
    private String mServer;

    private boolean mRun = true;

    private final String ec2ip = "34.213.61.15";
    private final String alexHomeip = "2601:645:c100:b669:ad86:cf34:9b81:48e3";
    private final String icelabip = "34.213.61.15";
    private final String sfStateip = "10.143.132.221";

    int count = 0;

    byte[] buffer = new byte[512];

    int length;

    public ClientCommunicationThread() {
        this.mServer = alexHomeip;
    }

    @Override
    public void run() {

        while (mRun) {
            Socket s = null;
            try {

                s = new Socket(mServer, TCP_SERVER_PORT);
                DataInputStream input = new DataInputStream(s.getInputStream());

                while (mRun) {
                    if ((length = input.read(buffer)) != -1)
                        Log.d("Cloud Prediction: ", String.valueOf(buffer[0]) + "  :  " + String.valueOf(System.currentTimeMillis()));
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //close connection
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void close() {
        mRun = false;
    }
}