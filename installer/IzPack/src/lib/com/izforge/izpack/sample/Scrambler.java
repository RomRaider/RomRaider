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
import com.izforge.izpack.panels.Processor;

/*---------------------------------------------------------------------------*/
/**
 * This class provides a demonstration for using an encryption service in
 * connection with a <code>RuleInputField</code>, as used in a
 * <code>UserInputPanel</code>.
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class Scrambler implements Processor
{
 /*--------------------------------------------------------------------------*/
 /**
  * Rearranges the input fields and concatenates the result, separating
  * individual fields with a '*'.
  *
  * @param     client   the client object using the services of this encryptor.
  *
  * @return    the encryption result.
  */
 /*--------------------------------------------------------------------------*/
  public String process (ProcessingClient client)
  {
    StringBuffer buffer = new StringBuffer ();
    
    for (int i = client.getNumFields () - 1; i > -1; i--)
    {
      buffer.append (client.getFieldContents (i));
      if (i > 0)
      {
        buffer.append ('*');
      }
    }
    
    return (buffer.toString ());
  }
}
/*---------------------------------------------------------------------------*/
