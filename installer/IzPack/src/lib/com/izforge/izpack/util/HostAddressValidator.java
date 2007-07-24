/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Thorsten Kamann
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
import com.izforge.izpack.panels.Validator;

/**
 * A validator to check wheter a host:port is available (free).
 * 
 * This validator can be used for rule input fields in the UserInputPanel to make sure that the port
 * the user entered is not in use.
 * 
 * @author thorque
 */
public class HostAddressValidator implements Validator
{

    public boolean validate(ProcessingClient client)
    {
        InetAddress inet = null;
        String host = "";
        int port = 0;
        boolean retValue = false;

        try
        {
            host = client.getFieldContents(0);
            port = Integer.parseInt(client.getFieldContents(1));
        }
        catch (Exception e)
        {
            return false;
        }

        try
        {
            inet = InetAddress.getByName(host);
            ServerSocket socket = new ServerSocket(port, 0, inet);
            retValue = socket.getLocalPort() > 0;
            socket.close();
        }
        catch (Exception ex)
        {
            retValue = false;
        }
        return retValue;
    }

}
