/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Thorsten Kamman
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util;

import java.net.InetAddress;
import java.net.ServerSocket;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Processor;

/**
 * Checks whether the value of the field comtemt is a port and is free. If false the next free port
 * will be searched.
 * 
 * @author Thorsten Kamann <thorsten.kamann@planetes.de>
 */
public class PortProcessor implements Processor
{

    public String process(ProcessingClient client)
    {
        String retValue = "";
        String host = "localhost";
        int port = 0;
        int oPort = 0;
        boolean found = false;
        InetAddress inet = null;
        ServerSocket socket = null;

        try
        {
            if (client.getNumFields() > 1)
            {
                host = client.getFieldContents(0);
                oPort = Integer.parseInt(client.getFieldContents(1));
            }
            else
            {
                oPort = Integer.parseInt(client.getFieldContents(0));
            }
        }
        catch (Exception ex)
        {
            return getReturnValue(client, null, null);
        }

        port = oPort;
        while (!found)
        {
            try
            {
                inet = InetAddress.getByName(host);
                socket = new ServerSocket(port, 0, inet);
                if (socket.getLocalPort() > 0)
                {
                    found = true;
                    retValue = getReturnValue(client, null, String.valueOf(port));
                }
                else
                {
                    port++;
                }
            }
            catch (java.net.BindException ex)
            {
                port++;
            }
            catch (Exception ex)
            {
                return getReturnValue(client, null, null);
            }
            finally
            {
                try
                {
                    socket.close();
                }
                catch (Exception ex)
                {}
            }
        }
        return retValue;
    }

    /**
     * Creates the return value
     * 
     * @param client The ProcessingClient
     */
    private String getReturnValue(ProcessingClient client, String host, String port)
    {
        String retValue = "";
        String _host = "";
        String _port = "";

        if (client.getNumFields() > 1)
        {
            _host = (host == null) ? client.getFieldContents(0) : host;
            _port = (port == null) ? client.getFieldContents(1) : port;
            retValue = _host + "*" + _port;
        }
        else
        {
            _port = (port == null) ? client.getFieldContents(0) : port;
            retValue = _port;
        }

        return retValue;
    }
}
