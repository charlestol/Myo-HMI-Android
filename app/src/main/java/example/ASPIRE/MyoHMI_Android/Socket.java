//package example.ASPIRE.MyoHMI_Android;
//
//import android.util.Log;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.UnknownHostException;
//
///**
// * Created by Alex on 9/1/2017.
// */
//
//public class Socket {
//
//    public static String PC_IP = "131.179.30.42";
//    public static String ANDROID_IP = "131.179.45.175";
//    public static Integer PORT = 9940;
//    private Socket mySocket;
//
//    public static void sendDataToServer() {
//
//        new Thread() {
//            public void run() {
//                DataOutputStream os = null;
//                // DataInputStream is = null;
//                BufferedReader is = null;
//
//                // initialize a Socket for TCP/IP communication; here Android device is client and PC is server
//                try {
//                    Socket mySocket;
//                    mySocket = new Socket();
//                    InetAddress serverAddr = InetAddress.getByName(PC_IP);
//                    mySocket.connect(new InetSocketAddress(serverAddr, PORT), 5000);
//                    os = new DataOutputStream(mySocket.getOutputStream());
//                    // is = new DataInputStream(mySocket.getInputStream());
//                    is = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
//                } catch (UnknownHostException exception) {
//                    Log.d("sunnyDay", exception.getMessage());
//                } catch (IOException exception) {
//                    Log.d("sunnyDay", exception.getMessage());
//                }
//
//                if (mySocket != null && os != null && is != null) {
//                    try {
//
//                        os.writeBytes("hey there");
//
//                        // close the Socket in client mode; later start in server mode to receiver result data from server.
//                        Log.d("sunnyDay", "Closing the Socket...");
//                        os.close();
//                        is.close();
//                        mySocket.close();
//                    }  catch (IOException exception) {
//                        Log.d("sunnyDay", exception.getMessage());
//                    }
//                }
//            }
//
//        }.start();
//
////        updateTextView2(textViewMessage);
//    }
//}
