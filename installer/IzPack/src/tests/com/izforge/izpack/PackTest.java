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

package com.izforge.izpack;

import junit.framework.TestCase;

public class PackTest extends TestCase
{

    public void testToByteUnitsString()
    {
        TestCase.assertEquals("5 bytes", Pack.toByteUnitsString(5));
        TestCase.assertEquals("1 KB", Pack.toByteUnitsString(1024));
        TestCase.assertEquals("2 KB", Pack.toByteUnitsString(2048));
        TestCase.assertEquals("1 MB", Pack.toByteUnitsString(1024 * 1024));
        TestCase.assertEquals("1 GB", Pack.toByteUnitsString(1024 * 1024 * 1024));
    }

}
