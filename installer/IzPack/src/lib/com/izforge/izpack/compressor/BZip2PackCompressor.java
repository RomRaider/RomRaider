/*
 * $Id: BZip2PackCompressor.java 1816 2007-04-23 19:57:27Z jponge $
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
 * This class implements the PackCompressor for the compression format "bzip2".
 * 
 * @author Klaus Bartz
 */
public class BZip2PackCompressor extends PackCompressorBase
{

    private static final String [] THIS_FORMAT_NAMES = {"bzip2"};
    private static final String [] THIS_CONTAINER_PATH = {"lib/ant.jar"};
    private static final String THIS_DECODER_MAPPER = "org.apache.tools.bzip2.CBZip2InputStream";
    private static final String [][] THIS_DECODER_CLASS_NAMES = 
        {{ "org.apache.tools.bzip2.BZip2Constants.*" ,
          "org.apache.tools.bzip2.CBZip2InputStream.*",
          "org.apache.tools.bzip2.CRC.*"
        }};
    private static final String THIS_ENCODER_CLASS_NAME = "org.apache.tools.bzip2.CBZip2OutputStream";

    /**
     * 
     */
    public BZip2PackCompressor()
    {
        super();
        formatNames = THIS_FORMAT_NAMES;
        containerPaths = THIS_CONTAINER_PATH;
        decoderMapper = THIS_DECODER_MAPPER;
        decoderClassNames = THIS_DECODER_CLASS_NAMES;
        encoderClassName = THIS_ENCODER_CLASS_NAME;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getOutputStream(java.io.OutputStream)
     */
    public OutputStream getOutputStream(OutputStream os) throws Exception
    {
        // TODO Auto-generated method stub
        
        return( getOutputInstance(os));
    }

}
