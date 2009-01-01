/* 
 * The IzPack Launcher
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright (c) 2004 - 2008 Julien Ponge - All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

#include "launcher.h"

Launcher::Launcher()
{
    QSettings ini("launcher.ini", QSettings::IniFormat, 0);
    
    // Top-level settings
    if (ini.contains("jre"))
    {
        jre = ini.value("jre").toString();
    }
    if (ini.contains("jar"))
    {
        jar = ini.value("jar").toString();
    }
    if (ini.contains("download"))
    {
        download = ini.value("download").toString();
    }
    
    // Platform-specific settings
#ifdef Q_WS_WIN
    QString group = "win32";
#endif
#ifdef Q_WS_MAC
    QString group = "mac";
#endif
#ifdef Q_WS_X11
    QString group = "x11";
#endif
    if (ini.contains(group + "/jre"))
    {
        jre = ini.value(group + "/jre").toString();
    }
    if (ini.contains(group + "/jar"))
    {
        jar = ini.value(group + "/jar").toString();
    }
    if (ini.contains(group + "/download"))
    {
        download = ini.value(group + "/download").toString();
    }
}

bool Launcher::launch()
{
    return launch(javaExecPath);
}

bool Launcher::launch(const QString &runtimeExecPath)
{
    QStringList args;
    args.append("-jar");
    args.append(jar);
    return Launcher::execute(runtimeExecPath, args);
}

void Launcher::downloadJRE()
{

#ifdef Q_WS_WIN

    QStringList args;
    args.append("url.dll,FileProtocolHandler");
    args.append(download);
    Launcher::execute("rundll32", args);

#endif

#ifdef Q_WS_X11

    QStringList browsers;
    browsers.append("firefox");
    browsers.append("mozilla");
    browsers.append("konqueror");
    browsers.append("opera");
    for (int i = 0; i < browsers.size(); ++i)
    {
        if (Launcher::execute(browsers.at(i), QStringList("-v")) == 0)
        {
            Launcher::execute(browsers.at(i), QStringList(download));
            return;
        }
    }

#endif

#ifdef Q_WS_MAC

    Launcher::execute("open", QStringList(download));

#endif

}

bool Launcher::installProvidedJRE()
{
    return Launcher::execute(jre);
}

bool Launcher::detectJRE()
{

#ifdef Q_WS_WIN

    // Windows-specific registry lookups    
    QSettings settings("HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft"
                       "\\Java Runtime Environment\\", QSettings::NativeFormat);
    if (settings.contains("CurrentVersion"))
    {
        QString version = settings.value("CurrentVersion").toString();
        if (version != "1.1")
        {
            QString path = settings.value(version + "/JavaHome").toString();
            javaExecPath = path + "\\bin\\java";
            return true;
        }
    }
    
#endif

    // JAVA_HOME lookup
    char* envRes = getenv("JAVA_HOME");
    if (envRes)
    {
        QString path = QString(envRes);
#ifdef Q_WS_QWIN
        path = path + "\\bin\\java";
#else
        path = path + "bin/java";
#endif
        if (QFile::exists(path))
        {
            javaExecPath = path;
            return true;
        }
    }

#ifdef Q_WS_MAC
	
	// Mac OS X
	QString pathOnOSX = "/System/Library/Frameworks/JavaVM.framework/"
						"Versions/CurrentJDK/Commands/java";
	if (Launcher::execute(pathOnOSX, QStringList("-version")) == 0)
	{
		javaExecPath = pathOnOSX;
		return true;
	}
	
#endif

    // Last chance, lucky trial
    if (Launcher::execute("java", QStringList("-version")) == 0)
    {
        javaExecPath = "java";
        return true;
    }

    return false;
}

int Launcher::execute(const QString &program, const QStringList &arguments) 
{
    QProcess process;
    
    process.setReadChannelMode(QProcess::ForwardedChannels);
    process.start(program, arguments);
    process.waitForFinished(-1);
    
    int exitCode = process.exitCode();
    if (process.error() == QProcess::FailedToStart)
    {
        exitCode = -1;
    }
    
    return exitCode;
}

int Launcher::execute(const QString &program)
{
    QProcess process;
    process.setReadChannelMode(QProcess::ForwardedChannels);
    process.start(program);
    process.waitForFinished(-1);
    
    int exitCode = process.exitCode();
    if (process.error() == QProcess::FailedToStart)
    {
        exitCode = -1;
    }
    
    return exitCode;
}

