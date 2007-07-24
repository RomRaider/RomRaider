/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2005 Marc Eppelmann
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Provides general global file utility methods
 *
 * @author marc.eppelmann
 */
public class FileUtil
{
  //~ Constructors ***********************************************************************

  /**
   * Creates a new FileUtil object.
   */
  public FileUtil(){}

  //~ Methods ****************************************************************************

  /** 
   * Gets the content from a File as StringArray List.
   *
   * @param fileName A file to read from.
   *
   * @return List of individual line of the specified file. List may be empty but not
   *         null.
   *
   * @throws IOException
   */
  public static ArrayList getFileContent( String fileName )
                                  throws IOException
  {
    ArrayList result = new ArrayList();

    File      aFile = new File( fileName );

    if( ! aFile.isFile() )
    {
      //throw new IOException( fileName + " is not a regular File" );
      return result; // None
    }

    BufferedReader reader = null;

    try
    {
      reader = new BufferedReader( new FileReader( aFile ) );
    }
    catch( FileNotFoundException e1 )
    {
      // TODO handle Exception
      e1.printStackTrace();

      return result;
    }

    String aLine = null;

    while( ( aLine = reader.readLine() ) != null )
    {
      result.add( aLine + "\n" );
    }

    reader.close();

    return result;
  }

  /** 
   * Searches case sensitively, and returns true if the given SearchString occurs in the
   * first File with the given Filename.
   *
   * @param aFileName A files name
   * @param aSearchString the string search for
   *
   * @return true if found in the file otherwise false
   */
  public static boolean fileContains( String aFileName, String aSearchString )
  {
    return ( fileContains( aFileName, aSearchString, false ) );
  }

  /** 
   * Tests if the given File contains the given Search String
   *
   * @param aFileName A files name
   * @param aSearchString the String to search for
   * @param caseInSensitiveSearch If false the Search is casesensitive
   *
   * @return true if found in the file otherwise false
   */
  public static boolean fileContains( String aFileName, String aSearchString,
                                      boolean caseInSensitiveSearch )
  {
    boolean result = false;

    String  searchString = caseInSensitiveSearch
            ? aSearchString.toLowerCase() : aSearchString;

    ArrayList fileContent = new ArrayList();

    try
    {
      fileContent = getFileContent( aFileName );
    }
    catch( IOException e )
    {
      // TODO handle Exception
      e.printStackTrace(  );
    }

    Iterator linesIter = fileContent.iterator(  );

    while( linesIter.hasNext() )
    {
      String currentline = (String) linesIter.next(  );

      if(caseInSensitiveSearch )
      {
        currentline = currentline.toLowerCase(  );
      }

      if( currentline.indexOf( searchString ) > -1 )
      {
        result = true;

        break;
      }
    }

    return result;
  }

  /** 
   * Test main
   *
   * @param args
   */
  public static void main( String[] args ){}
}
