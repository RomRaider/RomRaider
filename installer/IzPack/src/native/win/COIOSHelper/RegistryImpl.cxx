/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005-2006 Klaus Bartz
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
#include "com_coi_tools_os_win_RegistryImpl.h"
#include <windows.h>
#include "COIOSHelper.h"


// --------------------------------------------------------------------------
// A "lookup table" to  get signature for the methods of RegDataContainer
// --------------------------------------------------------------------------
static char *TYPE_INIT_SIGNATURE_TABLE[] =
{
	"",							// REG_NONE
	"(Ljava/lang/String;)V",	// REG_SZ
	"(Ljava/lang/String;)V",	// REG_EXPAND_SZ
	"([B)V",					// REG_BINARY
	"(J)V",						// REG_DWORD
	"",							// REG_DWORD_MSB
	"",							// REG_LINK
	"([Ljava/lang/String;)V"	// REG_MULTI_SZ
	
};

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    exist
 * Signature: (ILjava/lang/String;)Z
 * Returns whether a registry key exist or not.
 */
JNIEXPORT jboolean JNICALL Java_com_coi_tools_os_win_RegistryImpl_exist
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jboolean  retval = false;
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	retval = regKeyExist(&libEnv,  jRoot, key );

	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	return( retval  );
	
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    createKeyN
 * Signature: (ILjava/lang/String;)V
 * Creates the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_createKeyN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	createRegKey(&libEnv,  jRoot, key );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    setValueN
 * Signature: (ILjava/lang/String;Ljava/lang/String;Lcom/coi/tools/os/win/RegDataContainer;)V
 * Sets the given value into the given RegDataContainer . On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_setValueN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue, jobject jContents)
{
	WinLibEnv libEnv( env, obj );
	WinLibEnv *plibEnv = &libEnv;
	const TCHAR *key = NULL;
	if( libEnv.verifyNullObjects( jValue, jContents ))
	{
		if( jKey != NULL )
			key = env->GET_STRING_CHARS( jKey , 0);
		const TCHAR *value = env->GET_STRING_CHARS( jValue , 0);
		const TCHAR *buf = NULL;
		jint type	= 0;
				int i;
		LPBYTE contents = NULL;
		DWORD ddata = 0;
		DWORD length = 0;
		jstring  jStr = NULL;
		jbyteArray jBin = NULL;
		jobjectArray jObj = NULL;
		jboolean isCopy = false;
		jstring *jMultiStr = NULL;
		jint multiSize = 0;
		const TCHAR ** intermedMulti = NULL;;
		while( 1 )
		{
			jmethodID	mid = NULL;
			jclass clazz	= env->GetObjectClass( jContents );
			if( clazz == NULL )	// oops, class not bound ??
				ERROR_BREAK(_T("registry.MissingRegDataContainer"), plibEnv);
			if( (mid = env->GetMethodID( clazz, "getType", "()I" ) ) == NULL )
				ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
			type = env->CallIntMethod( jContents, mid );
			switch( type )
			{
			case REG_DWORD:
				if( (mid = env->GetMethodID( clazz, "getDwordData", "()J" ) ) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				ddata  = (DWORD) env->CallLongMethod( jContents, mid );
				contents = (LPBYTE) &ddata;
				length = 4;
				break;
			case REG_SZ:
			case REG_EXPAND_SZ:
				if( (mid = env->GetMethodID( clazz, "getStringData", "()Ljava/lang/String;" ) ) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				if( (jStr = (jstring) env->CallObjectMethod( jContents, mid )) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				buf = env->GET_STRING_CHARS( jStr , 0);
				contents = (LPBYTE) buf;
				length	= (_tcslen( buf) + 1) * sizeof(TCHAR);
				break;
			case REG_BINARY:
				if( (mid = env->GetMethodID( clazz, "getBinData", "()[B" ) ) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				if( (jBin = (jbyteArray) env->CallObjectMethod( jContents, mid )) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				contents = (LPBYTE) env->GetByteArrayElements( jBin, &isCopy );
				length	= (DWORD) env->GetArrayLength( jBin );
				break;
			case REG_MULTI_SZ:
				if( (mid = env->GetMethodID( clazz, "getMultiStringData", "()[Ljava/lang/String;" ) ) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				if( (jObj = (jobjectArray) env->CallObjectMethod( jContents, mid)) == NULL )
					ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);
				multiSize = env->GetArrayLength( jObj );
				LPBYTE pos;
				jMultiStr = new jstring[multiSize];
				intermedMulti = new const TCHAR *[multiSize];
				length = 2 ;
				for( i = 0; i < multiSize; ++i )
				{
					jMultiStr[i] = (jstring) env->GetObjectArrayElement( jObj, i );
					intermedMulti[i] = env->GET_STRING_CHARS( jMultiStr[i], 0);
					length += _tcslen( intermedMulti[i] ) + 1 ;
				}
				pos = contents = (LPBYTE) new TCHAR[length];
				int monoLength; 
				for( i = 0; i < multiSize; ++i )
				{
					monoLength = (_tcslen(intermedMulti[i] ) + 1) * sizeof(TCHAR);
					memcpy( pos, intermedMulti[i], monoLength);
					pos += monoLength;
				}
				*pos++	= 0;
				*pos	= 0;
				length *= sizeof(TCHAR);

				break;
			default:
				ERROR_BREAK(_T("registry.DataTypeNotSupported"), plibEnv);

			}
			if( ! libEnv.good() )	// Today not necessary, but may be someone add a line ...
				break;
			break;
		}
		if( libEnv.good() )
			setRegValue(plibEnv, jRoot, key, value, type, contents, length );

		if( buf )
			env->RELEASE_STRING_CHARS( jStr, buf);
		if( jBin )
		{
			env->ReleaseByteArrayElements( jBin, (jbyte *) contents, JNI_ABORT  );
		}
		if( jMultiStr )
		{
			for( i = 0; i < multiSize; ++i )
			{
				env->RELEASE_STRING_CHARS(jMultiStr[i], intermedMulti[i]);
				env->DeleteLocalRef( jMultiStr[i] );
			}
			delete [] jMultiStr;
			delete [] intermedMulti;
			delete contents;

		}
		if( key != NULL )
			env->RELEASE_STRING_CHARS( jKey, key);
		env->RELEASE_STRING_CHARS( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
	return;
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getValueType
 * Signature: (ILjava/lang/String;Ljava/lang/String;)I
 * Returns the value type of the given value. On error a NativeLibException will be thrown.
 */
JNIEXPORT jint JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueType
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue)
{
	WinLibEnv libEnv( env, obj );
	jint  retval = 0;
	const TCHAR *key = NULL;
	if( libEnv.verifyNullObjects( jValue ))
	{
		if( jKey != NULL )
			key = env->GET_STRING_CHARS( jKey , 0);
		const TCHAR *value = env->GET_STRING_CHARS( jValue , 0);
		retval = getRegValueType(&libEnv,  jRoot, key, value );
		if( key != NULL )
			env->RELEASE_STRING_CHARS( jKey, key);
		env->RELEASE_STRING_CHARS( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getValue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)Lcom/coi/tools/os/win/RegDataContainer;
 * Returns the contents of the given value. On error a NativeLibException will be thrown.
 */
JNIEXPORT jobject JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValue
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue)
{
	WinLibEnv libEnv( env, obj );
	WinLibEnv *plibEnv = &libEnv;
	jobject retval = NULL;
	if( libEnv.verifyNullObjects( jValue ))
	{
		const TCHAR *key = NULL;
		const TCHAR *value = env->GET_STRING_CHARS( jValue , 0);
		if(  jKey != NULL )
			key = env->GET_STRING_CHARS( jKey , 0);
		DWORD type;
		DWORD length;

		LPBYTE winData = getRegValue(&libEnv,  jRoot, key, value , &type, &length);

		while( libEnv.good() )
		{
			jmethodID	mid = NULL;
			jclass 		clazz = NULL;
			if( (clazz = env->FindClass("com/coi/tools/os/win/RegDataContainer") ) == NULL )
				ERROR_BREAK(_T("registry.MissingRegDataContainer"), plibEnv);
			if( strlen( TYPE_INIT_SIGNATURE_TABLE[type] ) < 1 )
				ERROR_BREAK(_T("registry.UnsupportedDataType"), plibEnv);
			if( (mid = env->GetMethodID( clazz, "<init>", TYPE_INIT_SIGNATURE_TABLE[type]  ) ) == NULL )
				ERROR_BREAK(_T("registry.MalformedRegDataContainer"), plibEnv);

			switch( type )
			{
			case REG_DWORD:
				{	// A simple (jlong) *winData will only contains the value of the first byte.
					LPDWORD tDW = (LPDWORD) winData;
					retval =  env->NewObject( clazz, mid,  (jlong) *tDW ); 
				}
				break;
			case REG_SZ:
			case REG_EXPAND_SZ:
				retval = env->NewObject( clazz, mid,  env->NEW_STRING( (TCHAR *) winData) ); 
				break;
			case REG_BINARY:
				{
					jbyteArray jba = env->NewByteArray( length );
					env->SetByteArrayRegion( jba, 0, length , (jbyte *) winData );
					retval = env->NewObject( clazz, mid,  jba ); 
				}
				break;
			case REG_MULTI_SZ:
				{
					TCHAR *pos = (TCHAR *) winData;
					int count = 0;
					jclass	claString;
					while( *pos )
					{
						count++;
						pos += _tcslen(pos) + 1;
					}
					if( (claString = env->FindClass("java/lang/String") ) == NULL )
						ERROR_BREAK(_T("registry.StringClassNotFound"), plibEnv);
					jobjectArray joa = env->NewObjectArray( count, claString, 0 );
					pos = (TCHAR *) winData;
					for( int i = 0; i < count; ++i )
					{
						env->SetObjectArrayElement( joa, i, env->NEW_STRING( pos ) );
						pos += _tcslen(pos) + 1;
					}
					retval = env->NewObject( clazz, mid,  joa ); 
				}
				break;
			}
			if( ! libEnv.good() )	// Today not necessary, but may be someone add a line ...
				break;
			break;
		}
		if( winData )
			delete [] winData;
		if( key != NULL )
			env->RELEASE_STRING_CHARS( jKey, key);
		env->RELEASE_STRING_CHARS( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
	return( retval );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    deleteValueN
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 * Deletes the given value. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_deleteValueN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jstring jValue)
{
	WinLibEnv libEnv( env, obj );
	if( libEnv.verifyNullObjects(jValue ))
	{
		const TCHAR *key = NULL;
		const TCHAR *value = env->GET_STRING_CHARS( jValue , 0);
		if( jKey != NULL) 
			key = env->GET_STRING_CHARS( jKey , 0);
		if( key == NULL || _tcslen(key) == 0 )
			deleteRegValue(&libEnv,  jRoot, NULL, value );
		else
			deleteRegValue(&libEnv,  jRoot, key, value );
		if( key != NULL)	// Release only if get performed.
			env->RELEASE_STRING_CHARS( jKey, key);
		env->RELEASE_STRING_CHARS( jValue, value);
	}
	libEnv.verifyAndThrowAtError();
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    deleteKeyN
 * Signature: (ILjava/lang/String;)V
 * Deletes the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_deleteKeyN
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	deleteRegKey(&libEnv,  jRoot, key );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    isKeyEmpty
 * Signature: (ILjava/lang/String;)Z
 * Returns whether the given key is empty or not. On error a NativeLibException will be thrown.
 */
JNIEXPORT jboolean JNICALL Java_com_coi_tools_os_win_RegistryImpl_isKeyEmpty
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jboolean retval = false;
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	retval = isKeyEmpty(&libEnv,  jRoot, key );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	return( retval );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyCount
 * Signature: (ILjava/lang/String;)I
 * Returns how much subkeys are under the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT jint JNICALL Java_com_coi_tools_os_win_RegistryImpl_getSubkeyCount
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	DWORD	subkeys = 0;
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	DWORD	values = 1;

	determineCounts(&libEnv,  jRoot, key, &subkeys, &values  );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	return( subkeys );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyCount
 * Signature: (ILjava/lang/String;)I
 * Returns how much values are under the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT jint JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueCount
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	DWORD	values = 0;
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	DWORD	subkeys = 1;

	determineCounts(&libEnv,  jRoot, key, &subkeys, &values  );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	return( values );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyName
 * Signature: (ILjava/lang/String;I)Ljava/lang/String;
 * Returns the name of the subkey of the given key identified with the id.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jstring JNICALL Java_com_coi_tools_os_win_RegistryImpl_getSubkeyName
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jint jKeyId)
{
	jstring	retval = NULL;
	WinLibEnv libEnv( env, obj );
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	TCHAR *name = getSubkeyName(&libEnv,  jRoot, key, jKeyId  );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	if( libEnv.good() )	
		retval = env->NEW_STRING( name );
	if( name )
		delete [] name;
	libEnv.verifyAndThrowAtError();
	return( retval );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyName
 * Signature: (ILjava/lang/String;I)Ljava/lang/String;
 * Returns the name of the value of the given key identified with the id.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jstring JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueName
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jint jKeyId)
{
	jstring	retval = NULL;
	WinLibEnv libEnv( env, obj );
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	TCHAR *name = getValueName(&libEnv,  jRoot, key, jKeyId  );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	if( libEnv.good() )	
		retval = env->NEW_STRING( name );
	if( name )
		delete [] name;
	libEnv.verifyAndThrowAtError();
	return( retval );
}


/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getSubkeyNames
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 * Returns the names of all subkeys of the given key.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jobjectArray JNICALL Java_com_coi_tools_os_win_RegistryImpl_getSubkeyNames
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jobjectArray  newArr = NULL;
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	jclass clazz;
	TCHAR **names;
	LONG length = 0;
	jstring utf_str;
	int i;
	clazz = env->FindClass( "java/lang/String");
	if( ( length = getSubkeyNames( &libEnv, jRoot, key, &names ) ) > 0 )
	{
		newArr = env->NewObjectArray( length, clazz, NULL);
		for( i = 0; i < length; ++i )
		{
			utf_str = env->NEW_STRING( names[i] );
			env->SetObjectArrayElement( newArr, i, utf_str);
			env->DeleteLocalRef( utf_str );
			delete [] names[i];
			//LocalFree( groups[i] );
			names[i] = NULL;
		}
		delete [] names;
		//LocalFree( groups );
	}
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	if( libEnv.good() )	
		return( newArr );
	return( NULL );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getValueNames
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 * Returns the names of all values under the the given key.
 * On error a NativeLibException will be thrown.
 */
JNIEXPORT jobjectArray JNICALL Java_com_coi_tools_os_win_RegistryImpl_getValueNames
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	jobjectArray  newArr = NULL;
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	jclass clazz;
	TCHAR **names;
	LONG length = 0;
	jstring utf_str;
	int i;
	clazz = env->FindClass( "java/lang/String");
	if( ( length = getValueNames( &libEnv, jRoot, key, &names ) ) > 0 )
	{
		newArr = env->NewObjectArray( length, clazz, NULL);
		for( i = 0; i < length; ++i )
		{
			utf_str = env->NEW_STRING( names[i] );
			env->SetObjectArrayElement( newArr, i, utf_str);
			env->DeleteLocalRef( utf_str );
			delete [] names[i];
			//LocalFree( groups[i] );
			names[i] = NULL;
		}
		delete [] names;
		//LocalFree( groups );
	}
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	if( libEnv.good() )	
		return( newArr );
	return( NULL );
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    getKeyACL
 * Signature: (ILjava/lang/String;)Lcom/coi/tools/os/win/AccessControlList;
 * Returns the ACL associated with the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT jobject JNICALL Java_com_coi_tools_os_win_RegistryImpl_getKeyACL
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey)
{
	WinLibEnv libEnv( env, obj );
	const TCHAR *key = NULL;
	if( jKey != NULL) 
		key = env->GET_STRING_CHARS( jKey , 0);
	createRegKey(&libEnv,  jRoot, key );
	if( key != NULL )
		env->RELEASE_STRING_CHARS( jKey, key);
	libEnv.verifyAndThrowAtError();
	return(NULL);
}

/*
 * Class:     com_coi_tools_os_win_RegistryImpl
 * Method:    modifyKeyACL
 * Signature: (ILjava/lang/String;Ljava/lang/String;Lcom/coi/tools/os/win/AccessControlList;)V
 * Modifies the ACL which is associated to the given key. On error a NativeLibException will be thrown.
 */
JNIEXPORT void JNICALL Java_com_coi_tools_os_win_RegistryImpl_modifyKeyACL
	(JNIEnv *env, jobject obj, jint jRoot, jstring jKey, jobject jContents )

{
	WinLibEnv libEnv( env, obj );
	WinLibEnv *plibEnv = &libEnv;
	while( libEnv.good())
	{
		ERROR_BREAK(_T("registry.ACLNotSupported"), plibEnv);
	}
	libEnv.verifyAndThrowAtError();
}
