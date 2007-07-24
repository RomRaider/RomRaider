/*
 * $Id: Packager.java 1671 2007-01-02 10:28:58Z dreil $
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2006 Dennis Reil
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

package com.izforge.izpack.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for packager classes
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class PackagerHelper
{    
    /**
     * Copies all the data from the specified input stream to the specified output stream.
     * 
     * @param in the input stream to read
     * @param out the output stream to write
     * @return the total number of bytes copied
     * @exception IOException if an I/O error occurs
     */
    public static long copyStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[5120];
        long bytesCopied = 0;
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, bytesInBuffer);
            bytesCopied += bytesInBuffer;
        }
        return bytesCopied;
    }
}
