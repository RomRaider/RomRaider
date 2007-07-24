/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2007 Markus Schlegel
 * Copyright 2007 Julien Ponge
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

package com.izforge.izpack.event;

import com.izforge.izpack.installer.*;
import com.izforge.izpack.panels.ShortcutPanel;
import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * Creates the Shortcuts after files have been installed.
 * Use this listener, if you place the ShortcutPanel before the Installation of the files.
 * 
 * @author Marcus Schlegel, Pulinco
 */
public class LateShortcutInstallListener extends SimpleInstallerListener {

  public LateShortcutInstallListener()
  {
      ShortcutPanel.createImmediately = false;
  }
  
  public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
  throws Exception
  {
      //now it's time to write down the shortcuts...
      ShortcutPanel.getInstance().createAndRegisterShortcuts();
  }
}
