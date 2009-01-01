#!/bin/bash
#
# Embeds the Qt libraries inside the application bundle, allowing it to be redistributed
# to any Mac without Qt installed.
# 
# -- Julien Ponge <julien@izforge.com>

mkdir launcher.app/Contents/Frameworks
cp -R $1/QtCore.framework launcher.app/Contents/Frameworks
cp -R $1/QtGui.framework launcher.app/Contents/Frameworks

find launcher.app -name '*_debug' | xargs rm
find launcher.app -name 'Headers' | xargs rm -rf

install_name_tool -id @executable_path/../Frameworks/QtCore.framework/Versions/4.0/QtCore \
launcher.app/Contents/Frameworks/QtCore.framework/Versions/4.0/QtCore

install_name_tool -id @executable_path/../Frameworks/QtGui.framework/Versions/4.0/QtGui \
launcher.app/Contents/Frameworks/QtGui.framework/Versions/4.0/QtGui

install_name_tool -change $1/QtCore.framework/Versions/4.0/QtCore \
@executable_path/../Frameworks/QtCore.framework/Versions/4.0/QtCore \
launcher.app/Contents/MacOs/launcher

install_name_tool -change $1/QtGui.framework/Versions/4.0/QtGui \
@executable_path/../Frameworks/QtGui.framework/Versions/4.0/QtGui \
launcher.app/Contents/MacOs/launcher

install_name_tool -change path/to/Qt/lib/QtCore.framework/Versions/4.0/QtCore \
@executable_path/../Frameworks/QtCore.framework/Versions/4.0/QtCore \
launcher.app/Contents/Frameworks/QtGui.framework/Versions/4.0/QtGui

tar cjf ../dist/launcher.app-macosx-universal.tar.bz2 launcher.app/
