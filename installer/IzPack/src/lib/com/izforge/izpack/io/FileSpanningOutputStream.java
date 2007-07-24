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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.izforge.izpack.util.Debug;

/**
 * An outputstream which transparently spans over multiple volumes. The size of the volumes and an
 * additonal space for the first volume can be specified.
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class FileSpanningOutputStream extends OutputStream
{

    public static final long KB = 1024;

    public static final long MB = 1024 * KB;

    // the default size of a volume
    public static final long DEFAULT_VOLUME_SIZE = 650 * MB;

    // free space on first volume
    // may be used for placing additional files on cd beside the pack files
    // default is 0, so there's no additional space
    public static final long DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE = 0;

    // the default volume name
    protected static final String DEFAULT_VOLUME_NAME = "installer";

    protected static final long FILE_NOT_AVAILABLE = -1;

    // the maximum size of a volume
    protected long maxvolumesize = DEFAULT_VOLUME_SIZE;

    // the addition free space of volume 0
    protected long firstvolumefreespacesize = DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE;
    
    public static final String VOLUMES_INFO = "/volumes.info";  

    // the current file this stream writes to
    protected File currentfile;

    // the name of the volumes
    protected String volumename;

    // the current index of the volume, the stream writes to
    protected int currentvolumeindex;

    // a normal file outputstream for writting to the current volume
    private FileOutputStream fileoutputstream;

    private GZIPOutputStream zippedoutputstream;

    // the current position in the open file
    protected long filepointer;

    protected long totalbytesofpreviousvolumes;

    /**
     * Creates a new spanning output stream with specified volume names and a maximum volume size
     * 
     * @param volumename - the name of the volumes
     * @param maxvolumesize - the maximum volume size
     * @throws IOException
     */
    public FileSpanningOutputStream(String volumename, long maxvolumesize) throws IOException
    {
        this(new File(volumename), maxvolumesize);
    }

    /**
     * Creates a new spanning output stream with specified volume names and a maximum volume size
     * 
     * @param volume - the first volume
     * @param maxvolumesize - the maximum volume size
     * @throws IOException
     */
    public FileSpanningOutputStream(File volume, long maxvolumesize) throws IOException
    {
        this(volume, maxvolumesize, 0);
    }

    /**
     * Creates a new spanning output stream with specified volume names and a maximum volume size
     * 
     * @param volume - the first volume
     * @param maxvolumesize - the maximum volume size
     * @param currentvolume - the current volume
     * @throws IOException
     */
    protected FileSpanningOutputStream(File volume, long maxvolumesize, int currentvolume)
            throws IOException
    {
        this.createVolumeOutputStream(volume, maxvolumesize, currentvolume);
    }

    /**
     * Actually creates the outputstream for writing a volume with index currentvolume and a maximum
     * of maxvolumesize
     * 
     * @param volume - the volume to write to
     * @param maxvolumesize - the maximum volume size
     * @param currentvolume - the currentvolume index
     * @throws IOException
     */
    private void createVolumeOutputStream(File volume, long maxvolumesize, int currentvolume)
            throws IOException
    {
        fileoutputstream = new FileOutputStream(volume);
        zippedoutputstream = new GZIPOutputStream(fileoutputstream, 256);
        currentfile = volume;
        this.currentvolumeindex = currentvolume;
        this.maxvolumesize = maxvolumesize;
        // try to get the volumename from the given volume file
        // the first volume has no suffix, additional volumes have a .INDEX# suffix
        String volumesuffix = "." + currentvolume;
        String volabsolutePath = volume.getAbsolutePath();
        if (volabsolutePath.endsWith(volumesuffix))
        {
            volumename = volabsolutePath.substring(0, volabsolutePath.indexOf(volumesuffix));
        }
        else
        {
            volumename = volabsolutePath;
        }
    }

    /**
     * 
     * @param volume
     * @throws IOException
     */
    public FileSpanningOutputStream(File volume) throws IOException
    {
        this(volume.getAbsolutePath(), DEFAULT_VOLUME_SIZE);
    }

    /**
     * 
     * @param volumename
     * @throws IOException
     */
    public FileSpanningOutputStream(String volumename) throws IOException
    {
        this(volumename, DEFAULT_VOLUME_SIZE);
    }

    /**
     * 
     * @throws IOException
     */
    public FileSpanningOutputStream() throws IOException
    {
        this(DEFAULT_VOLUME_NAME, DEFAULT_VOLUME_SIZE);
    }

    /**
     * Returns the size of the current volume
     * 
     * @return the size of the current volume FILE_NOT_AVAILABLE, if there's no current volume
     */
    protected long getCurrentVolumeSize()
    {
        if (currentfile == null) { return FILE_NOT_AVAILABLE; }
        try
        {
            flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        // create a new instance
        currentfile = new File(currentfile.getAbsolutePath());
        if (currentvolumeindex == 0)
        {
            // this is the first volume, add the additional free space
            // and add a reserve for overhead and not yet written data
            return currentfile.length() + this.firstvolumefreespacesize
                    + Math.round(0.001 * currentfile.length());
        }
        else
        {
            // not the first volume, just return the actual length
            // and add a reserve for overhead and not yet written data
            return currentfile.length() + Math.round(0.001 * currentfile.length());
        }
    }

    /**
     * Closes the stream to the current volume and reopens to the next volume
     * 
     * @throws IOException
     */
    protected void createStreamToNextVolume() throws IOException
    {
        // close current stream
        close();
        totalbytesofpreviousvolumes = currentfile.length();
        currentvolumeindex++;
        // get the name of the next volume
        String nextvolumename = volumename + "." + currentvolumeindex;
        // does the creation
        this.createVolumeOutputStream(new File(nextvolumename), this.maxvolumesize,
                this.currentvolumeindex);
    }

    /**
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException
    {
        this.flush();
        zippedoutputstream.close();
        fileoutputstream.close();
        // reset the filepointer
        // filepointer = 0;
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (len > maxvolumesize) { throw new IOException(
                "file can't be written. buffer length exceeded maxvolumesize (" + " > "
                        + maxvolumesize + ")"); }
        // get the current size of this file
        long currentsize = getCurrentVolumeSize();
        // calculate the available bytes
        long available = maxvolumesize - currentsize;

        if (available < len)
        {
            Debug.trace("Not enough space left on volume. available: " + available);
            Debug.trace("current size is: " + currentsize);
            // there's not enough space available
            // create the next volume
            this.createStreamToNextVolume();
        }
        // enough space available, just write to the outputstream
        zippedoutputstream.write(b, off, len);
        // increase filepointer by written bytes
        filepointer += len;
    }

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException
    {
        this.write(b, 0, b.length);
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException
    {
        long availablebytes = maxvolumesize - getCurrentVolumeSize();
        if (availablebytes >= 1)
        {
            zippedoutputstream.write(b);
            // increase filepointer by written byte
            filepointer++;
        }
        else
        {
            // create next volume
            this.createStreamToNextVolume();
            zippedoutputstream.write(b);
            // increase filepointer by written byte
            filepointer++;
        }
    }

    /**
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException
    {
        zippedoutputstream.flush();
        fileoutputstream.flush();
    }

    /**
     * Returns the amount of currently created volumes
     * 
     * @return the amount of created volumes
     */
    public int getVolumeCount()
    {
        return this.currentvolumeindex + 1;
    }

    /**
     * 
     * @return
     */
    public long getFirstvolumefreespacesize()
    {
        return firstvolumefreespacesize;
    }

    /**
     * 
     * @param firstvolumefreespacesize
     */
    public void setFirstvolumefreespacesize(long firstvolumefreespacesize)
    {
        this.firstvolumefreespacesize = firstvolumefreespacesize;
    }

    /**
     * Returns the current position in this file
     * 
     * @return the position in this file
     * @throws IOException
     */
    public long getCompressedFilepointer() throws IOException
    {
        this.flush();
        // return filepointer;
        return totalbytesofpreviousvolumes + currentfile.length();
    }

    public long getFilepointer()
    {
        return filepointer;
    }
}
