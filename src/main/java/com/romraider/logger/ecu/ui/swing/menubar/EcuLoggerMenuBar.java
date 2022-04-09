/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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
import static java.awt.event.InputEvent.ALT_MASK;
import static java.awt.event.InputEvent.CTRL_MASK;
import static java.awt.event.InputEvent.SHIFT_MASK;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_F5;
import static java.awt.event.KeyEvent.VK_F6;
import static java.awt.event.KeyEvent.VK_F7;
import static java.awt.event.KeyEvent.VK_F8;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_M;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_V;
import static java.awt.event.KeyEvent.VK_X;
import static javax.swing.KeyStroke.getKeyStroke;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;

import com.romraider.Settings;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.ui.swing.menubar.action.AutoConnectAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ComPortAutoRefreshAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.DisconnectAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ElmEnabledAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ExitAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.FastPollModeAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.GlobalAdjustmentAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LearningTableValuesAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LoadProfileAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileAbsoluteTimestampAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileControllerSwitchAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileLocationAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LogFileNumberFormatAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LoggerDebugLocationAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LoggerDebuggingLevelAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.LoggerDefinitionLocationAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ReadEcuCodesAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ReloadProfileAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ResetConnectionAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.ResetEcuAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.SaveProfileAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.SaveProfileAsAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.SelectProtocolAction;
import com.romraider.logger.ecu.ui.swing.menubar.action.UpdateLoggerDefAction;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.swing.menubar.Menu;
import com.romraider.swing.menubar.MenuItem;
import com.romraider.swing.menubar.RadioButtonMenuItem;
import com.romraider.swing.menubar.action.AboutAction;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public class EcuLoggerMenuBar extends JMenuBar {

    private static final long serialVersionUID = 7081586516953740186L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(EcuLoggerMenuBar.class.getName());
    final Settings settings = SettingsManager.getSettings();

    public EcuLoggerMenuBar(EcuLogger logger, List<ExternalDataSource> externalDataSources) {

        // file menu items
        JMenu fileMenu = new Menu(rb.getString("FILE"), VK_F);
        fileMenu.add(new MenuItem(rb.getString("LOADPROFILE"), new LoadProfileAction(logger), VK_L, getKeyStroke(VK_L, CTRL_MASK)));
        fileMenu.add(new MenuItem(rb.getString("RELOADPROFILE"), new ReloadProfileAction(logger), VK_P, getKeyStroke(VK_P, CTRL_MASK)));
        fileMenu.add(new MenuItem(rb.getString("SAVEPROFILE"), new SaveProfileAction(logger), VK_S, getKeyStroke(VK_S, CTRL_MASK)));
        fileMenu.add(new MenuItem(rb.getString("SAVEPROFILEAS"), new SaveProfileAsAction(logger), VK_A, getKeyStroke(VK_S, CTRL_MASK | SHIFT_MASK)));
        fileMenu.add(new JSeparator());
        fileMenu.add(new MenuItem(rb.getString("EXIT"), new ExitAction(logger), VK_X));
        add(fileMenu);

        // settings menu items
        JMenu settingsMenu = new Menu(rb.getString("SETTINGS"), VK_S);
        settingsMenu.add(new MenuItem(rb.getString("DEFLOCATION"), new LoggerDefinitionLocationAction(logger), VK_F, getKeyStroke(VK_F, CTRL_MASK)));
        settingsMenu.add(new MenuItem(rb.getString("OUTLOCATION"), new LogFileLocationAction(logger), VK_O, getKeyStroke(VK_O, CTRL_MASK)));
        settingsMenu.add(new JSeparator());
        MenuItem selectProtocol = new MenuItem(rb.getString("PROTOOPTIONS"), new SelectProtocolAction(logger), VK_O, getKeyStroke(VK_O, ALT_MASK));
        selectProtocol.setToolTipText(rb.getString("PROTOOPTIONSTT"));
        settingsMenu.add(selectProtocol);
        RadioButtonMenuItem fileLoggingControl = new RadioButtonMenuItem(rb.getString("DEFOGGERSW"), VK_C, getKeyStroke(VK_C, CTRL_MASK), new LogFileControllerSwitchAction(logger), logger.getSettings().isFileLoggingControllerSwitchActive());
        fileLoggingControl.setEnabled(false);
        fileLoggingControl.setSelected(false);
        settingsMenu.add(fileLoggingControl);
        logger.getComponentList().put("fileLoggingControl", fileLoggingControl);
        RadioButtonMenuItem autoConnect = new RadioButtonMenuItem(rb.getString("AUTOCONNECT"), VK_A, getKeyStroke(VK_A, CTRL_MASK), new AutoConnectAction(logger), logger.getSettings().getAutoConnectOnStartup());
        autoConnect.setToolTipText(rb.getString("AUTOCONNECT"));
        settingsMenu.add(autoConnect);
        RadioButtonMenuItem autoRefresh = new RadioButtonMenuItem(rb.getString("COMREFRESH"), VK_E, getKeyStroke(VK_E, CTRL_MASK), new ComPortAutoRefreshAction(logger), logger.getSettings().getRefreshMode());
        autoRefresh.setToolTipText(rb.getString("COMREFRESHTT"));
        settingsMenu.add(autoRefresh);
        RadioButtonMenuItem elmEnabled = new RadioButtonMenuItem(rb.getString("ELM327ENABLED"), VK_C, getKeyStroke(VK_C, CTRL_MASK), new ElmEnabledAction(logger), logger.getSettings().getElm327Enabled());
        elmEnabled.setToolTipText(rb.getString("ELM327ENABLEDTT"));
        elmEnabled.setSelected(false);
        logger.getComponentList().put("elmEnabled", elmEnabled);
        logger.updateElmSelectable();

        settingsMenu.add(elmEnabled);
        
        RadioButtonMenuItem fastPoll = new RadioButtonMenuItem(rb.getString("FASTPOLL"), VK_M, getKeyStroke(VK_M, CTRL_MASK), new FastPollModeAction(logger), logger.getSettings().isFastPoll());
        fastPoll.setToolTipText(rb.getString("FASTPOLLTT"));
        fastPoll.setEnabled(false);
        fastPoll.setSelected(false);
        settingsMenu.add(fastPoll);
        logger.getComponentList().put("fastPoll", fastPoll);
        settingsMenu.add(new JSeparator());
        settingsMenu.add(new RadioButtonMenuItem(rb.getString("ABSTIMESTAMP"), VK_T, getKeyStroke(VK_T, CTRL_MASK), new LogFileAbsoluteTimestampAction(logger), logger.getSettings().isFileLoggingAbsoluteTimestamp()));
        final RadioButtonMenuItem numFormat = new RadioButtonMenuItem(rb.getString("USNUMBERS"), VK_B, getKeyStroke(VK_B, CTRL_MASK), new LogFileNumberFormatAction(logger), logger.getSettings().isUsNumberFormat());
        numFormat.setToolTipText(rb.getString("USNUMBERSTT"));
        settingsMenu.add(numFormat);
        add(settingsMenu);

        // connection menu items
        JMenu connectionMenu = new Menu(rb.getString("CONNECTION"), VK_C);
        connectionMenu.add(new MenuItem(rb.getString("RESET"), new ResetConnectionAction(logger), VK_R, getKeyStroke(VK_R, CTRL_MASK)));
        connectionMenu.add(new MenuItem(rb.getString("DISCONNECT"), new DisconnectAction(logger), VK_D, getKeyStroke(VK_D, CTRL_MASK)));
        add(connectionMenu);

        // tools menu items
        JMenu toolsMenu = new Menu(rb.getString("TOOLS"), VK_T);
        final MenuItem resetMenu = new MenuItem(rb.getString("RESETECU"), new ResetEcuAction(logger), VK_R, getKeyStroke(VK_F7, 0));
        toolsMenu.add(resetMenu);
        logger.getComponentList().put("resetMenu", resetMenu);
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new MenuItem(rb.getString("READDTC"), new ReadEcuCodesAction(logger), VK_D, getKeyStroke(VK_F8, 0)));
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new MenuItem(rb.getString("GLOBALADJ"), new GlobalAdjustmentAction(logger), VK_T, getKeyStroke(VK_F5, 0)));
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new MenuItem(rb.getString("LTV"), new LearningTableValuesAction(logger), VK_V, getKeyStroke(VK_F6, 0)));
        add(toolsMenu);

        // plugins menu items
        JMenu pluginsMenu = new Menu(rb.getString("PLUGINS"), VK_P);
        pluginsMenu.setEnabled(!externalDataSources.isEmpty());
        for (ExternalDataSource dataSource : externalDataSources) {
            Action action = dataSource.getMenuAction(logger);
            if (action != null) {
                pluginsMenu.add(new MenuItem(dataSource.getName(), action));
            }
        }
        add(pluginsMenu);

        // help menu stuff
        JMenu helpMenu = new Menu(rb.getString("HELP"), VK_H);
        helpMenu.add(new MenuItem(rb.getString("UPDATEDEF"), new UpdateLoggerDefAction(logger), VK_U));
        helpMenu.add(new JSeparator());
        ButtonGroup group = new ButtonGroup();
        JMenu debug = new JMenu(rb.getString("DEBUGLVL"));
        debug.setMnemonic(VK_D);
        debug.setToolTipText(rb.getString("DEBUGLVLTT"));
        RadioButtonMenuItem info = new RadioButtonMenuItem(rb.getString("INFO"), VK_I, null, new LoggerDebuggingLevelAction(logger, "INFO"), logger.getSettings().getLoggerDebuggingLevel().equalsIgnoreCase("INFO"));
        RadioButtonMenuItem db = new RadioButtonMenuItem(rb.getString("DEBUG"), VK_D, null, new LoggerDebuggingLevelAction(logger, "DEBUG"), logger.getSettings().getLoggerDebuggingLevel().equalsIgnoreCase("DEBUG"));
        RadioButtonMenuItem trace = new RadioButtonMenuItem(rb.getString("TRACE"), VK_T, null, new LoggerDebuggingLevelAction(logger, "TRACE"), logger.getSettings().getLoggerDebuggingLevel().equalsIgnoreCase("TRACE"));
        group.add(info);
        group.add(db);
        group.add(trace);
        debug.add(info);
        debug.add(db);
        debug.add(trace);
        debug.add(new JSeparator());
        debug.add(new MenuItem(rb.getString("DEBUGLOC"), new LoggerDebugLocationAction(logger), VK_O, getKeyStroke(VK_O, ALT_MASK)));
        helpMenu.add(debug);
        helpMenu.add(new JSeparator());
        helpMenu.add(new MenuItem(MessageFormat.format(rb.getString("ABOUT"), PRODUCT_NAME), new AboutAction(logger), VK_A));
        add(helpMenu);

    }
}
