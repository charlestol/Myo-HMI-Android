package example.ASPIRE.MyoHMI_Android;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerCommunicationThread extends Thread {

    public final static int TCP_SERVER_PORT = 9940;

    private ArrayList<byte[]> mMessages = new ArrayList<>();
    private String mServer;

    private boolean mRun = true;

    public ServerCommunicationThread(String server) {
        this.mServer = "2601:645:c100:b669:ad86:cf34:9b81:48e3";
    }

    @Override
    public void run() {

        while (mRun) {
            Socket s = null;
            try {
                s = new Socket(mServer, TCP_SERVER_PORT);

//                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                DataOutputStream output = new DataOutputStream(s.getOutputStream());

//                ByteArrayOutputStream arrayOutputStream = (ByteArrayOutputStream)s.getOutputStream();

//                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                while (mRun) {
                    byte[] message;

                    // Wait for message
                    synchronized (mMessages) {
                        while (mMessages.isEmpty()) {
                            try {
                                mMessages.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // Get message and remove from the list
                        message = mMessages.get(0);
                        mMessages.remove(0);
                    }

                    Log.d("sent", Arrays.toString(message));
                    output.write(message);
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

    public void send(byte[] message) {
        synchronized (mMessages) {
            mMessages.add(message);
            mMessages.notify();
        }
    }

    public void close() {
        mRun = false;
    }
}