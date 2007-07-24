/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Elmar Grom
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

package   com.izforge.izpack.sample;

import    com.izforge.izpack.panels.*;

/*---------------------------------------------------------------------------*/
/**
 * This class represents a simple validator for passwords to demonstrate
 * the implementation of a password validator that cooperates with the
 * password field in the <code>UserInputPanel</code>
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class PWDValidator implements Validator
{
 /*--------------------------------------------------------------------------*/
 /**
  * Validates the contend of multiple password fields. The test 
  *
  * @param     client   the client object using the services of this validator.
  *
  * @return    <code>true</code> if the validation passes, otherwise <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
  public boolean validate (ProcessingClient client)
  {
    int numFields = client.getNumFields ();
    
    // ----------------------------------------------------
    // verify that there is more than one field. If there
    // is only one field we have to return true.
    // ----------------------------------------------------
    if (numFields < 2)
    {
      return (true);
    }
    
    boolean match   = true;
    String  content = client.getFieldContents (0);
    
    for (int i = 1; i < numFields; i++)
    {
      if (!content.equals (client.getFieldContents (i)))
      {
        match = false;
      }      
    }
    
    return (match);
  }
}
/*---------------------------------------------------------------------------*/
