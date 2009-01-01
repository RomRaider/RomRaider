TEMPLATE = app
CONFIG += warn_on release qt
macx {
    QMAKE_MAC_SDK=/Developer/SDKs/MacOSX10.4u.sdk
    CONFIG += x86 ppc
}
QT = core gui
FORMS = resolve-dialog.ui
HEADERS = launcher.h resolve-dialog.h
SOURCES = main.cpp launcher.cpp resolve-dialog.cpp
RESOURCES = resources.qrc
TRANSLATIONS = launcher_fr.ts
TARGET=launcher
win32 {
    RC_FILE = win32.rc
}
macx {
    RC_FILE = img/mac.icns
}
