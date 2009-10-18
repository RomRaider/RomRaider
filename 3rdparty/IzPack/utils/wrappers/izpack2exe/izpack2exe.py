#!/usr/bin/env python
# ........................................................................... #
#
# IzPack - Copyright 2007, 2008 Julien Ponge, All Rights Reserved.
#
# http://izpack.org/
# http://izpack.codehaus.org/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ........................................................................... #

import os
import sys
from shutil import *
from optparse import OptionParser

def parse_options():
    parser = OptionParser()
    parser.add_option("--file", action="append", dest="file",
                      help="The installer JAR file / files")
    parser.add_option("--output", action="store", dest="output",
                      default="setup.exe",
                      help="The executable file")
    parser.add_option("--with-7z", action="store", dest="p7z",
                      default="7za",
                      help="Path to the 7-Zip executable")
    parser.add_option("--with-upx", action="store", dest="upx",
                      default="upx",
                      help="Path to the UPX executable")
    parser.add_option("--no-upx", action="store_true", dest="no_upx",
                      default=False,
                      help="Do not use UPX to further compress the output")
    parser.add_option("--launch-file", action="store", dest="launch",
                      default="launcher.exe",                      
                      help="File to launch after extract")
    (options, args) = parser.parse_args()
    if (options.file is None):
        parser.error("no installer file has been given")
    return options    

def create_exe(settings):
    filename = os.path.basename(settings.launch)
    if(len(settings.file) == 1):
        filename = os.path.basename(settings.file[0])

    files = " ".join(settings.file);  
    p7z = '"%s" a -t7z -mx=9 -ms=off installer.7z %s' % (settings.p7z, files)

    os.system(p7z)
    config = open('config.txt', 'w')
    config.write(';!@Install@!UTF-8!\r\n')
    config.write('Title="IzPack"\r\n')
    config.write('Progress="yes"\r\n')
    config.write('ExecuteFile="%s"\r\n' % filename)
    config.write(';!@InstallEnd@!\r\n')
    config.close()

    if settings.p7z == '7za':
        sfx = os.path.join(os.path.dirname(sys.argv[0]), '7zS.sfx')
    else:    
        sfx = os.path.join(os.path.dirname(settings.p7z), '7zS.sfx')
    files = [sfx, 'config.txt', 'installer.7z']
    output = open(settings.output, 'wb')
    for f in files:
        in_file = open(f, 'rb')
        copyfileobj(in_file, output, 2048)
        in_file.close()
    output.close()

    if (not settings.no_upx):
        upx = '"%s" --ultra-brute %s' % (settings.upx, settings.output)
        os.system(upx)
    
    os.remove('config.txt')
    os.remove('installer.7z')

def main():
    create_exe(parse_options())

if __name__ == "__main__":
    main()
