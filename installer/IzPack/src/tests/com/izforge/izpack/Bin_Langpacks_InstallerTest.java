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

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * A JUnit TestCase to check completeness of the all the language packs
 * 
 * @author Hans Aikema
 *
 */
public class Bin_Langpacks_InstallerTest extends TestCase
{
    private final static String referencePack = "eng.xml";
    private final static String basePath= "." + File.separator + 
                                "bin" + File.separator +
                                "langpacks" + File.separator +
                                "installer" + File.separator;
    private static LocaleDatabase reference;
    private LocaleDatabase check;

    /**
     * Creates a new 'test all' installer langpack testcase
     * 
     * @throws Exception Forwarded Exception from LocaleDatabase constructor or FileNotFoundException on allocation of the reference languagepack
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     */
    public Bin_Langpacks_InstallerTest() throws Exception
    {
        this("");
    }
    
    /**
     * Creates a new 'single testmethod' installer langpack testcase
     * 
     * @throws Exception Forwarded Exception from LocaleDatabase constructor or FileNotFoundException on allocation of the reference languagepack
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public Bin_Langpacks_InstallerTest(String arg0) throws Exception
    {
        super(arg0);
        Bin_Langpacks_InstallerTest.reference = new LocaleDatabase(new FileInputStream(basePath + referencePack));
    }
    
    private void checkLangpack(String langpack) throws Exception{
        this.check = new LocaleDatabase(new FileInputStream(basePath + langpack));
        // all keys in the English langpack should be present in the foreign langpack
        for (Iterator i = reference.keySet().iterator();i.hasNext();) {
            // Locale Database uses the id strings as keys
            String id = (String) i.next();
            assertTrue("Missing translation for id:"+id,this.check.containsKey(id));
        }
        // there should be no keys in the foreign langpack which don't exist in the 
        // english langpack
        for (Iterator i = this.check.keySet().iterator();i.hasNext();) {
            // LocaleDatabase uses the id strings as keys
            String id = (String) i.next();
            assertTrue("Superfluous translation for id:"+id,reference.containsKey(id));
        }
    }
    /**
     * Checks the Catalan language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testCat() throws Exception{
        this.checkLangpack("cat.xml");
    }
    
    /**
     * Checks the Chinese language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
public void testChn() throws Exception{
        this.checkLangpack("chn.xml");
    }

/**
 * Checks the Czech language pack for missing / superfluous translations
 * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
 * 
 * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
 * @see java.io.FileInputStream#FileInputStream(java.lang.String)
 * @see java.util.TreeMap#containsKey(java.lang.Object)
 */
    public void testCze() throws Exception{
        this.checkLangpack("cze.xml");
    }
    
    /**
     * Checks the Danish language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
public void testDan() throws Exception{
        this.checkLangpack("dan.xml");
    }
/**
 * Checks the German language pack for missing / superfluous translations
 * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
 * 
 * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
 * @see java.io.FileInputStream#FileInputStream(java.lang.String)
 * @see java.util.TreeMap#containsKey(java.lang.Object)
 */
public void testDeu() throws Exception{
        this.checkLangpack("deu.xml");
    }
/**
 * Checks the Modern Greek language pack for missing / superfluous translations
 * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
 * 
 * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
 * @see java.io.FileInputStream#FileInputStream(java.lang.String)
 * @see java.util.TreeMap#containsKey(java.lang.Object)
 */
public void testEll() throws Exception{
        this.checkLangpack("ell.xml");
    }

/**
 * Checks the English language pack for missing / superfluous translations<br />
 * <em>This test should always succeed, since the english langpack is the reference pack)</em>
 * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
 * 
 * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
 * @see java.io.FileInputStream#FileInputStream(java.lang.String)
 * @see java.util.TreeMap#containsKey(java.lang.Object)
 */
public void testEng() throws Exception{
        this.checkLangpack("eng.xml");
    }
/**
 * Checks the Finnish language pack for missing / superfluous translations
 * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
 * 
 * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
 * @see java.io.FileInputStream#FileInputStream(java.lang.String)
 * @see java.util.TreeMap#containsKey(java.lang.Object)
 */
    public void testFin() throws Exception{
        this.checkLangpack("fin.xml");
    }
    /**
     * Checks the French language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testFra() throws Exception{
        this.checkLangpack("fra.xml");
    }
    /**
     * Checks the Hungarian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testHun() throws Exception{
        this.checkLangpack("hun.xml");
    }
    /**
     * Checks the Indonesian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testInd() throws Exception{
        this.checkLangpack("ind.xml");
    }
    /**
     * Checks the Italian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testIta() throws Exception{
        this.checkLangpack("ita.xml");
    }
    /**
     * Checks the Japanese language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testJpn() throws Exception{
        this.checkLangpack("jpn.xml");
    }
    /**
     * Checks the Korean language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testKor() throws Exception{
        this.checkLangpack("kor.xml");
    }
    /**
     * Checks the Malaysian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testMys() throws Exception{
        this.checkLangpack("mys.xml");
    }
    /**
     * Checks the Dutch language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testNed() throws Exception{
        this.checkLangpack("ned.xml");
    }
    /**
     * Checks the Norwegian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testNor() throws Exception{
        this.checkLangpack("nor.xml");
    }
    /**
     * Checks the Polish language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testPol() throws Exception{
        this.checkLangpack("pol.xml");
    }
    /**
     * Checks the Portugese language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testPor() throws Exception{
        this.checkLangpack("por.xml");
    }
    /**
     * Checks the Romanian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testRom() throws Exception{
        this.checkLangpack("rom.xml");
    }
    /**
     * Checks the Russian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testRus() throws Exception{
        this.checkLangpack("rus.xml");
    }
    /**
     * Checks the Serbia/Montenegro language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testScg() throws Exception{
        this.checkLangpack("scg.xml");
    }
    /**
     * Checks the Spanish language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testSpa() throws Exception{
        this.checkLangpack("spa.xml");
    }
    /**
     * Checks the Slovak language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testSvk() throws Exception{
        this.checkLangpack("svk.xml");
    }
    /**
     * Checks the Swedish language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
public void testSwe() throws Exception{
        this.checkLangpack("swe.xml");
    }
/**
 * Checks the Turkish language pack for missing / superfluous translations
 * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
 * 
 * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
 * @see java.io.FileInputStream#FileInputStream(java.lang.String)
 * @see java.util.TreeMap#containsKey(java.lang.Object)
 */
    public void testTur() throws Exception{
        this.checkLangpack("tur.xml");
    }
    /**
     * Checks the Ukranian language pack for missing / superfluous translations
     * @throws Exception Forwarded Exception for the LocaleDatabase contructor, FileInputStream constructor or TreeMap containsKey method
     * 
     * @see com.izforge.izpack.LocaleDatabase#LocaleDatabase(java.io.InputStream)
     * @see java.io.FileInputStream#FileInputStream(java.lang.String)
     * @see java.util.TreeMap#containsKey(java.lang.Object)
     */
    public void testUkr() throws Exception{
        this.checkLangpack("ukr.xml");
    }
}
