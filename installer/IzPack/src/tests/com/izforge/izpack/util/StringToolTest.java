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

package com.izforge.izpack.util;

import junit.framework.TestCase;

public class StringToolTest extends TestCase
{

    /*
     * Class under test for String replace(String, String, String[, boolean])
     */
    public void testReplace()
    {
        String ref = "ABC-012-def";

        TestCase.assertEquals(null, StringTool.replace(null, null, null));
        TestCase.assertEquals("ABC-012-def", StringTool.replace(ref, null, null));
        TestCase.assertEquals("ABC-012-def", StringTool.replace(ref, "something", null));
        TestCase.assertEquals("ABC012def", StringTool.replace(ref, "-", null));
        TestCase.assertEquals("abc-012-def", StringTool.replace(ref, "ABC", "abc"));
        TestCase.assertEquals("ABC-012-def", StringTool.replace(ref, "abc", "abc", false));
        TestCase.assertEquals("ABC-012-def", StringTool.replace(ref, "abc", "abc", true));
    }

    /*
     * Class under test for String normalizePath(String[, String])
     */
    public void testNormalizePath()
    {
        TestCase.assertEquals("C:\\Foo\\Bar\\is\\so\\boring;plop;plop", StringTool.normalizePath(
                "C:\\Foo/Bar/is\\so\\boring:plop;plop", "\\"));
        TestCase.assertEquals("/some/where/that:matters:really", StringTool.normalizePath(
                "/some/where\\that:matters;really", "/"));
    }

}
