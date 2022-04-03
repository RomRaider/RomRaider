:: Uncomment the line for the mode you wish to run.  Comment all other lines.
:: Adjust path to javaw.exe as required for your Java system installation
set java_path=
:: Start Editor with console redirected to %HOMEPATH%\.RomRaider\romraider_sout.log
start %java_path%javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1

:: Start Logger
rem start %java_path%javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1

:: Start Logger in full screen mode
rem start %java_path%javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger.fullscreen 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1

:: Start Logger in Touch screen mode
rem start %java_path%javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger.touch 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1
