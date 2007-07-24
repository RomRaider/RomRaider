/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Gaganis Giorgos
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

package com.izforge.izpack.panels;

import com.izforge.izpack.LocaleDatabase;

/**
 * This interface is used to be able to access the common information in the PackPanel and the
 * ImgPacksPAnel through a common type. I introduced it so that I can remove the duplicate
 * PacksModel from each class and create a common one for both.
 * 
 * This could be avoided by inheriting ImgPacksPanel from PacksPanel
 * 
 * User: Gaganis Giorgos Date: Sep 17, 2004 Time: 8:29:22 AM
 */

/*
 * @todo evaluate whether we want to eliminate this interface with inheritance
 */
public interface PacksPanelInterface
{

    public LocaleDatabase getLangpack();

    public int getBytes();

    public void setBytes(int bytes);

    public void showSpaceRequired();

    public void showFreeSpace();
}
