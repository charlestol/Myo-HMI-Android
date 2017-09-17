package example.ASPIRE.MyoHMI_Android;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alex on 9/15/2017.
 */

public class ServerComm {

    public static String PC_IP = "2601:645:c100:b669:ad86:cf34:9b81:48e3";
    public static String ANDROID_IP = "2601:645:c100:b669:e814:5ed6:d407:fc13";
    public static Integer PORT = 9940;
    public static String result;
    public static Socket mySocket = null;
    public static DataOutputStream os = null;
    public static BufferedReader is = null;
    static Thread sendThread;
    static Thread startThread;
    static Handler handler;
    static Message msg;
    private static ArrayList<String> mMessages = new ArrayList<>();

    public ServerComm(){

    }

    public static void connectToServer(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sendThread = new Thread(new Runnable() {
            public void run() {
                mySocket = null;
                os = null;
                try {
                    mySocket = new Socket(PC_IP, PORT);
                    os = new DataOutputStream(mySocket.getOutputStream());
                    while(true){
                        String message;
//                        System.out.println("!");
//                        synchronized (mMessages) {
//                            while (mMessages.isEmpty()) {
//                                try {
//                                    mMessages.wait();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            // Get message and remove from the list
//                            message = mMessages.get(0);
//                            mMessages.remove(0);
//                        }
                        String outMsg = "hola";
                        os.writeBytes(outMsg);
                        os.flush();
                    }
                } catch (UnknownHostException exception) {
                    Log.d("sunnyDay", exception.getMessage());
                } catch (IOException exception) {
                    Log.d("sunnyDay", exception.getMessage());
                }
            }
        });
        sendThread.start();
    }

    public static void sendDataToServer(byte[] data) {

        String dataString = Arrays.toString(data);

        synchronized (mMessages) {
            mMessages.add(dataString);
            mMessages.notify();
//            System.out.println(mMessages.get(0));
        }
    }

    public static void receiveResultFromServer() {

        Thread t = new Thread() {
            public void run() {

                // initailize a Socket. Here Android device is server and PC is client.
                ServerSocket echoServer = null;
                String line;
                // DataInputStream is;
                BufferedReader is;
                PrintStream os;
                Socket clientSocket = null;

                Log.d("sunnyDay", "Initializing Socket..." + PC_IP);
                try {
                    echoServer = new ServerSocket(PORT);

                } catch (IOException e) {
                    System.out.println(e);
                }
                // Create a socket object from the ServerSocket to listen and accept
                // connections.
                // Open input and output streams
                try {

                    clientSocket = echoServer.accept();
                    Log.d("cloudyDay", "accepted Socket...");
                    // is = new DataInputStream(clientSocket.getInputStream());
                    is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                    os = new PrintStream(clientSocket.getOutputStream());

                    Log.d("sunnyDay", "Connection with client established. Listening for incoming messages...");
                    while (true) {
                        line = is.readLine();
                        // os.println(line);
                        // Log.d("sunnyDay", "Echoed the message from client.");

                        result = line; // received the String from PC for result
                        Log.d("sunnyDay", result);
                        Log.d("sunnyDay", "Received analysis result. Closing server...");
                        break;
                    }
                } catch (IOException e) {
                    Log.d("sunnyDay", e.getMessage());
                }

                // After receiving the analysis result from PC, close the serverSocket.
                Log.d("sunnyDay", "Closing sockets...");
                try {
                    clientSocket.close();
                    echoServer.close();
                } catch (IOException exception) {
                    Log.d("sunnyDay", exception.getMessage());
                }

                Log.d("sunnyDay", "receiveResultFromServer() thread finishes.");
            }
        };

        t.start();
        try {
            // Block the main thread until thread t finishes, or after 3 seconds. Then update data analysis result.
            t.join(3000);
        } catch (InterruptedException ex) {
            Log.d("sunnyDay", ex.getMessage());
        }

        System.out.println(result);
    }
}
