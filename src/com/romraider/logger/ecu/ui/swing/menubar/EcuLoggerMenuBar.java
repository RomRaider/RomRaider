/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.ui.swing.menubar;

import static com.romraider.Version.PRODUCT_NAME;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.external.ExternalDataSource;
import com.romraider.logger.ecu.ui.swing.menubar.action.DisconnectAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ExitAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LoadProfileAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileAbsoluteTimestampAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileControllerSwitchAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileLocationAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LoggerDefinitionLocationAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ReloadProfileAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ResetConnectionAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ResetEcuAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.SaveProfileAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.SaveProfileAsAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.UpdateLoggerDefAction;
import com.romraider.swing.menubar.Menu;
import com.romraider.swing.menubar.MenuItem;
import com.romraider.swing.menubar.RadioButtonMenuItem;
import com.romraider.swing.menubar.action.AboutAction;
import javax.swing.Action;
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
import static java.awt.event.KeyEvent.VK_F7;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_X;
import java.util.List;

public class EcuLoggerMenuBar extends JMenuBar {

    private static final long serialVersionUID = 7081586516953740186L;

    public EcuLoggerMenuBar(EcuLogger logger, List<ExternalDataSource> externalDataSources) {

        // file menu items
        JMenu fileMenu = new Menu("File", VK_F);
        fileMenu.add(new MenuItem("Load Profile...", new LoadProfileAction(logger), VK_L, getKeyStroke(VK_L, CTRL_MASK)));
        fileMenu.add(new MenuItem("Reload Profile", new ReloadProfileAction(logger), VK_P, getKeyStroke(VK_P, CTRL_MASK)));
        fileMenu.add(new MenuItem("Save Profile", new SaveProfileAction(logger), VK_S, getKeyStroke(VK_S, CTRL_MASK)));
        fileMenu.add(new MenuItem("Save Profile As...", new SaveProfileAsAction(logger), VK_A, getKeyStroke(VK_S, CTRL_MASK | SHIFT_MASK)));
        fileMenu.add(new JSeparator());
        fileMenu.add(new MenuItem("Exit", new ExitAction(logger), VK_X));
        add(fileMenu);

        // settings menu items
        JMenu settingsMenu = new Menu("Settings", VK_S);
        settingsMenu.add(new MenuItem("Logger Definition Location...", new LoggerDefinitionLocationAction(logger), VK_D, getKeyStroke(VK_D, CTRL_MASK)));
        settingsMenu.add(new MenuItem("Log File Output Location...", new LogFileLocationAction(logger), VK_O, getKeyStroke(VK_O, CTRL_MASK)));
        settingsMenu.add(new JSeparator());
        settingsMenu.add(new RadioButtonMenuItem("Control File Logging With Defogger Switch", VK_C, getKeyStroke(VK_C, CTRL_MASK), new LogFileControllerSwitchAction(logger), logger.getSettings().isFileLoggingControllerSwitchActive()));
        settingsMenu.add(new RadioButtonMenuItem("Use Absolute Timestamp In Log File", VK_T, getKeyStroke(VK_T, CTRL_MASK), new LogFileAbsoluteTimestampAction(logger), logger.getSettings().isFileLoggingAbsoluteTimestamp()));
        add(settingsMenu);

        // connection menu items
        JMenu connectionMenu = new Menu("Connection", VK_C);
        connectionMenu.add(new MenuItem("Reset", new ResetConnectionAction(logger), VK_R, getKeyStroke(VK_R, CTRL_MASK)));
        connectionMenu.add(new MenuItem("Disconnect", new DisconnectAction(logger), VK_D, getKeyStroke(VK_D, CTRL_MASK)));
        add(connectionMenu);

        // tools menu items
        JMenu toolsMenu = new Menu("Tools", VK_T);
        toolsMenu.add(new MenuItem("Reset ECU", new ResetEcuAction(logger), VK_R, getKeyStroke(VK_F7, 0)));
        add(toolsMenu);

        // plugins menu items
        JMenu pluginsMenu = new Menu("Plugins", VK_P);
        pluginsMenu.setEnabled(!externalDataSources.isEmpty());
        for (ExternalDataSource dataSource : externalDataSources) {
            Action action = dataSource.getMenuAction(logger);
            if (action != null) {
                pluginsMenu.add(new MenuItem(dataSource.getName(), action));
            }
        }
        add(pluginsMenu);

        // help menu stuff
        JMenu helpMenu = new Menu("Help", VK_H);
        helpMenu.add(new MenuItem("Update Logger Definition...", new UpdateLoggerDefAction(logger), VK_U));
        helpMenu.add(new JSeparator());
        helpMenu.add(new MenuItem("About " + PRODUCT_NAME, new AboutAction(logger), VK_A));
        add(helpMenu);

    }
}
