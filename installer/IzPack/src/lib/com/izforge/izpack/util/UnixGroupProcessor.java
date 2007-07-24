/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2004 Thorsten Kamann
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
import java.io.FileReader;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Processor;

/**
 * @author thorsten-kamann
 */
public class UnixGroupProcessor implements Processor {

	public String process (ProcessingClient client){
		String retValue = "";
		String filepath = "/etc/group";
		BufferedReader reader = null;
		String line = "";
				
		try{
			reader = new BufferedReader(new FileReader(filepath));
			while ((line = reader.readLine()) != null){
				retValue += line.substring(0, line.indexOf(":"))+":";
			}
			if (retValue.endsWith(":")){
				retValue = retValue.substring(0, retValue.length()-1);
			}			
		}catch (Exception ex){
			retValue = "";
		}
		
		return retValue;
	}

}
