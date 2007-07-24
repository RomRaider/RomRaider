/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
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

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

/*---------------------------------------------------------------------------*/
/**
 * This class represents a simple validator for IP addresses to demonstrate
 * the implementation of a rule validator that cooperates with the
 * <code>RuleInputField</code> used in the <code>UserInputPanel</code>
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class IPValidator implements Validator
{
 /*--------------------------------------------------------------------------*/
 /**
  * Validates the contend of a <code>RuleInputField</code>. The test is
  * intended for a rule input field composed of four sub-fields. The
  * combination of their individual content is assumed to represent an IP
  * address.
  *
  * @param     client   the client object using the services of this validator.
  *
  * @return    <code>true</code> if the validation passes, otherwise <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
  public boolean validate (ProcessingClient client)
  {
    // ----------------------------------------------------
    // verify that there are actually four sub-fields. A
    // different number would indicate that we are not
    // connected with the RuleInputField that we expect
    // ----------------------------------------------------
    if (client.getNumFields () != 4)
    {
      return (false);
    }
    
    // ----------------------------------------------------
    // test each field to make sure it actually contains
    // an integer and the value of the integer is beween
    // 0 and 255.
    // ----------------------------------------------------
    boolean isIP = true;
    
    for (int i = 0; i < 4; i++)
    {
      int value;
      
      try
      {
        value = Integer.parseInt (client.getFieldContents (i));
        if ((value < 0) || (value > 255))
        {
          isIP = false;
        }
      }
      catch (Throwable exception)
      {
        isIP = false;
      }
    }
    
    return (isIP);
  }
}
/*---------------------------------------------------------------------------*/
