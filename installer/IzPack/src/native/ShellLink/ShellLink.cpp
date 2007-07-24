/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
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

/* Stuff for differ between UNICODE and MBCS 
 * May be this work only with visual studio 6.x
 * To switch between MBCS and UNICODE change the
 * precompiler defines from /D _MBCS to /D _UNICODE
 * ----------------- START ---------------------
 */
#ifdef _UNICODE
#define UNICODE
#define NEW_STRING(a)		NewString(a, _tcslen(a))
#define GET_STRING_CHARS	GetStringChars
#define RELEASE_STRING_CHARS ReleaseStringChars
#else
#define NEW_STRING(a)		NewStringUTF(a)
#define GET_STRING_CHARS	GetStringUTFChars
#define RELEASE_STRING_CHARS ReleaseStringUTFChars
#endif

#ifdef _WINDOWS
#define WINVER 0x0400
#define _WIN32_WINNT 0x0400
#endif
/* Stuff for differ between UNICODE and MBCS 
 * May be this work only with visual studio 6.x
 * ----------------- END ---------------------
 */



#include "com_izforge_izpack_util_os_ShellLink.h"
#include <winerror.h>
#include <objbase.h>
#include <basetyps.h>
#include <shlobj.h>
#include <objidl.h>
#include <windows.h>
#include <tchar.h>

// --------------------------------------------------------------------------
// Gound rules used for the implementation of this native interface
// of ShellLink:
//
// 1) all functions return an integer success code
// 2) positive success codes report that everything went ok
// 3) negative success codes report some type of problem
// 4) a success code of 0 does not exist
// 5) 'get' functions deposit their results in the corresponding member
//    variables on the Java side.
// 6) "set' functions retrieve their input from the corresponding member
//    variables on the Java side.
// 7) functions other than 'get' and 'set' recive their input -if any- in
//    the form of arguments.
// 8) functions that are exposed on the Java side (public, protectd)
//    follow the Java naming conventions, in that they begin with a lower
//    case character.
// 9) all functions that have a Java wrapper by the same name follow the
//    Windows naming convention, in that they start with an upper case
//    letter. This avoids having to invent new method names for Java and
//    it allows to keep a clean naming convention on the Java side.
// ============================================================================
//
// I M P O R T A N T !
// -------------------
//
// This interface communicates with the OS via COM. In order for things to
// work properly, it is necessary to observe the following pattern of
// operation and to observe the order of execution (i.e. do not call
// getInterface() before calling initializeCOM()).
//
// 1) call initializeCOM() - It's best to do this in the constructor
// 2) call getInterface() - It's best to do this in the constructor as well
//
// 3) do your stuff (load, save, get, set ...)
//
// 4) call releaseInterface() before terminating the application, best done
//    in the finalizer
// 5) call releaseCOM() before terminating the application, best done
//    in the finalizer. Do NOT call this if the call to initializeCOM() did
//    not succeed, otherwise you'll mess things up pretty badly!
// ============================================================================
// Variables that must be declared on the Java side:
//
// private int     nativeHandle;
//
// private String  linkPath;
// private String  linkName;
//
// private String  arguments;
// private String  description;
// private String  iconPath;
// private String  targetPath;
// private String  workingDirectory;
//
// private int     hotkey;
// private int     iconIndex;
// private int     showCommand;
// private int     linkType;
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
// Macro Definitions
// --------------------------------------------------------------------------
#define   ACCESS                0         // index for retrieving the registry access key
#define   MIN_KEY               0         // for verifying that an index received in the form of a call parameter is actually leagal
#define   MAX_KEY               5         // for verifying that an index received in the form of a call parameter is actually leagal

// --------------------------------------------------------------------------
// Prototypes
// --------------------------------------------------------------------------
// Hey C crowd, don't freak out! ShellLink.h is auto generated and I'd like
// to have everything close by in this package. Besides, these are only used
// in this file...
// --------------------------------------------------------------------------
int  getNewHandle ();
void freeLinks ();

// --------------------------------------------------------------------------
// Constant Definitions
// --------------------------------------------------------------------------

// the registry keys to get access to the various shortcut locations for the current user
const char CURRENT_USER_KEY [5][100] =
{
  "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", // this is where the details are stored in the registry
  "Desktop",                                                               // this is where desktop shortcuts go
  "Programs",                                                              // this is where items of the progams menu go
  "Start Menu",                                                            // this is right in the start menu
  "Startup"                                                                // this is where stuff goes that should be executed on OS launch
};

// the registry keys to get access to the various shortcut locations for all users
const char ALL_USER_KEY [5][100] =
{
  "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", // this is where the details are stored in the registry
  "Common Desktop",                                                        // this is where desktop shortcuts go
  "Common Programs",                                                       // this is where items of the progams menu go
  "Common Start Menu",                                                     // this is right in the start menu
  "Common Startup"                                                         // this is where stuff goes that should be executed on OS launch
};

// Success Codes
const jint  SL_OK                 =  1;     // returned if a call was successful
const jint  SL_ERROR              = -1;     // unspecific return if a call was not successful
const jint  SL_INITIALIZED        = -2;     // return value from initialization functions if already initialized
const jint  SL_NOT_INITIALIZED    = -3;     // return value from uninitialization functions if never initialized
const jint  SL_OUT_OF_HANDLES     = -4;     // there are no more interface handles available
const jint  SL_NO_IPERSIST        = -5;     // could not get a handle for the IPersist interface
const jint  SL_NO_SAVE            = -6;     // could not save the link
const jint  SL_WRONG_DATA_TYPE    = -7;     // an unexpected data type has been passed or received
const jint  SL_CAN_NOT_READ_PATH  = -8;     // was not able to read the link path from the Windows Registry

const int   MAX_TEXT_LENGTH       =  1000;  // buffer size for text buffers
const int   ALLOC_INCREMENT       =  10;    // allocation increment for allocation of additional storage space for link references

// --------------------------------------------------------------------------
// Variable Declarations
// --------------------------------------------------------------------------
int           referenceCount      = 0;

// --------------------------------------------------------
// DLLs are not objects!
// --------------------
// What this means is that if multiple references are made
// to the same DLL in the same program space, no new
// storage is allocated for the variables in the DLL.
// For all practical purposes, variables in DLLs are equal
// to static variables in classes - all instances share
// the same storage space.
// ========================================================
// Since this code is designed to operate in conjunction
// with a Java class, there is a possibility for multiple
// instances of the class to acces this code 'simultaniously'.
// As a result, one instance could be modifying the link
// data for another instance. To avoid this, I am
// artificially creating multiple DLL 'instances' by
// providing a storage array for pointers to multiple
// instances of IShellLink. Each Java instance must
// access its IShellLink through a handle (the array
// index where its corresponding pointer is stored).
// ========================================================
// For details on how this works see:
// - getNewHandle()
// - freeLinks()
// --------------------------------------------------------
int           linkCapacity        = 0;      // indicates the current capacity for storing pointers
IShellLink**  p_shellLink         = NULL;   // pointers to the IShellLink interface

// --------------------------------------------------------------------------
// Gain COM access
//
// returns: SL_OK       if the initialization was successfull
//          SL_ERROR    otherwise
//
// I M P O R T A N T !!
// --------------------
//
// 1) This method must be called first!
// 2) The application must call releaseCOM() just before terminating but
//    only if a result of SL_OK was retruned form this function!
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_initializeCOM (JNIEnv  *env,
                                                                                jobject  obj)
{
  HRESULT hres;

  if (referenceCount > 0)
  {
    referenceCount++;
    return (SL_OK);
  }

  hres = CoInitializeEx (NULL, COINIT_APARTMENTTHREADED);

  if (SUCCEEDED (hres))
  {
    referenceCount++;
    return (SL_OK);
  }

  return (SL_ERROR);
}

// --------------------------------------------------------------------------
// Releases COM and frees associated resources. This function should be
// called as the very last operation before the application terminates.
// Call this function only if a prior call to initializeCOM() returned SL_OK.
//
// returns: SL_OK               under normal circumstances
//          SL_NOT_INITIALIZED  if the reference count indicates that no
//                              current users exist.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_releaseCOM (JNIEnv  *env,
                                                                             jobject  obj)
{
  referenceCount--;

  if (referenceCount == 0)
  {
    CoUninitialize ();
    // This is the end of things, so this is a good time to
    // free the storage for the IShellLink pointers.
    freeLinks ();
    return (SL_OK);
  }
  else if (referenceCount < 0)
  {
    referenceCount++;
    return (SL_NOT_INITIALIZED);
  }
  else
  {
    return (SL_OK);
  }
}

// --------------------------------------------------------------------------
// This function gains access to the ISchellLink interface. It must be
// called before any other calls can be made but after initializeCOM().
//
// I M P O R T A N T !!
// --------------------
//
// releaseInterface() must be called before terminating the application!
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_getInterface (JNIEnv  *env,
                                                                               jobject  obj)
{
  HRESULT hres;
  int     handle;

  // Get a handle
  handle = getNewHandle ();
  if (handle < 0)
  {
    return (SL_OUT_OF_HANDLES);
  }

  // Store the handle on the Java side
  jclass      cls       = (env)->GetObjectClass( obj );
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");

  (env)->SetIntField (obj, handleID, (jint)handle);

  /*
   * Note: CoCreateInstance() supports only the creation of a single instance.
   * Need to find out how to use CoGetClassObject() to create multiple instances.
   * It should be possible to have multiple instances available, got to make this work!
   */

  // Get a pointer to the IShellLink interface
  hres = CoCreateInstance (CLSID_ShellLink,
                           NULL,
                           CLSCTX_INPROC_SERVER,
                           IID_IShellLink,
                           (void **)&p_shellLink [handle]);

  // Do error handling
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }

  return (SL_ERROR);
}

// --------------------------------------------------------------------------
// This function returns a new handle to be used for the next client. If no
// more handles are available -1 is returnd.
// --------------------------------------------------------------------------
int getNewHandle ()
{
  IShellLink* pointer;

  // loop through the array to find an unoccupied location
  int i;
  for (i = 0; i < linkCapacity; i++)
  {
    pointer = p_shellLink [i];
    // if an unoccupied location is found return the index
    if (pointer == NULL)
    {
      return (i);
    }
  }

  // if we get here, all locations are in use and we need to
  // create more storage space to satisfy the request
  int   newSize     = sizeof (IShellLink*) * (linkCapacity + ALLOC_INCREMENT);
  void* tempPointer = realloc ((void *)p_shellLink, newSize);

  if (tempPointer != NULL)
  {
    p_shellLink  = (IShellLink**)tempPointer;
    linkCapacity = linkCapacity + ALLOC_INCREMENT;

    for (int k = i; k < linkCapacity; k++)
    {
      p_shellLink [k] = NULL;
    }
    return (i);
  }
  else
  {
    return (-1);
  }
}

// --------------------------------------------------------------------------
// This function frees the storage that was allocated for the storage of
// pointers to IShellLink interfaces. It also cleans up any interfaces that
// have not yet been reliquished (clients left a mess -> bad boy!).
// --------------------------------------------------------------------------
void freeLinks ()
{
  if (p_shellLink != NULL)
  {
    // loop through the array and release any interfaces that
    // have not been freed yet
    IShellLink* pointer;
    for (int i = 0; i < linkCapacity; i++)
    {
      pointer = p_shellLink [i];
      // if an unoccupied location is found, return the index
      if (pointer != NULL)
      {
        pointer->Release ();
        p_shellLink [i] = NULL;
      }
    }

    // free the pointer storage itself
    linkCapacity = 0;
    free (p_shellLink);
  }
}

// --------------------------------------------------------------------------
// This function frees this dll, allowing the operating system to remove
// the code from memory and releasing the reference to the dll on disk. 
// After this call this dll can not be used any more.
//
// THIS FUNCTION DOES NOT RETURN !!!
// --------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_izforge_izpack_util_os_ShellLink_FreeLibrary (JNIEnv *env, 
                                                                              jobject obj,
                                                                              jstring name)
{
  // convert the name from Java string type
  const TCHAR *libraryName = (env)->GET_STRING_CHARS (name, 0);

  // get a module handle 
  HMODULE handle = GetModuleHandle (libraryName);

  // release the string object
  (env)->RELEASE_STRING_CHARS (name, libraryName);
  
  // now we are rady to free the library
  FreeLibraryAndExitThread (handle, 0);
}

// --------------------------------------------------------------------------
// Releases the interface
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_releaseInterface (JNIEnv  *env,
                                                                                   jobject  obj)
{
  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  if (handle < 0)
  {
    return (SL_OK);
  }
  
  if (p_shellLink [handle] == NULL)
  {
    return (SL_NOT_INITIALIZED);
  }

  p_shellLink [handle]->Release ();
  p_shellLink [handle] = NULL;
  (env)->SetIntField (obj, handleID, -1);
  return (SL_OK);
}

// --------------------------------------------------------------------------
// Retrieves the command-line arguments associated with a shell link object
//
// Result is deposited in 'arguments'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetArguments (JNIEnv  *env,
                                                                               jobject  obj)
{
  TCHAR    arguments [MAX_TEXT_LENGTH];
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetArguments (arguments,
                                             MAX_TEXT_LENGTH);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  argumentsID = (env)->GetFieldID      (cls, "arguments", "Ljava/lang/String;");
    jstring   j_arguments = (env)->NEW_STRING      (arguments);

    (env)->SetObjectField (obj, argumentsID, j_arguments);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the description string for a shell link object.
//
// Result is deposited in 'description'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetDescription (JNIEnv  *env,
                                                                                 jobject  obj)
{
  TCHAR description [MAX_TEXT_LENGTH];
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetDescription (description,
                                               MAX_TEXT_LENGTH);

  if (SUCCEEDED (hres))
  {
    jfieldID  descriptionID = (env)->GetFieldID      (cls, "description", "Ljava/lang/String;");
    jstring   j_description = (env)->NEW_STRING      (description); // convert to Java String type

    (env)->SetObjectField (obj, descriptionID, j_description);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the hot key for a shell link object.
//
// Result is deposited in 'hotkey'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetHotkey	(JNIEnv  *env,
                                                                             jobject  obj)
{
  WORD    hotkey;
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetHotkey (&hotkey);

  if (SUCCEEDED (hres))
  {
    jfieldID  hotkeyID = (env)->GetFieldID      (cls, "hotkey", "I");

    (env)->SetIntField (obj, hotkeyID, (jint)hotkey);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }


}

// --------------------------------------------------------------------------
// Retrieves the location (path and index) of the icon for a shell link object.
//
// The path is deposited in 'iconPath'
// The index is deposited in 'iconIndex'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetIconLocation	(JNIEnv  *env,
                                                                                   jobject  obj)
{
  HRESULT hres;
  TCHAR    iconPath [MAX_PATH];
  int     iconIndex;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetIconLocation (iconPath,
                                                MAX_PATH,
                                                &iconIndex);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  pathID      = (env)->GetFieldID      (cls, "iconPath", "Ljava/lang/String;");
    jfieldID  indexID     = (env)->GetFieldID      (cls, "iconIndex", "I");
    jstring   j_iconPath  = (env)->NEW_STRING      (iconPath);

    (env)->SetObjectField  (obj, pathID, j_iconPath);
    (env)->SetIntField     (obj, indexID, (jint)iconIndex);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }


}

// --------------------------------------------------------------------------
// Retrieves the path and filename of a shell link object.
//
// Result is deposited in 'targetPath'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetPath (JNIEnv  *env,
                                                                          jobject  obj)
{
  WIN32_FIND_DATA findData;
  TCHAR            targetPath [MAX_PATH];
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetPath (targetPath,
                                        MAX_PATH,
                                        &findData,
                                        SLGP_UNCPRIORITY);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  pathID        = (env)->GetFieldID(cls, "targetPath", "Ljava/lang/String;");
    jstring   j_targetPath  = (env)->NEW_STRING(    targetPath);

    (env)->SetObjectField (obj, pathID, j_targetPath);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the show (SW_) command for a shell link object.
//
// Result is deposited in 'showCommand'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetShowCommand (JNIEnv  *env,
                                                                                 jobject  obj)
{
  HRESULT   hres;
  int       showCommand;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetShowCmd (&showCommand);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  commandID = (env)->GetFieldID      (cls, "showCommand", "I");

    (env)->SetIntField (obj, commandID, (jint)showCommand);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Retrieves the name of the working directory for a shell link object.
//
// Result is deposited in 'workingDirectory'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetWorkingDirectory	(JNIEnv  *env,
                                                                                       jobject  obj)
{
  HRESULT hres;
  TCHAR workingDirectory [MAX_PATH];

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->GetWorkingDirectory (workingDirectory,
                                                    MAX_PATH);

  // ------------------------------------------------------
  // set the member variables
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    jfieldID  directoryID         = (env)->GetFieldID      (cls, "workingDirectory", "Ljava/lang/String;");
    jstring   j_workingDirectory  = (env)->NEW_STRING      (workingDirectory);

    (env)->SetObjectField (obj, directoryID, j_workingDirectory);
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Resolves a shell link by searching for the shell link object and
// updating the shell link path and its list of identifiers (if necessary).
//
// I recommend to call this function before saving the shortcut. This will
// ensure that the link is working and all the identifiers are updated, so
// that the link will actually work when used later on. If for some reason
// the link can not be resolved, at least the creating application knows
// about this.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_Resolve	(JNIEnv  *env,
                                                                           jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  hres = p_shellLink [handle]->Resolve (NULL,
                                        SLR_NO_UI | SLR_UPDATE);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the command-line arguments associated with a shell link object.
//
// Input is taken from 'arguments'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetArguments (JNIEnv  *env,
                                                                               jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    argumentsID = (env)->GetFieldID               (cls, "arguments", "Ljava/lang/String;");
  jstring     j_arguments = (jstring)(env)->GetObjectField  (obj, argumentsID);
  const TCHAR *arguments   = (env)->GET_STRING_CHARS        (j_arguments, 0);

  hres = p_shellLink [handle]->SetArguments (arguments);

  (env)->RELEASE_STRING_CHARS(j_arguments, arguments);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the description string for a shell link object.
//
// Input is taken from 'description'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetDescription (JNIEnv  *env,
                                                                                 jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    descriptionID = (env)->GetFieldID               (cls, "description", "Ljava/lang/String;");
  jstring     j_description = (jstring)(env)->GetObjectField  (obj, descriptionID);
  const TCHAR *description   = (env)->GET_STRING_CHARS        (j_description, 0);

  hres = p_shellLink [handle]->SetDescription( description );

  (env)->RELEASE_STRING_CHARS(j_description, description);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the hot key for a shell link object.
//
// Input is taken from 'hotkey'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetHotkey	(JNIEnv  *env,
                                                                             jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    hotkeyID  = (env)->GetFieldID      (cls, "hotkey", "I");
  jint        hotkey    = (env)->GetIntField     (obj, hotkeyID);

  hres = p_shellLink [handle]->SetHotkey ((unsigned short)hotkey);
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the location (path and index) of the icon for a shell link object.
//
// The path is taken from 'iconPath'
// The index is taken from 'iconIndex'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetIconLocation	(JNIEnv  *env,
                                                                                   jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    pathID        = (env)->GetFieldID               (cls, "iconPath", "Ljava/lang/String;");
  jstring     j_iconPath    = (jstring)(env)->GetObjectField  (obj, pathID);
  const TCHAR *iconPath      = (env)->GET_STRING_CHARS        (j_iconPath, 0);

  jfieldID    indexID       = (env)->GetFieldID               (cls, "iconIndex", "I");
  jint        iconIndex     = (env)->GetIntField              (obj, indexID);

  hres = p_shellLink [handle]->SetIconLocation (iconPath,
                                                iconIndex);

  (env)->RELEASE_STRING_CHARS(j_iconPath, iconPath);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the path and filename of a shell link object.
//
// Input is taken from 'targetPath'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetPath	(JNIEnv  *env,
                                                                           jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    pathID        = (env)->GetFieldID               (cls, "targetPath", "Ljava/lang/String;");
  jstring     j_targetPath  = (jstring)(env)->GetObjectField  (obj, pathID);
  const TCHAR *targetPath    = (env)->GET_STRING_CHARS        (j_targetPath, 0);

  hres = p_shellLink [handle]->SetPath (targetPath);

  (env)->RELEASE_STRING_CHARS(j_targetPath, targetPath);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the show (SW_) command for a shell link object.
//
// Input is taken from 'showCommand'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetShowCommand (JNIEnv  *env,
                                                                                 jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    commandID   = (env)->GetFieldID      (cls, "showCommand", "I");
  jint        showCommand = (env)->GetIntField     (obj, commandID);

  hres = p_shellLink [handle]->SetShowCmd (showCommand);
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// Sets the name of the working directory for a shell link object.
//
// Input is taken from 'workingDirectory'
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_SetWorkingDirectory	(JNIEnv  *env,
                                                                                       jobject  obj)
{
  HRESULT hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ------------------------------------------------------
  // get the member variables
  // ------------------------------------------------------
  jfieldID    pathID              = (env)->GetFieldID               (cls, "workingDirectory", "Ljava/lang/String;");
  jstring     j_workingDirectory  = (jstring)(env)->GetObjectField  (obj, pathID);
  const TCHAR *workingDirectory    = (env)->GET_STRING_CHARS        (j_workingDirectory, 0);

  hres = p_shellLink [handle]->SetWorkingDirectory (workingDirectory);

  (env)->RELEASE_STRING_CHARS(j_workingDirectory, workingDirectory);

  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}

// --------------------------------------------------------------------------
// This function saves the shell link.
//
// name - the fully qualified path for saving the shortcut.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_saveLink (JNIEnv  *env,
                                                                           jobject  obj,
                                                                           jstring  name)
{
  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ----------------------------------------------------
  // Query IShellLink for the IPersistFile interface for
  // saving the shell link in persistent storage.
  // ----------------------------------------------------
  IPersistFile* p_persistFile;
  HRESULT       hres = p_shellLink [handle]->QueryInterface (IID_IPersistFile,
                                                             (void **)&p_persistFile);

  if (!SUCCEEDED (hres))
  {
    return (SL_NO_IPERSIST);
  }

  // ----------------------------------------------------
  // convert from Java string type
  // ----------------------------------------------------
  const unsigned short *pathName = (env)->GetStringChars (name, 0);
  
  // ----------------------------------------------------
  // Save the link
  // ----------------------------------------------------
  hres = p_persistFile->Save   ((wchar_t*)pathName, FALSE);
  p_persistFile->SaveCompleted ((wchar_t*)pathName);
  
  // ----------------------------------------------------
  // Release the pointer to IPersistFile
  // and the string object
  // ----------------------------------------------------
  p_persistFile->Release ();
  (env)->ReleaseStringChars (name, pathName);

  // ------------------------------------------------------
  // return success code
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_NO_SAVE);
  }
}

// --------------------------------------------------------------------------
// This function loads a shell link.
//
// name - the fully qualified path for loading the shortcut.
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_loadLink (JNIEnv  *env,
                                                                           jobject  obj,
                                                                           jstring  name)
{
  HRESULT     hres;

  // Get the handle from the Java side
  jclass      cls       = (env)->GetObjectClass  (obj);
  jfieldID    handleID  = (env)->GetFieldID      (cls, "nativeHandle", "I");
  jint        handle    = (env)->GetIntField     (obj, handleID);

  // ----------------------------------------------------
  // Query IShellLink for the IPersistFile interface for
  // saving the shell link in persistent storage.
  // ----------------------------------------------------
  IPersistFile* p_persistFile;
  hres = p_shellLink [handle]->QueryInterface (IID_IPersistFile,
                                               (void **)&p_persistFile);

  if (SUCCEEDED (hres))
  {
    // convert from Java string type
    const unsigned short *pathName = (env)->GetStringChars (name, 0);

    // --------------------------------------------------
    // Load the link
    // --------------------------------------------------
    hres = p_persistFile->Load ((wchar_t *)pathName,
                                STGM_DIRECT    |
                                STGM_READWRITE |
                                STGM_SHARE_EXCLUSIVE);

    // --------------------------------------------------
    // Release the pointer to IPersistFile
    // --------------------------------------------------
    p_persistFile->Release ();
    (env)->ReleaseStringChars (name, pathName);
  }

  // ------------------------------------------------------
  // return success code
  // ------------------------------------------------------
  if (SUCCEEDED (hres))
  {
    return (SL_OK);
  }
  else
  {
    return (SL_ERROR);
  }
}


// --------------------------------------------------------------------------
// resolves a Windows Standard path using SHGetPathFromIDList
// inputs:
//  inc iCsidl - one of the CSIDL
//    valid  	CSIDL_COMMON_DESKTOPDIRECTORY
//				CSIDL_COMMON_STARTMENU	
//				CSIDL_COMMON_PROGRAMS
//				CSIDL_COMMON_STARTUP
//           	CSIDL_DESKTOPDIRECTORY
//				CSIDL_STARTMENU	
//				CSIDL_PROGRAMS
//				CSIDL_STARTUP
// returns:
//   the Windows Standard Path in szPath.
// --------------------------------------------------------------------------
LONG GetLinkPath( int iCsidl, LPTSTR szPath )
{
    HRESULT hr;

    // Allocate a pointer to an Item ID list
    LPITEMIDLIST pidl;

    // Get a pointer to an item ID list that
    // represents the path of a special folder
    hr = SHGetSpecialFolderLocation(NULL, iCsidl, &pidl);

    if ( SUCCEEDED(hr) )
    {
        // Convert the item ID list's binary
        // representation into a file system path
        BOOL f = SHGetPathFromIDList(pidl, szPath);

        // Allocate a pointer to an IMalloc interface
        LPMALLOC pMalloc;

        // Get the address of our task allocator's IMalloc interface
        hr = SHGetMalloc(&pMalloc);

        // Free the item ID list allocated by SHGetSpecialFolderLocation
        pMalloc->Free(pidl);

        // Free our task allocator
        pMalloc->Release();

		if ( f == FALSE )
		{
		    *szPath = TCHAR('\0');
			return E_FAIL;
		}

        // return the special folder's path (contained in szPath)
        return S_OK;
    }
	else
	{
		// null path for error return.
		*szPath = TCHAR('\0');
	}

	return E_FAIL;
}

// --------------------------------------------------------------------------
// This function retrieves the location of the folders that hold shortcuts.
// The information comes from SHGetSpecialFolderLocation, 
// since it's more accurate.
//  SHGetSpecialFolderLocation (since shell32.dll ver 4.0 - win 95, IE 3).
//
// target   - where the path should point. The following are legal values
//            to use
//
//            1 - path for shortcuts that show on the desktop
//            2 - path for shortcuts that show in the Programs menu
//            3 - path for shortcuts that show in the start menu
//            4 - path to the Startup group. These shortcuts are executed
//                at OS launch time
//
//            Note: all other values cause an empty string to be returned
//
// Program groups (sub-menus) in the programs and start menus can be created
// by creating a new folder at the indicated location and placing the links
// in that folder. These folders can be nested to any depth with each level
// creating an additional menu level.
//
// Results are deposited in 'currentUserLinkPath' and 'allUsersLinkPath' 
// respectively
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_ShellLink_GetFullLinkPath
  (JNIEnv *env, jobject obj, jint utype, jint ltype)
{
  ULONG   ul_size = MAX_PATH;       // buffer size
  TCHAR   szPath [MAX_PATH];        // path we are looking for 
  int	  csidl;
  jclass	cls;
  jfieldID  pathID;
  jstring   j_path;
  LONG 		successCode;
  
  if ((ltype > MIN_KEY) && (ltype < MAX_KEY))
  {
	//translate request into a CSIDL, based on user-type and link-type

	// user type
	if ( utype == com_izforge_izpack_util_os_ShellLink_ALL_USERS )
	{
		switch ( ltype )		// link type
		{
			case ( com_izforge_izpack_util_os_ShellLink_DESKTOP ) :
			csidl = CSIDL_COMMON_DESKTOPDIRECTORY;
			break;

			case ( com_izforge_izpack_util_os_ShellLink_START_MENU ) :
			csidl = CSIDL_COMMON_STARTMENU;
			break;

			case ( com_izforge_izpack_util_os_ShellLink_PROGRAM_MENU ) :
			csidl = CSIDL_COMMON_PROGRAMS;
			break;

			case ( com_izforge_izpack_util_os_ShellLink_STARTUP ) :
			csidl = CSIDL_COMMON_STARTUP;
			break;

			default :
			break;
		}

		successCode = GetLinkPath( csidl, szPath );

		if ( SUCCEEDED(successCode) )
		{

			// ------------------------------------------------------
			// set the member variables
			// ------------------------------------------------------
			cls    = (env)->GetObjectClass (obj);
			pathID = (env)->GetFieldID     (cls, "allUsersLinkPath", "Ljava/lang/String;");
			j_path = (env)->NEW_STRING   (szPath);

			(env)->SetObjectField (obj, pathID, j_path);
			return (SL_OK);
		}
		else
		{
			// failure code from GetLinkPath()
			return successCode;
		}
	}
	else if ( utype == com_izforge_izpack_util_os_ShellLink_CURRENT_USER )
	{
		switch ( ltype )		// link type
		{
			case ( com_izforge_izpack_util_os_ShellLink_DESKTOP ) :
			csidl = CSIDL_DESKTOPDIRECTORY;
			break;

			case ( com_izforge_izpack_util_os_ShellLink_START_MENU ) :
			csidl = CSIDL_STARTMENU;
			break;

			case ( com_izforge_izpack_util_os_ShellLink_PROGRAM_MENU ) :
			csidl = CSIDL_PROGRAMS;
			break;

			case ( com_izforge_izpack_util_os_ShellLink_STARTUP ) :
			csidl = CSIDL_STARTUP;
			break;

			default :
			break;
		}

		successCode = GetLinkPath( csidl, szPath );

		if ( SUCCEEDED(successCode) )
		{
			// ------------------------------------------------------
			// set the member variables
			// ------------------------------------------------------
			cls    = (env)->GetObjectClass (obj);
			pathID = (env)->GetFieldID     (cls, "currentUserLinkPath", "Ljava/lang/String;");
			j_path = (env)->NEW_STRING   (szPath);

			(env)->SetObjectField (obj, pathID, j_path);

			return (SL_OK);
		}
		else
		{
			// failure code from GetLinkPath()
			return successCode;
		}
	}

  }

  return SL_CAN_NOT_READ_PATH;
}

