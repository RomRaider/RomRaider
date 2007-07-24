/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

#include "UnicodeHelper.h"
#include <windows.h>
#include <jni.h>
#include "com_coi_tools_os_izpack_COIOSHelper.h"
#include "WinLibEnv.h"


#ifndef RC_INVOKED    // start of source code

//----------------------------------------------------------------------
// This is the main source of COI OS helper.
// The package contains the C++ side of Java classes; the files has the
// names like the classes with Impl at end. They contains only the
// native java methods with the Java specific handling. The real work
// will be done in files, which ends with "Internal".
// Most of the internals are simple functions, not classes. 
// This way was choosen because most functions should call functions of
// the OS to do there work. To wrap it into classes will produce much
// overhead. Some functions will be called only once, but other
// functions can be called much times.
// 
// For exception handling and so on, every internal function is called
// with an object labelled "WinLibEnv". It is short and will be used
// only if an error occurs.
//
//----------------------------------------------------------------------

// --------------------------------------------------------------------------
// This function frees this dll, allowing the operating system to remove
// the code from memory and releasing the reference to the dll on disk. 
// After this call this dll can not be used any more.
//
// THIS FUNCTION DOES NOT RETURN !!!
// --------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_coi_tools_os_izpack_COIOSHelper_FreeLibrary
	(JNIEnv *env, jobject obj, jstring name)
{
	// convert the name from Java string type
	const TCHAR *libraryName = env->GET_STRING_CHARS (name, 0);

	// get a module handle 
	HMODULE handle = GetModuleHandle (libraryName);

	// release the string object
	env->RELEASE_STRING_CHARS (name, libraryName);
	
	// destroy the acl factory
	// now we are rady to free the library
	FreeLibraryAndExitThread (handle, 0);
}







	

#else // RC_INVOKED, end of source code, start of resources
// resource definition go here

#endif // RC_INVOKED

		
