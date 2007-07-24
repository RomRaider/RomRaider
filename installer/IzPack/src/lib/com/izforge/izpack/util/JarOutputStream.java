/*
 * $Id: JarOutputStream.java 1816 2007-04-23 19:57:27Z jponge $
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
//import java.util.zip.ZipException;

//The declarations for ZipOutputStreams will be done
//as full qualified to clear at the use point that
//we do not use the standard class else the extended
//from apache.
//import org.apache.tools.zip.ZipOutputStream;
//import org.apache.tools.zip.ZipEntry;

/**
 * IzPack will be able to support different compression methods for the
 * packs included in the installation jar file.
 * For this a jar output stream will be needed with which the info
 * data (size, CRC) can be written after the compressed data.
 * This is not possible with the standard class
 * java.util.jar.JarOutputStream. Therefore we create an own class
 * which supports it. Really the hole work will be delegated to the
 * ZipOutputStream from the apache team which solves the problem.
 * 
 * @author Klaus Bartz
 */
public class JarOutputStream extends org.apache.tools.zip.ZipOutputStream
{
    private static final int JAR_MAGIC = 0xCAFE;
    private boolean firstEntry = true;
    private boolean preventClose = false;

    /**
     * Creates a new <code>JarOutputStream</code> with no manifest.
     * Using this constructor it will be NOT possible to write
     * data with compression format STORED to the stream without
     * declare the info data (size, CRC) at <code>putNextEntry</code>.
     * @param out the actual output stream
     * @exception IOException if an I/O error has occurred
     */
    public JarOutputStream(OutputStream out) throws IOException 
    {
        super(out);
    }

    /**
     * Creates a new <code>JarOutputStream</code> with the specified
     * <code>Manifest</code>. The manifest is written as the first
     * entry to the output stream which will be created from the 
     * file argument.
     *
     * @param fout the file object with which the output stream
     * should be created
     * @param man the <code>Manifest</code>
     * @exception IOException if an I/O error has occurred
     */
    public JarOutputStream(File  fout, Manifest man) throws IOException 
    {
        super( fout );
        if (man == null)
        {
            throw new NullPointerException("man");
        }
        org.apache.tools.zip.ZipEntry e = 
            new org.apache.tools.zip.ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(e);
        man.write(new BufferedOutputStream(this));
        closeEntry();
    }
    /**
     * Creates a new <code>JarOutputStream</code> with no manifest.
     * Will use random access if possible.
     * @param arg0 the file object with which the output stream
     * should be created
     * @throws java.io.IOException
     */
    public JarOutputStream(File arg0) throws IOException
    {
        super(arg0);
    }
    /**
     * Begins writing a new JAR file entry and positions the stream
     * to the start of the entry data. This method will also close
     * any previous entry. The default compression method will be
     * used if no compression method was specified for the entry.
     * The current time will be used if the entry has no set modification
     * time.
     *
     * @param ze the ZIP/JAR entry to be written
     * @exception java.util.zip.ZipException if a ZIP error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void putNextEntry(org.apache.tools.zip.ZipEntry ze) throws IOException 
    {
        if (firstEntry) 
        {
            // Make sure that extra field data for first JAR
            // entry includes JAR magic number id.
            byte[] edata = ze.getExtra();
            if (edata != null && !hasMagic(edata)) 
            {
                // Prepend magic to existing extra data
                byte[] tmp = new byte[edata.length + 4];
                System.arraycopy(tmp, 4, edata, 0, edata.length);
                edata = tmp;
            } 
            else 
            {
                edata = new byte[4];
            }
            set16(edata, 0, JAR_MAGIC); // extra field id
            set16(edata, 2, 0);         // extra field size
            ze.setExtra(edata);
            firstEntry = false;
        }
        super.putNextEntry(ze);
    }

    /**
     * @return Returns the preventClose.
     */
    public boolean isPreventClose()
    {
        return preventClose;
    }
    /**
     * Determine whether a call of the close method
     * will be performed or not. This is a hack for
     * FilterOutputStreams like the CBZip2OutputStream
     * of apache which calls close of the slave via
     * the final method which will be called from
     * the garbage collector.
     * @param preventClose The preventClose to set.
     */
    public void setPreventClose(boolean preventClose)
    {
        this.preventClose = preventClose;
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with the stream if isPreventClose is not true.
     * Else nothing will be done. This is a hack for 
     * FilterOutputStreams like the CBZip2OutputStream which 
     * calls the close method of the slave at finalizing the class
     * may be triggert by the GC.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException
    {
        if( ! isPreventClose() )
            super.close();
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with the stream also isPreventClose is true.
     * This is a hack for FilterOutputStreams like the CBZip2OutputStream which 
     * calls the close method of the slave at finalizing the class
     * may be triggert by the GC.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void closeAlways() throws IOException
    {
        setPreventClose( false );
        close();
    }

    /*
     * Returns true if specified byte array contains the
     * jar magic extra field id.
     */
    private static boolean hasMagic(byte[] edata) 
    {   
         
		try 
		{
			int i = 0;
			while (i < edata.length) 
			{
				if (get16(edata, i) == JAR_MAGIC) 
				{
					return true;
				}
				i += get16(edata, i + 2) + 4;
			}
		} 
		catch (ArrayIndexOutOfBoundsException e) 
		{
			// Invalid extra field data
		}
		return false;
    }

    /*
     * Fetches unsigned 16-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    private static int get16(byte[] b, int off) 
    {
    	return (b[off] & 0xff) | ((b[off+1] & 0xff) << 8);
    }

    /*
     * Sets 16-bit value at specified offset. The bytes are assumed to
     * be in Intel (little-endian) byte order.
     */
    private static void set16(byte[] b, int off, int value) 
    {
		b[off] = (byte)value;
		b[off+1] = (byte)(value >> 8);
    }

}
