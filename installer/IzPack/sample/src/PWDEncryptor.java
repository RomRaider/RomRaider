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
 * This class provides a demonstration for using an encryption service in
 * connection with a password field, as used in a <code>UserInputPanel</code>.
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class PWDEncryptor implements Processor
{
 /*--------------------------------------------------------------------------*/
 /**
  * Encrypts the a password and returns the encrypted result. <br>
  * <b>Note:</b> this is not a real encryption algorithm. The code only
  * demonstrates the use of this interface in a real installation environment.
  * For a real application a proper encryption mechanism must be used. Though
  * Java 1.4.X provides such algorithms, you need to consider that not all
  * potential target environments have this version installed. It seems best
  * to include the necessary encryption library with the installer.
  *
  * @param     client   the client object using the services of this encryptor.
  *
  * @return    the encryption result.
  */
 /*--------------------------------------------------------------------------*/
  public String process (ProcessingClient client)
  {
    if (client.getNumFields () < 1)
    {
      return ("");
    }
    
    char [] password = client.getFieldContents (0).toCharArray ();
    char [] result   = new char [password.length];
    int  temp;
    
    for (int i = 0; i < password.length; i++)
    {
      temp = password [i] - 57;
      if (i > 0)
      {
        temp = temp + password [i - 1];
      }

      if ((temp % 3) == 0)
      {
        temp = temp + 13;
      }
      if (temp < 0)
      {
        temp = temp + 193;
      }
    
      result [i] = (char)temp;
    }

    return (new String (result));
  }
}
/*---------------------------------------------------------------------------*/
