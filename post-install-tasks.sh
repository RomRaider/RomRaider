#!/bin/bash
if [ ! -d "$HOME/.romraider" ]
then
	mkdir "$HOME/.romraider"
fi
if [ ! -e "$HOME/.romraider/settings.xml" ]
then
	if [ -e $1/settings.xml ]
	then
		mv -f $1/settings.xml "$HOME/.romraider/"
		mv -f $1/profile_backup.xml "$HOME/.romraider/"
	fi
fi
rm -f $1/rr_system.log $1/romraider.log > nul 2>&1
exit 0
