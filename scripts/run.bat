@echo off
:: Uncomment the line for the mode you wish to run.  Comment all other lines.
:: Adjust java_path to javaw.exe as required for your Java system installation,
:: or if you wish to run RomRaider on a different version of Java.
:: Note: an installation specific settings.xml file can be saved to the same
:: directory as this run.bat to keep it separate from an installed version of
:: RomRaider.
:: If the path has spaces in it make sure it is within the quotes of java_path.
:: End the path with a \ directory separator.
set "java_path="

:: Start Editor with console redirected to %HOMEPATH%\.RomRaider\romraider_sout.log
start "Editor" /NORMAL "%java_path%javaw.exe" -Djava.library.path=lib/windows/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar

:: Start Logger
rem start "Logger" /NORMAL "%java_path%javaw.exe" -Djava.library.path=lib/windows/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1

:: Start Logger in full screen mode
rem start "Logger Full Screen" /NORMAL "%java_path%javaw.exe" -Djava.library.path=lib/windows/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger.fullscreen 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1

:: Start Logger in Touch screen mode
rem start "Logger Touch Screen" /NORMAL "%java_path%javaw.exe" -Djava.library.path=lib/windows/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger.touch 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1
