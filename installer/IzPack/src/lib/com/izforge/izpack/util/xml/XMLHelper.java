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
package com.izforge.izpack.util.xml;

import net.n3.nanoxml.XMLElement;


/**
 * A Collection of convenient XML-Helper Methods and Constants
 *
 * @author marc.eppelmann&#064;gmx.de
 * @version $Revision: 1.1 $
 */
public class XMLHelper
{
  //~ Static fields/initializers *********************************************************

  /** YES   = "YES" */
  public final static String YES   = "YES";

  /** NO = "NO" */
  public final static String NO = "NO";

  /** TRUE = "TRUE" */
  public final static String TRUE = "TRUE";

  /** FALSE = "FALSE" */
  public final static String FALSE = "FALSE";

  /** ON = "ON" */
  public final static String ON = "ON";

  /** OFF = "OFF" */
  public final static String OFF = "OFF";

  /** _1 = "1" */
  public final static String _1 = "1";

  /** _0 = "0" */
  public final static String _0 = "0";

  //~ Constructors ***********************************************************************

  /**
   * Creates a new XMLHelper object.
   */
  public XMLHelper(  )
  {
    super(  );
  }

  //~ Methods ****************************************************************************

  /** 
   * Determines if the named attribute in true. True is represented by any of the
   * following strings and is not case sensitive. <br>
   * 
   * <ul>
   * <li>
   * yes
   * </li>
   * <li>
   * 1
   * </li>
   * <li>
   * true
   * </li>
   * <li>
   * on
   * </li>
   * </ul>
   * 
   * <br> Every other string, including the empty string as well as the non-existence of
   * the attribute will cuase <code>false</code> to be returned.
   *
   * @param element the <code>XMLElement</code> to search for the attribute.
   * @param name the name of the attribute to test.
   *
   * @return <code>true</code> if the attribute value equals one of the pre-defined
   *         strings, <code>false</code> otherwise.
   */

  /*--------------------------------------------------------------------------*/
  public static boolean attributeIsTrue( XMLElement element, String name )
  {
    String value = element.getAttribute( name, "" ).toUpperCase(  );

    if( value.equals( YES ) )
    {
      return ( true );
    }
    else if( value.equals( TRUE ) )
    {
      return ( true );
    }
    else if( value.equals( ON ) )
    {
      return ( true );
    }
    else if( value.equals( _1 ) )
    {
      return ( true );
    }

    return ( false );
  }

  /** 
   * The Opposit of AttributeIsTrue()
   *
   * @param element the element to inspect
   * @param name the attribute to inspect
   *
   * @return returns true if name attribute of the given element contains &quot;false&quot;  
   */
  public static boolean attributeIsFalse( XMLElement element, String name )
  {
    String value = element.getAttribute( name, "" ).toUpperCase(  );

    if( value.equals( "NO" ) )
    {
      return ( true );
    }
    else if( value.equals( "FALSE" ) )
    {
      return ( true );
    }
    else if( value.equals( "OFF" ) )
    {
      return ( true );
    }
    else if( value.equals( "0" ) )
    {
      return ( true );
    }

    return ( false );
  }
}
