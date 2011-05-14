#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
    echo JAVA_HOME not set.
    exit 1
fi

$JAVA_HOME/bin/java -Djava.library.path=lib/linux -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Dsun.java2d.d3d=false -Xms64M -Xmx512M -XX:-UseParallelGC -XX:CompileThreshold=10000 -jar RomRaider.jar >> romraider_sout.log 2>&1

exit 0
