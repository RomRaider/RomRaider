/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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
 */

package com.romraider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class EditorLoggerCommunication {

    public enum Exec_type {EDITOR, LOGGER, UNKNOWN};
    
    private static final String HOST = "localhost";
    private static final int PORT = 23272;
    
    private static Exec_type currentExecType;
    private static String[] currentArgs;
    
    static public class ExecutableInstance {
    	public Exec_type execType;
    	public String[] currentArgs;
    }
     
    public static void setExectable(Exec_type t, String[] a) {
    	currentExecType = t;
    	currentArgs = a;
    }
    
    public static Exec_type getExecutableType() {
    	return currentExecType;
    }
    
    public static String[] getExecutableArgs() {
    	return currentArgs;
    }

    public static boolean isRunning() {
        try {
            ServerSocket sock = new ServerSocket(PORT);
            sock.close();
            return false;
        } catch (IOException ex) {
            return true;
        }
    }

    public static ExecutableInstance waitForOtherExec() throws IOException {
        ServerSocket sock = new ServerSocket(PORT);
        
        try {
            Socket client = sock.accept();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String type_String =  br.readLine();
            String args_String =  br.readLine();
            
            ExecutableInstance instance = new ExecutableInstance();
                      
            if(type_String.equalsIgnoreCase(Exec_type.EDITOR.toString())) {
            	instance.execType =  Exec_type.EDITOR;
            }
            else if(type_String.equalsIgnoreCase(Exec_type.LOGGER.toString())) {
            	instance.execType =  Exec_type.LOGGER;
            }
            else {
            	instance.execType =  Exec_type.UNKNOWN;
            }
            
            instance.currentArgs = args_String.split(" ");           
            return instance;
            
        } finally {
            sock.close();
        }
    }
    
    public static void sendTypeToOtherExec(String[] args) {
        try {
            Socket socket = new Socket(HOST, PORT);
            OutputStream os = socket.getOutputStream();
            
            try {
                PrintWriter pw = new PrintWriter(os, true);
                pw.println(getExecutableType().toString());
                
                String argsSend = "";
                
                for (String arg: args) {
                	argsSend += arg + " ";
                }
                
                pw.println(argsSend);
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
