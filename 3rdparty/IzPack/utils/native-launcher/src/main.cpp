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

#include <QApplication>
#include <QMessageBox>
#include <QFileDialog>
#include <QTranslator>
#include <QLocale>

#include "launcher.h"
#include "resolve-dialog.h"

#include <iostream>
using namespace std;

void show_launch_error_message()
{
    QMessageBox::critical(0, QT_TR_NOOP("Error"),
        QT_TR_NOOP("The installer could not be launched."));
}

int main(int argc, char** argv)
{
    QApplication app(argc, argv);
    
    QString locale = QLocale::system().name();
    QTranslator translator;
    translator.load(QString("launcher_") + locale);
    app.installTranslator(&translator);
    
    Launcher launcher;
detect:
    if (launcher.detectJRE())
    {
        if (launcher.launch() != 0)
        {
            show_launch_error_message();
            return 1;   
        }
    }
    else
    {
        ResolveDialog* dlg = new ResolveDialog();
        if (!launcher.isJREProvided())
        {
            dlg->disableProvidedRadio();
        }
        if (dlg->exec() != QDialog::Accepted)
        {
            return 1;
        }
        switch (dlg->getResolveChoice())
        {
        case MANUAL:
            if (launcher.launch(QFileDialog::getOpenFileName(0,
                QT_TR_NOOP("Please select the 'java' executable program."))) != 0)
            {
                show_launch_error_message();
                return 1; 
            }
            break;
        
        case PROVIDED:
            if (launcher.installProvidedJRE())
            {
                QMessageBox::critical(0, QT_TR_NOOP("Error"),
                    QT_TR_NOOP("The provided Java Runtime Environment could not be installed."));
                return 1;
            }
            goto detect;
        
        case DOWNLOAD:
            launcher.downloadJRE();
            goto detect;
            break;
        
        default:
            break;
        }
    }
    
    return 0;
}
