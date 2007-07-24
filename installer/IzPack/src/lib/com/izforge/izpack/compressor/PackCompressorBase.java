/*
 * $Id: PackCompressorBase.java 1816 2007-04-23 19:57:27Z jponge $
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
package com.izforge.izpack.compressor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import com.izforge.izpack.compiler.Compiler;


/**
 * IzPack will be able to support different compression methods for the
 * packs included in the installation jar file.
 * This abstract class implements the interface PackCompressor for
 * the common needed methods.
 * 
 * @author Klaus Bartz
 */

public abstract class PackCompressorBase implements PackCompressor
{

    protected String [] formatNames = null;
    protected String [] containerPaths = null;
    protected String decoderMapper = null;
    /**
     * Should contain all full qualified (use dots, not slashes)
     * names of the class files. Regex will be suported in the
     * manner of <code>String.match</code>. <br>
     * Example:
     * <pre>"org.apache.tools.bzip2.CBZip2InputStream.*"</pre>
     * Do not forget the dot before the asterix.
     * For an other example see class BZip2PackCompressor.
     */
    protected String [][] decoderClassNames = null;
    protected String encoderClassName = null;
    
    protected Class [] paramsClasses = null;

    private Compiler compiler;
    private Constructor constructor;
    private int level = -1;
   /**
     * 
     */
    public PackCompressorBase()
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getContainerPath()
     */
    public String[] getContainerPaths()
    {
        return(containerPaths);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getEncoderClassName()
     */
    public String getEncoderClassName()
    {
        return(encoderClassName);
    }
    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getDecoderClassNames()
     */
    public String[][] getDecoderClassNames()
    {
        return(decoderClassNames);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#useStandardCompression()
     */
    public boolean useStandardCompression()
    {
        return( false );
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getCompressionFormatSymbols()
     */
    public String[] getCompressionFormatSymbols()
    {
        return(formatNames);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getDecoderMapperName()
     */
    public String getDecoderMapperName()
    {
        return(decoderMapper);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#setCompiler(com.izforge.izpack.compiler.Compiler)
     */
    public void setCompiler(Compiler compiler)
    {
        this.compiler = compiler;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#setCompressionLevel(int)
     */
    public void setCompressionLevel(int level)
    {
        this.level =  level;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getCompressionLevel()
     */
    public int getCompressionLevel()
    {
        return( level);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#needsBufferedOutputStream()
     */
    public boolean needsBufferedOutputStream()
    {
        return(true);
    }


    /**
     * Loads the given class from the previos setted container paths.
     * @param className full qualified name of the class to be loaded
     * @throws Exception
     */
    public void loadClass( String className) throws Exception
    {
        if( getEncoderClassName() == null)
            return;
        Class encoder = null;
        if( getContainerPaths() == null  )
        {   // May be class files are in the compiler.jar.
            encoder = Class.forName(className);
        }
        if( encoder == null)
        {
            String [] rawPaths = getContainerPaths();
            URL [] uRLs = new URL[rawPaths.length];
            Object instance = null;
            int i;
            int j = 0;

            for(i = 0; i < rawPaths.length; ++i)
            {
                if( rawPaths[i] == null )
                    continue;
                String jarPath = compiler.replaceProperties(rawPaths[i]);
                URL url = compiler.findIzPackResource(jarPath, "Pack compressor jar file");
                if (url != null)
                {
                    uRLs[j++] = url;
                    if (getClass().getResource("/" + jarPath) != null)
                    { // Oops, standalone, URLClassLoader will not work ...
                        // Write the jar to a temp file.
                        InputStream in = null;
                        FileOutputStream outFile = null;
                        byte[] buffer = new byte[5120];
                        File tf = null;
                        try
                        {
                            tf = File.createTempFile("izpj", ".jar");
                            tf.deleteOnExit();
                            outFile = new FileOutputStream(tf);
                            in = getClass().getResourceAsStream("/" + jarPath);
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            while ((bytesInBuffer = in.read(buffer)) != -1)
                            {
                                outFile.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                        }
                        finally
                        {
                            if (in != null) in.close();
                            if (outFile != null) outFile.close();
                        }
                        url = tf.toURL();
        
                    }
                }
            }
            if( j > 0 )
            {
                if( j < uRLs.length)
                {
                    URL [] nurl = new URL[j];
                    for( i = 0; i < j; ++i)
                        nurl[i] = uRLs[i];
                    uRLs = nurl;
                }
                // Use the class loader of the interface as parent, else
                // compile will fail at using it via an Ant task.
                URLClassLoader ucl = new URLClassLoader(uRLs, PackCompressor.class
                        .getClassLoader());
                encoder = ucl.loadClass(className);
            }
        }

        if (encoder != null)
        {
            // Be aware, paramsClasses should be defined earlier! For
            // default in the constructor of this class.
            constructor = encoder.getDeclaredConstructor(paramsClasses);
        }
        else
            compiler.parseError( "Cannot find defined compressor " + className);
    }
    
    /**
     * Returns a newly created instance of the output stream which should be
     * used by this pack compressor. This method do not declare the 
     * return value as FilterOutputStream although there must be an constructor
     * with a slave output stream as argument. This is done in this way because
     * some encoding streams from third party are only implemented as 
     * "normal" output stream.
     * @param slave output stream to be used as slave
     * @return a newly created instance of the output stream which should be
     * used by this pack compressor
     * @throws Exception
     */
    protected OutputStream getOutputInstance(OutputStream slave) 
        throws Exception
    {
        if( needsBufferedOutputStream())
        {
            slave = new BufferedOutputStream( slave);
        }
        Object [] params = resolveConstructorParams( slave );
        if( constructor == null )
            loadClass(getEncoderClassName());
        if( constructor == null )
            return(null);
        Object instance = null;
        instance = constructor.newInstance( params);
        if (!OutputStream.class.isInstance(instance))
            compiler.parseError( "'" + getEncoderClassName() + "' must be derived from "
                    + OutputStream.class.toString());
        return((OutputStream) instance );
        
    }
    
    /**
     * This method will be used to support different constructor signatures.
     * The default is 
     * <pre>XXXOutputStream( OutputStream slave )</pre>
     * if level is -1 or
     * <pre>XXXOutputStream( OutputStream slave, int level )</pre>
     * if level is other than -1.<br>
     * If the signature of the used output stream will be other, overload
     * this method in the derived pack compressor class.
     * @param slave output stream to be used as slave
     * @return the constructor params as Object [] to be used as construction
     * of the constructor via reflection
     * @throws Exception
     */
    protected Object[] resolveConstructorParams( OutputStream slave) throws Exception
    {
        if( level == -1 )
        {
            paramsClasses = new Class[1];
            paramsClasses[0] = Class.forName("java.io.OutputStream");
            Object[] params = { slave};
            return( params );
        }
        paramsClasses = new Class[2];
        paramsClasses[0] = Class.forName("java.io.OutputStream");
        paramsClasses[1] = java.lang.Integer.TYPE;
        Object[] params = { slave, new Integer(level)};
        return( params );
     }

}
