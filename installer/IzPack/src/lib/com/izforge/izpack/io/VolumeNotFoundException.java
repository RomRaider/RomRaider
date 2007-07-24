/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2007 Dennis Reil
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.io;

import java.io.IOException;

/**
 * Exception, indicating, that a volume was not found.
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class VolumeNotFoundException extends IOException
{

    protected String volumename;

    protected long alreadyskippedbytes;

    private static final long serialVersionUID = 9062182895972373707L;

    public VolumeNotFoundException()
    {
        super();
    }

    public VolumeNotFoundException(String message, String volumename)
    {
        super(message);
        this.volumename = volumename;
    }

    /**
     * Returns the name of the volume, which couldn't be found
     * 
     * @return the name of the volume
     */
    public String getVolumename()
    {
        return volumename;
    }

    /**
     * Returns the amount of skipped bytes, if a skip-operation was in progress
     * 
     * @return the amount of skipped bytes
     */
    public long getAlreadyskippedbytes()
    {
        return alreadyskippedbytes;
    }

    /**
     * Sets the amount of already skipped bytes.
     * 
     * @param alreadyskippedbytes
     */
    public void setAlreadyskippedbytes(long alreadyskippedbytes)
    {
        this.alreadyskippedbytes = alreadyskippedbytes;
    }
}
