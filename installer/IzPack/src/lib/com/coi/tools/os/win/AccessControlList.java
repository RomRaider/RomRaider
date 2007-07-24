/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2006 Klaus Bartz
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

package com.coi.tools.os.win;

import java.util.ArrayList;

/**
 * Data container for access control lists used by the registry stuff in the java and in the native
 * part. DO NOT CHANGE METHODE SIGNATURES etc. without addapt the native methods
 * RegistryImpl.modifyKeyACL and RegistryImpl.getKeyACL.
 * 
 * @author Klaus Bartz
 * 
 */
public class AccessControlList extends java.util.ArrayList
{

    private ArrayList permissions = new ArrayList();

    /**
     * Default constructor.
     */
    public AccessControlList()
    {
        super();
    }

    /**
     * Creates an ACE entry in the permission array with the given values.
     * 
     * @param owner owner of the ACE
     * @param allowed access allowed mask
     * @param denied access denied mask
     */
    public void setACE(String owner, int allowed, int denied)
    {
        AccessControlEntry ace = new AccessControlEntry(owner, allowed, denied);
        permissions.add(ace);
    }

    /**
     * Returns the access control entry related to the given id.
     * 
     * @param num id in the internal permisson array.
     * @return the access control entry for the given id
     */
    public AccessControlEntry getACE(int num)
    {
        return ((AccessControlEntry) (((AccessControlEntry) permissions.get(num)).clone()));
    }

    /**
     * Returns number of access control entries.
     * 
     * @return number of access control entries
     */
    public int getACECount()
    {
        return (permissions.size());
    }

    /**
     * This class holds a representation of MS Windows ACEs.
     * 
     * @author Klaus Bartz
     * 
     */
    public static class AccessControlEntry implements Cloneable
    {

        private String owner;

        private int accessAllowdMask;

        private int accessDeniedMask;

        /**
         * Default constructor.
         */
        public AccessControlEntry()
        {
            super();
        }

        /**
         * Creates an ACE with the given parameter.
         * 
         * @param owner2 owner of the ACE
         * @param allowed access allowed mask
         * @param denied access denied mask
         */
        public AccessControlEntry(String owner2, int allowed, int denied)
        {
            owner = owner2;
            accessAllowdMask = allowed;
            accessDeniedMask = denied;
        }

        /**
         * Returns the owner.
         * 
         * @return the owner
         */
        public String getOwner()
        {
            return owner;
        }

        /**
         * Sets owner to the given value.
         * 
         * @param owner The owner to set.
         */
        public void setOwner(String owner)
        {
            this.owner = owner;
        }

        /**
         * Returns the accessAllowdMask.
         * 
         * @return the accessAllowdMask
         */
        public int getAccessAllowdMask()
        {
            return accessAllowdMask;
        }

        /**
         * Sets accessAllowdMask to the given value.
         * 
         * @param accessAllowdMask The accessAllowdMask to set.
         */
        public void setAccessAllowdMask(int accessAllowdMask)
        {
            this.accessAllowdMask = accessAllowdMask;
        }

        /**
         * Returns the accessDeniedMask.
         * 
         * @return the accessDeniedMask
         */
        public int getAccessDeniedMask()
        {
            return accessDeniedMask;
        }

        /**
         * Sets accessDeniedMask to the given value.
         * 
         * @param accessDeniedMask The accessDeniedMask to set.
         */
        public void setAccessDeniedMask(int accessDeniedMask)
        {
            this.accessDeniedMask = accessDeniedMask;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#clone()
         */
        public Object clone()
        {
            try
            {
                return (super.clone());
            }
            catch (CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
            return (null);
        }
    }

}
