/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2007 Dennis Reil
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.izforge.izpack.util.Debug;

/**
 * An inputstream which transparently spans over multiple volumes. The amount of volumes has to be
 * specified
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class FileSpanningInputStream extends InputStream
{

    private static final int EOF = -1;

    protected FileInputStream fileinputstream;

    protected String volumename;

    protected int currentvolumeindex;

    protected int volumestotal;

    protected static boolean nextvolumenotfound = false;

    protected long filepointer;

    protected GZIPInputStream zippedinputstream;

    public FileSpanningInputStream(File volume, int volumestotal) throws IOException
    {
        fileinputstream = new FileInputStream(volume);
        zippedinputstream = new GZIPInputStream(fileinputstream);
        currentvolumeindex = 0;
        volumename = volume.getAbsolutePath();
        this.volumestotal = volumestotal;
        filepointer = 0;
        Debug.trace("Opening stream to " + volume);
    }

    public FileSpanningInputStream(String volumename, int volumestotal) throws IOException
    {
        this(new File(volumename), volumestotal);
    }

    /**
     * creates an inputstream to the next volume
     * 
     * @return true - an inputstream to the next volume has been created false - the last volume was
     * reached
     * @throws IOException
     */
    private boolean createInputStreamToNextVolume() throws IOException
    {
        currentvolumeindex++;
        // have we reached the last volume?
        if (currentvolumeindex >= volumestotal)
        {
            Debug.error("last volume reached.");
            return false;
        }
        // the next volume name
        String nextvolumename = volumename + "." + currentvolumeindex;
        Debug.trace("Trying to use next volume: " + nextvolumename);
        File nextvolumefile = new File(nextvolumename);
        if (!nextvolumefile.exists())
        {
            currentvolumeindex--;
            nextvolumenotfound = true;
            Debug.trace("volume not found");
            throw new VolumeNotFoundException(nextvolumename + "was not found.", nextvolumename);
        }
        Debug.trace("next volume found.");
        // try to open new stream to next volume
        fileinputstream = new FileInputStream(nextvolumefile);
        zippedinputstream = new GZIPInputStream(fileinputstream);
        // everything fine
        nextvolumenotfound = false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException
    {
        if (nextvolumenotfound)
        {
            createInputStreamToNextVolume();
        }
        // return fileinputstream.available();
        return zippedinputstream.available();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException
    {
        zippedinputstream.close();
        fileinputstream.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException
    {
        if (nextvolumenotfound)
        {
            // the next volume was not found, so try to create a new input stream to next volume
            createInputStreamToNextVolume();
        }
        int nextbyte = zippedinputstream.read();
        filepointer++;
        if (nextbyte == EOF)
        {
            // if end of file is reached, try to open InputStream to next volume
            // close the inputstream
            try
            {
                zippedinputstream.close();
            }
            catch (Exception e)
            {
                // do nothing
            }

            if (createInputStreamToNextVolume())
            {
                // try to read next byte
                nextbyte = zippedinputstream.read();
                filepointer++;
            }
        }
        return nextbyte;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (nextvolumenotfound)
        {
            // the next volume was not found, so try to create a new input stream to next volume
            createInputStreamToNextVolume();
        }
        int bytesread = zippedinputstream.read(b, off, len);
        filepointer += bytesread;
        if (bytesread == EOF)
        {
            filepointer++; // bytesread was -1;
            System.out.println("EOF reached.");
            // close the inputstream
            try
            {
                zippedinputstream.close();
            }
            catch (Exception e)
            {
                // do nothing
            }
            // try to open next volume
            if (createInputStreamToNextVolume())
            {
                // try to read next bytes
                Debug.trace("next volume opened, continuing read");
                bytesread = zippedinputstream.read(b, off, len);
                filepointer += bytesread;
                // System.out.println("read into buffer: " + bytesread + " Bytes");
            }
        }
        // System.out.println("return from read into buffer: " + bytesread + " Bytes");
        return bytesread;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException
    {
        return this.read(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long n) throws IOException
    {
        if (nextvolumenotfound)
        {
            // the next volume was not found, so try to create a new input stream to next volume
            createInputStreamToNextVolume();
        }
        long bytesskipped = 0;
        byte[] buffer = new byte[4096];
        try
        {
            while (bytesskipped < n)
            {
                int maxBytes = (int) Math.min(n - bytesskipped, buffer.length);

                int bytesInBuffer = this.read(buffer, 0, maxBytes);
                if (bytesInBuffer == -1)
                    throw new IOException("Unexpected end of stream (installer corrupted?)");

                bytesskipped += bytesInBuffer;
            }
        }
        catch (VolumeNotFoundException vnfe)
        {
            vnfe.setAlreadyskippedbytes(bytesskipped);
            throw vnfe;
        }
        return bytesskipped;
    }

    /**
     * Returns the name of the volume
     * 
     * @return the name of the volume
     */
    public String getVolumename()
    {
        return volumename;
    }

    /**
     * Sets the volumename
     * 
     * @param volumename
     */
    public void setVolumename(String volumename)
    {
        Debug.trace("new volumename: " + volumename);
        // try to get the volumename from the given volume file
        // the first volume has no suffix, additional volumes have a .INDEX# suffix
        String volumesuffix = "." + currentvolumeindex;
        String nextvolumesuffix = "." + (currentvolumeindex + 1);
        if (volumename.endsWith(volumesuffix))
        {
            this.volumename = volumename.substring(0, volumename.lastIndexOf(volumesuffix));
        }
        else if (volumename.endsWith(nextvolumesuffix))
        {
            this.volumename = volumename.substring(0, volumename.lastIndexOf(nextvolumesuffix));
        }
        else
        {
            this.volumename = volumename;
        }
        Debug.trace("Set volumename to: " + this.volumename);
    }

    /**
     * Returns the current position in the file. Notice: this is the global position in all volumes.
     * 
     * @return the current position in file.
     */
    public long getFilepointer()
    {
        return filepointer;
    }

}
