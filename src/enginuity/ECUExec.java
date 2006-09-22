package enginuity;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ECUExec {
    public static void main(String args[]) {
        // try create socket listener for shell opening new files
        ServerSocket sock = null; // original server socket
        String serverName = "localhost";
        Socket clientSocket = null; // socket created by accept
        PrintWriter pw = null; // socket output stream
        BufferedReader br = null; // socket input stream
        int serverPort = 8753;

        try {
            sock = new java.net.ServerSocket(serverPort);               // create socket and bind to port
            sock.close();

        } catch (Exception ex) {
            // pass filename if file present
            if (args.length > 0) {

                try {
                    Socket socket = new java.net.Socket(serverName, serverPort);       // create socket and connect
                    pw = new java.io.PrintWriter(socket.getOutputStream(), true);  // create reader and writer
                    br = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                    pw.println(args[0]);                      // send msg to the server
                    String answer = br.readLine();                              // get data from the server

                    pw.close();                                                 // close everything
                    br.close();
                    socket.close();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
                // after sending filename, exit
                System.exit(0);
            }
        }

        // launch editor
        ECUEditor editor = ECUEditorManager.getECUEditor();

        // open files, if passed
        try {
            if (args.length > 0) {
                editor.openImage(new File(args[0]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // listen for files
        try {

            while (true) {
                sock = new java.net.ServerSocket(serverPort); // create socket and bind to port
                clientSocket = sock.accept(); // wait for client to connect
                pw = new java.io.PrintWriter(clientSocket.getOutputStream(), true);
                br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(clientSocket.getInputStream()));
                String msg = br.readLine(); // read msg from client

                // open file from client
                editor.openImage(new File(msg));

                pw.close();  // close everything
                br.close();
                clientSocket.close();
                sock.close();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
