/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class ByteCountingOutputStreamTest extends TestCase
{

    public void testWriting() throws IOException
    {
        File temp = File.createTempFile("foo", "bar");
        FileOutputStream fout = new FileOutputStream(temp);
        ByteCountingOutputStream out = new ByteCountingOutputStream(fout);

        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        out.write(data);
        out.write(data, 3, 2);
        out.write(1024);
        out.close();

        TestCase.assertEquals(16, out.getByteCount());
    }

}
