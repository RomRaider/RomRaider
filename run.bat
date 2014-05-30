:: Uncomment the line for the mode you wish to run.  Comment all other lines.
:: Adjust path to javaw.exe as required for your Java system installation
rem start javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar 1>>"%HOMEPATH%\.RomRaider\romraider_sout.log" 2>&1
:: Start Editor
start javaw.exe -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar
:: Start Logger in full screen mode
rem start javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger.fullscreen
:: Start Logger
rem start javaw -Djava.library.path=lib/windows -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=true -Xms64M -Xmx512M -jar RomRaider.jar -logger
