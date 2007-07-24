/*
 * $Id: DefaultPackCompressor.java 1816 2007-04-23 19:57:27Z jponge $
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

import java.io.OutputStream;



/**
 * IzPack will be able to support different compression methods for the
 * packs included in the installation jar file.
 * This class implements the PackCompressor for the compression format "default"
 * which is current the LZ77 implementation of zlib (also known as
 * zip or deflate).
 * 
 * @author Klaus Bartz
 */
public class DefaultPackCompressor extends PackCompressorBase
{
    private static final String [] THIS_FORMAT_NAMES = 
    {"default", "deflate", "zip", "lz77"};
    private static final String [] THIS_CONTAINER_PATH = null;
    private static final String THIS_DECODER_MAPPER = null;
    private static final String [][] THIS_DECODER_CLASS_NAMES = null;
    private static final String THIS_ENCODER_CLASS_NAME = null;


    /**
     * 
     */
    public DefaultPackCompressor()
    {
        super();
        formatNames = THIS_FORMAT_NAMES;
        containerPaths = THIS_CONTAINER_PATH;
        decoderMapper = THIS_DECODER_MAPPER;
        encoderClassName = THIS_ENCODER_CLASS_NAME;
        decoderClassNames = THIS_DECODER_CLASS_NAMES;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getOutputStream(java.io.OutputStream)
     */
    public OutputStream getOutputStream(OutputStream os)
    {
        // This will crash the packager if implementation is wrong :-)
        return(null);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#useStandardCompression()
     */
    public boolean useStandardCompression()
    {
        return(true);
    }
    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#needsBufferedOutputStream()
     */
    public boolean needsBufferedOutputStream()
    {
        return(false);
    }

}
