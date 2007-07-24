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


/**
 * Constants related to MS Windows DACLs.
 * @author Klaus Bartz
 *
 */
public interface MSWinConstants
{
    /*
     * Registry root values, extracted from winreg.h
     */
    /** HKCR registry root */
    static final int HKEY_CLASSES_ROOT = 0x80000000;

    /** HKCU registry root */
    static final int HKEY_CURRENT_USER = 0x80000001;

    /** HKLM registry root */
    static final int HKEY_LOCAL_MACHINE = 0x80000002;

    /** HKU registry root */
    static final int HKEY_USERS = 0x80000003;

    /** HKPD registry root */
    static final int HKEY_PERFORMANCE_DATA = 0x80000004;

    /** HKCC registry root */
    static final int HKEY_CURRENT_CONFIG = 0x80000005;

    /** HKDD registry root */
    static final int HKEY_DYN_DATA = 0x80000006;

    /*
     * Registry value types, extracted from winnt.h
     */
    /** No value type */
    static final int REG_NONE = 0; 

    /** Unicode nul terminated string */
    static final int REG_SZ = 1; 

    /** Unicode nul terminated string */
    static final int REG_EXPAND_SZ = 2; 

    /** Free form binary */
    static final int REG_BINARY = 3; 

    /** 32-bit number */
    static final int REG_DWORD = 4; 

    /** Symbolic Link (unicode) */
    static final int REG_LINK = 6; 

    /** Multiple Unicode strings */
    static final int REG_MULTI_SZ = 7; 

   //
    // Define access rights to files and directories
    // Copied from winnt.h BUILD Version: 0095

    /** Flag for permission read file or pipe date. */
    static final int FILE_READ_DATA = 0x0001; // file & pipe

    /** Flag for permission list contents of a directory. */
    static final int FILE_LIST_DIRECTORY = 0x0001; // directory

    /** Flag for permission write file or pipe data. */
    static final int FILE_WRITE_DATA = 0x0002; // file & pipe

    /** Flag for permission add a file to a directory. */
    static final int FILE_ADD_FILE = 0x0002; // directory

    /** Flag for permission add data to a file (append). */
    static final int FILE_APPEND_DATA = 0x0004; // file

    /** Flag for permission add a subdirectory to a directory. */
    static final int FILE_ADD_SUBDIRECTORY = 0x0004; // directory

    /** Flag for permission create a named pipe. */
    static final int FILE_CREATE_PIPE_INSTANCE = 0x0004; // named pipe

    /** Flag for permission read. */
    static final int FILE_READ_EA = 0x0008; // file & directory

    /** Flag for permission write. */
    static final int FILE_WRITE_EA = 0x0010; // file & directory

    /** Flag for permission execute a file. */
    static final int FILE_EXECUTE = 0x0020; // file

    /** Flag for permission traverse through a directory. */
    static final int FILE_TRAVERSE = 0x0020; // directory

    /** Flag for permission delete a file or subdirectory in a directory. */
    static final int FILE_DELETE_CHILD = 0x0040; // directory

    /** Flag for permission all read attributes. */
    static final int FILE_READ_ATTRIBUTES = 0x0080; // all

    /** Flag for permission all write attributes. */
    static final int FILE_WRITE_ATTRIBUTES = 0x0100; // all

    /** Flag for permission delete. */
    static final int DELETE = 0x00010000;

    /** Flag for permission read. */
    static final int READ_CONTROL = 0x00020000;

    /** Flag for permission write a DAC. */
    static final int WRITE_DAC = 0x00040000;

    /** Flag for permission set owner. */
    static final int WRITE_OWNER = 0x00080000;

    /** Flag for permission use synchronize. */
    static final int SYNCHRONIZE = 0x00100000;

    /** Flag for permission standard rights for required. */
    static final int STANDARD_RIGHTS_REQUIRED = 0x000F0000;

    /** Flag for permission standard rights for read. */
    static final int STANDARD_RIGHTS_READ = 0x00020000; // original READ_CONTROL

    /** Flag for permission standard rights for write. */
    static final int STANDARD_RIGHTS_WRITE = 0x00020000; // original READ_CONTROL

    /** Flag for permission standard rights for execute. */
    static final int STANDARD_RIGHTS_EXECUTE = 0x00020000; // original READ_CONTROL

    /** Flag for permission all standard rights. */
    static final int STANDARD_RIGHTS_ALL = 0x001F0000;

    /** Flag for permission all specific rights. */
    static final int SPECIFIC_RIGHTS_ALL = 0x0000FFFF;

    /** Flag for permission STANDARD_RIGHTS_REQUIRED | SYNCHRONIZE | 0x3FF. */
    static final int FILE_ALL_ACCESS = 0x001F03FF;

    /** Flag for permission generic read. */
    static final int FILE_GENERIC_READ = 0x00120089;

    // #define FILE_GENERIC_WRITE (STANDARD_RIGHTS_WRITE |\
    // FILE_WRITE_DATA |\
    // FILE_WRITE_ATTRIBUTES |\
    // FILE_WRITE_EA |\
    // FILE_APPEND_DATA |\
    // SYNCHRONIZE)

    /** Flag for permission generic write. */
    static final int FILE_GENERIC_WRITE = 0x00120116;

    // #define FILE_GENERIC_EXECUTE (STANDARD_RIGHTS_EXECUTE |\
    // FILE_READ_ATTRIBUTES |\
    // FILE_EXECUTE |\
    // SYNCHRONIZE)
    /** Flag for permission generic execute. */
    static final int FILE_GENERIC_EXECUTE = 0x001200A0;

    //
    // AccessSystemAcl access type
    //

    /** Flag for permission all specific rights. */
    static final int ACCESS_SYSTEM_SECURITY = 0x01000000;

    //
    // MaximumAllowed access type
    //

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int MAXIMUM_ALLOWED = 0x02000000;

    //
    // These are the generic rights.
    //

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int GENERIC_READ = 0x80000000; // may be a problem with int ...

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int GENERIC_WRITE = 0x40000000;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int GENERIC_EXECUTE = 0x20000000;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int GENERIC_ALL = 0x10000000;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_CASE_SENSITIVE_SEARCH = 0x00000001;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_CASE_PRESERVED_NAMES = 0x00000002;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_UNICODE_ON_DISK = 0x00000004;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_PERSISTENT_ACLS = 0x00000008;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_FILE_COMPRESSION = 0x00000010;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_VOLUME_QUOTAS = 0x00000020;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_SUPPORTS_SPARSE_FILES = 0x00000040;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_SUPPORTS_REPARSE_POINTS = 0x00000080;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_SUPPORTS_REMOTE_STORAGE = 0x00000100;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_VOLUME_IS_COMPRESSED = 0x00008000;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_SUPPORTS_OBJECT_IDS = 0x00010000;

    /**
     * Flag for NT permissions: For more information see the Windows NT description for permisson
     * flags.
     */
    static final int FILE_SUPPORTS_ENCRYPTION = 0x00020000;


}
