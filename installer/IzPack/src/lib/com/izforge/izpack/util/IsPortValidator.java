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

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

/**
 * A validator to check whether the field content is a port .
 * 
 * @author thorque
 */
public class IsPortValidator implements Validator
{

    public boolean validate(ProcessingClient client)
    {
        int port = 0;

        if ("".equals(client.getFieldContents(0))) { return false; }

        try
        {
            port = Integer.parseInt(client.getFieldContents(0));
            if (port > 0 && port < 32000) { return true; }
        }
        catch (Exception ex)
        {
            return false;
        }

        return false;
    }

}
