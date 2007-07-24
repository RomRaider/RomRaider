/*
 * $Id: PackCompressor.java 1816 2007-04-23 19:57:27Z jponge $
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

import com.izforge.izpack.compiler.Compiler;


/**
 * IzPack will be able to support different compression methods for the
 * packs included in the installation jar file.
 * This interface declares the handler of one compression format.
 * 
 * @author Klaus Bartz
 */

public interface PackCompressor
{

    /**
     * Returns a newly created output stream which write method
     * writes the given input encoded to the defined output stream.
     * Attention! This method will be returned a valid output stream
     * only if it is used in the IzPack compiler, or if this pack compressor
     * needs no external classes. A call in the
     * installation should be throw if external classes are used.
     * The implementation should load the needed classes via reflection
     * because classes are not present in the installation.
     * @param os output stream to be used as listener
     * @return a newly created encoding output stream 
     * @throws Exception
     */
    OutputStream getOutputStream(OutputStream os) throws Exception;
    
    /**
     * Returns all symbolic names which are used for this compressor.
     * @return all symbolic names which are used for this compressor
     */
    String []getCompressionFormatSymbols();
    
    /**
     * Returns the path where the compiler can find the classes;
     * normaly this is a path to a jar file.
     * If no additional classes are needed, this method should return null.
     * @return the path where the compiler can find the classes
     */
    String[] getContainerPaths();
    
    /**
     * Returns the qualified names of all needed classes for decoding.
     * All class files should be placed in the container which will
     * be referred by the method getContainerPath.
     * If no additional classes are needed, this method should return null.
     * @return qualified names of all needed classes for decoding
     */
    String[][] getDecoderClassNames();
 
    /**
     * Returns the qualified name of the encoding output stream.
     * The class file should be placed in the container which will
     * be referred by the method getContainerPath.
     * @return qualified name of the encoding output stream
     */
    String getEncoderClassName();
 
    /**
     * Returns the qualified name of the class which should be used
     * as InputStream in the installer. This class mapps the "real"
     * decoder or - if useable - the decoder name will be returned self.
     * If useStandardCompression is true, this method returns null.
     * @return the qualified name of the class which should be used
     * as InputStream in the installer
     */
    String getDecoderMapperName();
    /**
     * Returns whether the standard comression should be used with
     * this pack compressor or not. If this method returns true,
     * the returns values of the methods getContainerPath and 
     * getDecoderClassNames are not valid (should be null).
     * @return whether the standard comression should be used or not
     */
    boolean useStandardCompression();

    /**
     * Receives the current used compiler.
     * Needed at loading encoder classes and error handling.
     * @param compiler current active compiler
     */
    void setCompiler(Compiler compiler);
    
    /**
     * Returns whether a buffered output stream should be used
     * intermediate between the output stream of this compressor
     * and the destination.
     * @return wether a buffered output stream should be used
     * intermediate between the output stream of this compressor
     * and the destination.
     */
    boolean needsBufferedOutputStream();
    
    /**
     * Receives the compression level to be used.
     * @param level compression level to be used
     */
    void setCompressionLevel(int level);
    
    /**
     * Returns the compression level to be used.
     * @return the compression level to be used
     */
    int getCompressionLevel();
    
    
    
}
