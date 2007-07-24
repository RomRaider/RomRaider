#ifndef WINLIBENV_H
#define  WINLIBENV_H

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


#define STD_ARRAY_LENGTH 16

enum WinLibEnvStatus_e
{
	WLES_UNKNOWN = 0,
	WLES_INITIALIZED,
	WLES_OK,
	WLES_WARNING,
	WLES_ERROR
};

class ExceptionNameRecord
{
	private:
		char	*shortName;
		char	*longName;
		int		signatureId;
		int		typeId;
	
	public:
		char	*getShortName() { return(shortName);};
		char	*getLongName() { return(longName);};
		int		getSignatureId() { return( signatureId);};
		int		getTypeId() { return( typeId );};
		ExceptionNameRecord( char *sn, char *ln, int sid, int tid ) {shortName = sn; longName = ln; signatureId = sid; typeId = tid;};
		~ExceptionNameRecord( ) {};
	
};

class WinLibEnv
{
	private:
		static char *ExceptionSignatureMap[];
		static ExceptionNameRecord ExceptionNameMap[];
		unsigned long	win32Error;
		unsigned long	externCode;
		TCHAR	*win32ErrorText;
		int		winLibError;
		TCHAR	*winLibErrorText;
		JNIEnv 	*jniEnv;
		jobject jniObj;
		WinLibEnvStatus_e	status;
		char	*exceptionTypeName;
		TCHAR    *args[STD_ARRAY_LENGTH];
		int		currentArg;
		void getOSMessage();
		ExceptionNameRecord *getExceptionNameRecord( char *exName );	
		void	initialize();
	protected:
	public:
		WinLibEnv(JNIEnv *env, jobject obj);
		virtual ~WinLibEnv();
		jboolean	good() { return(status < WLES_WARNING ? true : false);};
		JNIEnv *getJNIEnv() { return(jniEnv);};

		void	setError( TCHAR *err, char *errType);
		void	setError( TCHAR *err) { setError(err, ExceptionNameMap[1].getShortName());};
		void	setError( TCHAR *err,  unsigned long errCode) { setError( err ); win32Error = errCode;};
		void	setError( int err) 
			{ winLibError = err; status = WLES_ERROR;exceptionTypeName = ExceptionNameMap[1].getShortName();};
		
		void	setErrorWithOS( int err ) { setError(err); win32Error = GetLastError();};
		void	setErrorWithOS( TCHAR *err) { setErrorWithOS( err, ExceptionNameMap[1].getShortName() );};
		void	setErrorWithOS( TCHAR *err,  char *errType) 
			{ setError( err, errType );win32Error = GetLastError();};

		void	addArg( const TCHAR *arg1 );
		void	addArgs( const TCHAR *arg1, const TCHAR *arg2) { addArg(arg1); addArg(arg2); };
		void	addArgs( const TCHAR *arg1, const TCHAR *arg2, const TCHAR *arg3) { addArgs(arg1, arg2); addArg(arg3); };
		void	addArgs( const TCHAR *arg1, const TCHAR *arg2, const TCHAR *arg3, const TCHAR *arg4) 
			{ addArgs(arg1, arg2); addArgs(arg3, arg4); };

		void	reset();
		WinLibEnv *clone();
		void takeAcross( WinLibEnv *from);

		jboolean	verifyAndThrowAtError();
		jboolean	verifyNullObjects(jobject obj1, jobject obj2, jobject obj3, jobject obj4);
		jboolean	verifyNullObjects(jobject obj1, jobject obj2, jobject obj3)
			{ return( verifyNullObjects( obj1, obj2, obj3, (jobject) 47 ));};
		jboolean	verifyNullObjects(jobject obj1, jobject obj2)
			{ return( verifyNullObjects( obj1, obj2,  (jobject) 47, (jobject) 47 ));};
		jboolean	verifyNullObjects(jobject obj1)
			{ return( verifyNullObjects( obj1,  (jobject) 47, (jobject) 47, (jobject) 47 ));};
		
};
#endif
