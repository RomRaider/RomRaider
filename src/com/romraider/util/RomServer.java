package com.romraider.util;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class RomServer {
    private static final Logger LOGGER = getLogger(RomServer.class);
    private static final String HOST = "localhost";
    private static final int PORT = 54321;

    public static boolean isRunning() {
        try {
            ServerSocket sock = new ServerSocket(PORT);
            sock.close();
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    public static String waitForRom() throws IOException {
        ServerSocket sock = new ServerSocket(PORT);
        try {
            return waitForRom(sock);
        } finally {
            sock.close();
        }
    }

    public static void sendRomToOpenInstance(String rom) {
        try {
            Socket socket = new Socket(HOST, PORT);
            OutputStream os = socket.getOutputStream();
            try {
                write(os, rom);
            } finally {
                socket.close();
            }
        } catch (Throwable e) {
            LOGGER.error("Error occurred", e);
        }
    }

    private static void write(OutputStream os, String rom) {
        PrintWriter pw = new PrintWriter(os, true);
        try {
            pw.println(rom);
        } finally {
            pw.close();
        }
    }

    private static String waitForRom(ServerSocket sock) throws IOException {
        Socket client = sock.accept();
        try {
            return getRom(client);
        } finally {
            client.close();
        }
    }

    private static String getRom(Socket client) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        try {
            return br.readLine();
        } finally {
            br.close();
        }
    }
}
