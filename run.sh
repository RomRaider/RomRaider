#!/bin/bash

java -Djava.library.path=lib/linux -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=false -Xms32M -Xmx384M -XX:-UseParallelGC -XX:CompileThreshold=10000 -jar RomRaider.jar >> romraider_sout.log 2>&1

exit 0