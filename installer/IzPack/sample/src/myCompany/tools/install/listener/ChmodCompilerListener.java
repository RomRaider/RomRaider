/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
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

package com.myCompany.tools.install.listener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.n3.nanoxml.XMLElement;
import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.event.SimpleCompilerListener;


/**
 * <p>CompilerListener for file and directory permissions.</p>
 *
 * @author  Klaus Bartz
 *
 */
public class ChmodCompilerListener extends SimpleCompilerListener
{


  /* (non-Javadoc)
   * @see com.izforge.izpack.compiler.CompilerListener#reviseAdditionalDataMap(java.util.Map, net.n3.nanoxml.XMLElement)
   */
  public Map reviseAdditionalDataMap(Map existentDataMap, XMLElement element)
    throws CompilerException
  {
    Map retval = existentDataMap != null ? 
      existentDataMap : new  HashMap();
    Vector dataList = element.getChildrenNamed("additionaldata");
    Iterator iter = null;
    if( dataList == null ||  dataList.size() == 0  )
      return( existentDataMap);
    iter = dataList.iterator();
    while( iter.hasNext() )
    {
      XMLElement data = (XMLElement) iter.next();
      String [] relevantKeys = { "permission.dir", "permission.file"};
      for( int i = 0; i < relevantKeys.length; ++i )
      {
        String key = data.getAttribute("key");
        if( key.equalsIgnoreCase(relevantKeys[i]))
        {
          String value = data.getAttribute("value");
          if (value == null || value.length() == 0)
            continue;
          try
          {
            int radix = value.startsWith("0") ? 8 : 10;
            retval.put(key,Integer.valueOf(value, radix));
          } catch (NumberFormatException x)
          {
            throw new CompilerException("'" + relevantKeys[i] + "' must be an integer");
          }
        }
      }
    }
    return retval;
  }
}
