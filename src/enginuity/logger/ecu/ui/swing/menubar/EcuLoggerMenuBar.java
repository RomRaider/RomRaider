package enginuity.logger.ecu.ui.swing.menubar;

import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.ui.swing.menubar.action.AboutAction;
import enginuity.logger.ecu.ui.swing.menubar.action.DisconnectAction;
import enginuity.logger.ecu.ui.swing.menubar.action.ExitAction;
import enginuity.logger.ecu.ui.swing.menubar.action.LoadProfileAction;
import enginuity.logger.ecu.ui.swing.menubar.action.LogFileAbsoluteTimestampAction;
import enginuity.logger.ecu.ui.swing.menubar.action.LogFileControllerSwitchAction;
import enginuity.logger.ecu.ui.swing.menubar.action.LogFileLocationAction;
import enginuity.logger.ecu.ui.swing.menubar.action.ReloadProfileAction;
import enginuity.logger.ecu.ui.swing.menubar.action.ResetConnectionAction;
import enginuity.logger.ecu.ui.swing.menubar.action.SaveProfileAction;
import enginuity.logger.ecu.ui.swing.menubar.action.SaveProfileAsAction;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import static javax.swing.KeyStroke.getKeyStroke;
import static java.awt.event.KeyEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.SHIFT_MASK;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_X;

public class EcuLoggerMenuBar extends JMenuBar {

    public EcuLoggerMenuBar(EcuLogger logger) {

        // file menu items
        JMenu fileMenu = new EcuLoggerMenu("File", VK_F);
        fileMenu.add(new EcuLoggerMenuItem("Load Profile...", VK_L, getKeyStroke(VK_L, CTRL_MASK), new LoadProfileAction(logger)));
        fileMenu.add(new EcuLoggerMenuItem("Reload Profile", VK_P, getKeyStroke(VK_P, CTRL_MASK), new ReloadProfileAction(logger)));
        fileMenu.add(new EcuLoggerMenuItem("Save Profile", VK_S, getKeyStroke(VK_S, CTRL_MASK), new SaveProfileAction(logger)));
        fileMenu.add(new EcuLoggerMenuItem("Save Profile As...", VK_A, getKeyStroke(VK_S, CTRL_MASK | SHIFT_MASK), new SaveProfileAsAction(logger)));
        fileMenu.add(new JSeparator());
        fileMenu.add(new EcuLoggerMenuItem("Exit", VK_X, new ExitAction(logger)));
        add(fileMenu);

        // settings menu items
        JMenu settingsMenu = new EcuLoggerMenu("Settings", VK_S);
        settingsMenu.add(new EcuLoggerMenuItem("Log File Output Location...", VK_O, getKeyStroke(VK_O, CTRL_MASK), new LogFileLocationAction(logger)));
        settingsMenu.add(new JSeparator());
        settingsMenu.add(new EcuLoggerRadioButtonMenuItem("Control File Logging With Defogger Switch", VK_C, getKeyStroke(VK_C, CTRL_MASK), new LogFileControllerSwitchAction(logger), logger.getSettings().isFileLoggingControllerSwitchActive()));
        settingsMenu.add(new EcuLoggerRadioButtonMenuItem("Use Absolute Timestamp In Log File", VK_T, getKeyStroke(VK_T, CTRL_MASK), new LogFileAbsoluteTimestampAction(logger), logger.getSettings().isFileLoggingAbsoluteTimestamp()));
        add(settingsMenu);

        // connection menu items
        JMenu connectionMenu = new EcuLoggerMenu("Connection", VK_C);
        connectionMenu.add(new EcuLoggerMenuItem("Reset", VK_R, getKeyStroke(VK_R, CTRL_MASK), new ResetConnectionAction(logger)));
        connectionMenu.add(new EcuLoggerMenuItem("Disconnect", VK_D, getKeyStroke(VK_D, CTRL_MASK), new DisconnectAction(logger)));
        add(connectionMenu);

        // help menu stuff
        JMenu helpMenu = new EcuLoggerMenu("Help", VK_H);
        helpMenu.add(new EcuLoggerMenuItem("About", VK_A, new AboutAction(logger)));
        add(helpMenu);

    }

}
