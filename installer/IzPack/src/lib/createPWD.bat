
rem Script for generating Password class.
rem created on July 2005
rem @author Fabrice Mirabile


@echo off
rem %1 = file name of your own choice

IF !%1==! goto USAGE
javac .\com\izforge\izpack\sample\*.java
jar cf %1 .\com\izforge\izpack\sample\*.class 
goto ok

:USAGE
echo "USAGE: %0 <Jar_File_Name.jar>"
goto exit

:ok
echo "Success: jar %1 has been created."

:exit