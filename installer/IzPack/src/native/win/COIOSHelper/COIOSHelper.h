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


#include <jni.h>
#include "WinLibEnv.h"

// Defines
// shortcut for set a error value to WinLibEnv and break.

#define ERROR_BREAK( err, acceptor ) { acceptor->setError(err); break; }
// shortcut for set a error value to WinLibEnv with verifying the system error and break.
#define ERROR_BREAK_SYS( err, acceptor ) { acceptor->setErrorWithOS(err); break; }
#define ERROR_BREAK_CODE( err, acceptor, code ) { acceptor->setError(err, code); break; }
#define ERROR_BREAK_CODE_A1( err, arg1 , acceptor, code) \
	{ acceptor->setError(err, code); acceptor->addArg(arg1); break; }
#define ERROR_BREAK_CODE_A2( err, arg1, arg2, acceptor, code ) \
	{ acceptor->setError(err, code); acceptor->addArgs(arg1, arg2); break; }
#define ERROR_BREAK_CODE_A3( err, arg1, arg2, arg3, acceptor, code ) \
	{ acceptor->setError(err, code); acceptor->addArgs(arg1, arg2, arg3); break; }

#define ERROR_BREAK_VAR( err, acceptor, exName ) { acceptor->setError(err, exName); break; }


#define LOOK_OS()	if( ! _isNT4orHigher() ) { lastInternError = 8; return( -8 ); }
#define STRING_INIT "not found"

#define	MAX_ERROR	50
#ifndef MAX_NAME_LEN
#define MAX_NAME_LEN	256
#endif


// in RegistryInternal.c

extern jboolean regKeyExist(WinLibEnv *libEnv, int root, const TCHAR *key );
extern void setRegValue(WinLibEnv *libEnv, int root, const TCHAR *key, 
	const TCHAR *value, jint type, LPBYTE contents, jint length );
extern void createRegKey(WinLibEnv *libEnv, int root, const TCHAR *key );
extern jint getRegValueType( WinLibEnv *libEnv, int root, const TCHAR *key , const TCHAR *value );
extern LPBYTE getRegValue( WinLibEnv *libEnv, int root, const TCHAR *key , const TCHAR *value, DWORD *type, DWORD *length);
extern void deleteRegValue(WinLibEnv *libEnv, int root, const TCHAR *key, const TCHAR *value );
extern void deleteRegKey(WinLibEnv *libEnv, int root, const TCHAR *key );
extern jboolean isKeyEmpty(WinLibEnv *libEnv, int root, const TCHAR *key );
extern void determineCounts( WinLibEnv *libEnv, int root, const TCHAR *key, DWORD *subkeys, DWORD *values );
extern TCHAR *getSubkeyName( WinLibEnv *libEnv, int root, const TCHAR *key , int valueId );
extern TCHAR *getValueName( WinLibEnv *libEnv, int root, const TCHAR *key , int keyId );
extern int getValueNames( WinLibEnv *libEnv, int root, const TCHAR *key , TCHAR ***names  );
extern int getSubkeyNames( WinLibEnv *libEnv, int root, const TCHAR *key , TCHAR ***names  );








