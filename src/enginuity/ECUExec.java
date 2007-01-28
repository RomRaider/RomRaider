/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity;

import enginuity.swing.LookAndFeelManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ECUExec {

    private ECUExec() {
        throw new UnsupportedOperationException();
    }

    public static void main(String args[]) {

        // check for dodgy threading - dev only
//        RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager(true));

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

        // set look and feel
        LookAndFeelManager.initLookAndFeel();

        // launch editor
        ECUEditor editor = ECUEditorManager.getECUEditor();

        // open files, if passed
        try {
            if (args.length > 0) {
                editor.openImage(new File(args[0]).getCanonicalFile());
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
