@echo off
color f0

cd %~dp0


echo Make project in:
cd


set ShellLinkMak=ShellLink.mak

echo TODO if fail: Adjust JAVA_HOME in %CD%\%ShellLinkMak%

echo.

echo using C:\Program Files\Microsoft Visual Studio\VC98\Bin
echo  TODO if fail: adjust the path if not in $path
echo.

set MSVCDIR=C:\Program Files\Microsoft Visual Studio

SET INCLUDE=%MSVCDIR%\VC98\Include
SET LIB=%MSVCDIR%\VC98\Lib

set PATH=%MSVCDIR%\Common\MSDev98\Bin;%MSVCDIR%\VC98\Bin;%PATH%

echo PATH=%PATH%

echo.
echo cleaning up...

nmake /f %ShellLinkMak% CFG="ShellLink - Win32 Release" CLEAN

echo.
echo (Re-)Compiling...


nmake /f %ShellLinkMak% CFG="ShellLink - Win32 Release"

echo.
echo cleaning up...

nmake /f %ShellLinkMak% CFG="ShellLink - Win32 Release Unicode" CLEAN

echo.
echo (Re-)Compiling...


nmake /f %ShellLinkMak% CFG="ShellLink - Win32 Release Unicode"

echo.
echo Pause, to check the current build
echo.
pause
