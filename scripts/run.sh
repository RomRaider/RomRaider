#!/bin/bash

# To change your ZIP install startup preference, uncomment the preferred line.
# Make sure only one line is uncommented
# Java is expected to be available on your PATH

java -Djava.library.path=lib/linux/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=false -Xms64M -Xmx512M -XX:-UseParallelGC -XX:CompileThreshold=10000 -jar RomRaider.jar >> "$HOME/.RomRaider/romraider_sout.log" 2>&1
#java -Djava.library.path=lib/linux/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=false -Xms64M -Xmx512M -XX:-UseParallelGC -XX:CompileThreshold=10000 -jar RomRaider.jar -logger >> "$HOME/.RomRaider/romraider_sout.log" 2>&1
#java -Djava.library.path=lib/linux/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=false -Xms64M -Xmx512M -XX:-UseParallelGC -XX:CompileThreshold=10000 -jar RomRaider.jar -logger.fullscreen >> "$HOME/.RomRaider/romraider_sout.log" 2>&1
#java -Djava.library.path=lib/linux/32 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=false -Xms64M -Xmx512M -XX:-UseParallelGC -XX:CompileThreshold=10000 -jar RomRaider.jar -logger.touch >> "$HOME/.RomRaider/romraider_sout.log" 2>&1

exit 0