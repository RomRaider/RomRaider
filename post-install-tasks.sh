#!/bin/bash
if [ ! -d "$HOME/.RomRaider" ]
then
	mkdir "$HOME/.RomRaider"
fi
if [ ! -e "$HOME/.RomRaider/settings.xml" ]
then
	if [ -e $1/settings.xml ]
	then
		mv -f $1/settings.xml "$HOME/.RomRaider/"
		mv -f $1/profile_backup.xml "$HOME/.RomRaider/"
	fi
fi
rm -f $1/rr_system.log $1/romraider.log > nul 2>&1
exit 0
