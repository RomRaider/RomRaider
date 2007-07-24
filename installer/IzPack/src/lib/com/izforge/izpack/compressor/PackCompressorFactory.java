/*
 * $Id: PackCompressorFactory.java 1816 2007-04-23 19:57:27Z jponge $
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

import java.util.HashMap;

import com.izforge.izpack.compiler.CompilerException;



/**
 * IzPack will be able to support different compression methods for the
 * packs included in the installation jar file.
 * This class is the factory which offers different "compressors" to
 * IzPack. It is made to mask the internal structure of each "compressor"
 * and gaves a common API for all supported compression methods to IzPack.
 * IzPacks compiler uses this class to get an encoder and the informations
 * which are needed to support the decompression in the installation. 
 * All "compressors" should use this class as API and should not be
 * included directly in the IzPack compiler.
 * 
 * @author Klaus Bartz
 */
public class PackCompressorFactory
{
    /** This map contains all registered "compressors".
     *  The keys are the symbolic names which are used for a particular
     *  compression format.
     */
    private static HashMap typeMap = new HashMap();
    private static CompilerException ShitHappens = null;
    
    static
    {   // Add the well known pack compressors to this factory
        catchedRegister(new RawPackCompressor());
        catchedRegister(new DefaultPackCompressor());
        catchedRegister(new BZip2PackCompressor());
    }
    
    /**
     * No object of this factory needed.
     */
    private PackCompressorFactory()
    {
        super();
    }
    
    
    /**
     * Register a particular pack compressor to this factory.
     * The used symbolic name will be handled case insensitive.
     * @param pc an instance of the pack compressor which describes 
     * encoder and decoder for a special compression format
     */
    public static void catchedRegister(PackCompressor pc) 
    {
        if( !good())
            return;
        try
        {
            register(pc);
        }
        catch (CompilerException e)
        {
            ShitHappens = e;
        }

    }
    
    /**
     * Register a particular pack compressor to this factory.
     * The used symbolic name will be handled case insensitive.
     * @param pc an instance of the pack compressor which describes 
     * encoder and decoder for a special compression format
     * @throws CompilerException if the symbol already exist or if
     * the compressor is not valid
     */
    public static void register(PackCompressor pc) 
        throws CompilerException
    {
        String [] syms = pc.getCompressionFormatSymbols();
        for(int i = 0; i < syms.length; ++i)
        {
            String sym = syms[i].toLowerCase();
            if( typeMap.containsKey(sym))
                throw new CompilerException("PackCompressor for symbol " 
                        + sym + " allready registered");
            typeMap.put(sym, pc);
            // TODO: add verify of PackCompressor.
        }
     }
    
    /**
     * Returns whether a compressor exists for the given symbolic
     * name or not.
     * @param type symbolic compression name to be tested
     * @return whether the given compression format will be supported
     * or not
     * @throws CompilerException
     */
    public static boolean isTypeSupported( String type ) throws CompilerException
    {
        if( ! good())
            throw(ShitHappens);
        type = type.toLowerCase();
        return( typeMap.containsKey(type));
    }

    /**
     * Returns a newly created pack compressor with the given
     * compression format.
     * @param type symbol name of compression format to be used
     * @return a newly created pack compressor 
     * @throws CompilerException if no encoder is registered for 
     * the chosen compression format
     */
    public static PackCompressor get( String type) 
        throws CompilerException
    {
        if( ! good())
            throw(ShitHappens);
        type = type.toLowerCase();
        if( ! typeMap.containsKey(type))
            throw new CompilerException( 
                "No PackCompressor registered for the given symbol " 
                + type + ".");
        return((PackCompressor) typeMap.get(type));
        
    }
    /**
     * Returns the exception which was thrown during
     * registering of a pack compressor.
     * @return the exception which was thrown during
     * registering of a pack compressor
     */
    public static CompilerException getRegisterException()
    {
        return ShitHappens;
    }
    /**
     * Sets an exception which was thrown during registering a pack compressor.
     * @param registerException The register exception to set.
     */
    public static void setRegisterException(CompilerException registerException)
    {
        ShitHappens = registerException;
    }
    
    public static boolean good()
    {
        return( ShitHappens == null );
    }
}
