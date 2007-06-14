/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity;

import enginuity.io.connection.ConnectionProperties;
import enginuity.logger.ecu.definition.EcuDefinition;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

public class Settings implements Serializable {

    private Dimension windowSize = new Dimension(800, 600);
    private Point windowLocation = new Point();
    private int splitPaneLocation = 150;
    private boolean windowMaximized = false;

    private String romRevisionURL = "http://www.scoobypedia.co.uk/index.php/Knowledge/ECUVersionCompatibilityList";
    private String ecuDefsURL = "http://www.enginuity.org/viewtopic.php?t=360";
    private String supportURL = "http://www.enginuity.org";
    private String releaseNotes = "release notes.txt";
    private String recentVersion = "x";

    private Vector<File> ecuDefinitionFiles = new Vector<File>();
    private File lastImageDir = new File("images");
    private boolean obsoleteWarning = true;
    private boolean calcConflictWarning = true;
    private boolean debug = false;
    private int userLevel = 1;
    private boolean saveDebugTables = false;
    private boolean displayHighTables = true;
    private boolean valueLimitWarning = true;

    private Font tableFont = new Font("Arial", Font.BOLD, 11);
    private Dimension cellSize = new Dimension(42, 18);
    private Color maxColor = new Color(255, 102, 102);
    private Color minColor = new Color(153, 153, 255);
    private Color highlightColor = new Color(204, 204, 204);
    private Color increaseBorder = new Color(255, 0, 0);
    private Color decreaseBorder = new Color(0, 0, 255);
    private Color axisColor = new Color(255, 255, 255);
    private Color warningColor = new Color(255, 0, 0);
    private int tableClickCount = 1; // number of clicks to open table

    private String loggerPort = "COM4";
    private String loggerProtocol = "SSM";
    private String loggerConfigFilePath = "logger.xml";
    private String loggerProfileFilePath = "profile.xml";
    private String loggerOutputDirPath = System.getProperty("user.home");
    private String fileLoggingControllerSwitchId = "S20"; // defogger switch by default
    private boolean fileLoggingControllerSwitchActive = true;
    private boolean fileLoggingAbsoluteTimestamp = false;

    private Dimension loggerWindowSize = new Dimension(1000, 600);
    private Point loggerWindowLocation = new Point();
    private boolean loggerWindowMaximized = false;
    private ConnectionProperties loggerConnectionProperties;
    private Map<String, EcuDefinition> loggerEcuDefinitionMap;


    public Settings() {
        //center window by default
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        windowLocation.move(((int) (screenSize.getWidth() - windowSize.getWidth()) / 2),
                ((int) (screenSize.getHeight() - windowSize.getHeight()) / 2));
    }

    public Dimension getWindowSize() {
        return windowSize;
    }

    public Point getWindowLocation() {
        return windowLocation;
    }

    public void setWindowSize(Dimension size) {
        windowSize.setSize(size);
    }

    public void setWindowLocation(Point location) {
        windowLocation.setLocation(location);
    }

    public Vector<File> getEcuDefinitionFiles() {
        if (ecuDefinitionFiles.isEmpty()) {
            // if no files defined, add default
            ecuDefinitionFiles.add(new File("ecu_defs.xml"));
        }
        return ecuDefinitionFiles;
    }

    public void addEcuDefinitionFile(File ecuDefinitionFile) {
        ecuDefinitionFiles.add(ecuDefinitionFile);
    }

    public void setEcuDefinitionFiles(Vector<File> ecuDefinitionFiles) {
        this.ecuDefinitionFiles = ecuDefinitionFiles;
    }

    public File getLastImageDir() {
        return lastImageDir;
    }

    public void setLastImageDir(File lastImageDir) {
        this.lastImageDir = lastImageDir;
    }

    public int getSplitPaneLocation() {
        return splitPaneLocation;
    }

    public void setSplitPaneLocation(int splitPaneLocation) {
        this.splitPaneLocation = splitPaneLocation;
    }

    public boolean isWindowMaximized() {
        return windowMaximized;
    }

    public void setWindowMaximized(boolean windowMaximized) {
        this.windowMaximized = windowMaximized;
    }

    public String getRomRevisionURL() {
        return romRevisionURL;
    }

    public String getSupportURL() {
        return supportURL;
    }

    public Font getTableFont() {
        return tableFont;
    }

    public void setTableFont(Font tableFont) {
        this.tableFont = tableFont;
    }

    public boolean isObsoleteWarning() {
        return obsoleteWarning;
    }

    public void setObsoleteWarning(boolean obsoleteWarning) {
        this.obsoleteWarning = obsoleteWarning;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Dimension getCellSize() {
        return cellSize;
    }

    public void setCellSize(Dimension cellSize) {
        this.cellSize = cellSize;
    }

    public Color getMaxColor() {
        return maxColor;
    }

    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
    }

    public Color getMinColor() {
        return minColor;
    }

    public void setMinColor(Color minColor) {
        this.minColor = minColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public boolean isCalcConflictWarning() {
        return calcConflictWarning;
    }

    public void setCalcConflictWarning(boolean calcConflictWarning) {
        this.calcConflictWarning = calcConflictWarning;
    }

    public Color getIncreaseBorder() {
        return increaseBorder;
    }

    public void setIncreaseBorder(Color increaseBorder) {
        this.increaseBorder = increaseBorder;
    }

    public Color getDecreaseBorder() {
        return decreaseBorder;
    }

    public void setDecreaseBorder(Color decreaseBorder) {
        this.decreaseBorder = decreaseBorder;
    }

    public Color getAxisColor() {
        return axisColor;
    }

    public void setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        if (userLevel > 5) {
            this.userLevel = 5;
        } else if (userLevel < 1) {
            this.userLevel = 1;
        } else {
            this.userLevel = userLevel;
        }
    }

    public int getTableClickCount() {
        return tableClickCount;
    }

    public void setTableClickCount(int tableClickCount) {
        this.tableClickCount = tableClickCount;
    }

    public String getRecentVersion() {
        return recentVersion;
    }

    public void setRecentVersion(String recentVersion) {
        this.recentVersion = recentVersion;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public boolean isSaveDebugTables() {
        return saveDebugTables;
    }

    public void setSaveDebugTables(boolean saveDebugTables) {
        this.saveDebugTables = saveDebugTables;
    }

    public boolean isDisplayHighTables() {
        return displayHighTables;
    }

    public void setDisplayHighTables(boolean displayHighTables) {
        this.displayHighTables = displayHighTables;
    }

    public boolean isValueLimitWarning() {
        return valueLimitWarning;
    }

    public void setValueLimitWarning(boolean valueLimitWarning) {
        this.valueLimitWarning = valueLimitWarning;
    }

    public Color getWarningColor() {
        return warningColor;
    }

    public void setWarningColor(Color warningColor) {
        this.warningColor = warningColor;
    }

    public String getLoggerPort() {
        return loggerPort;
    }

    public void setLoggerPort(String loggerPort) {
        this.loggerPort = loggerPort;
    }

    public String getLoggerProtocol() {
        return loggerProtocol;
    }

    public String getLoggerConfigFilePath() {
        return loggerConfigFilePath;
    }

    public String getLoggerOutputDirPath() {
        return loggerOutputDirPath;
    }

    public Point getLoggerWindowLocation() {
        return loggerWindowLocation;
    }

    public void setLoggerWindowLocation(Point loggerWindowLocation) {
        this.loggerWindowLocation = loggerWindowLocation;
    }

    public boolean isLoggerWindowMaximized() {
        return loggerWindowMaximized;
    }

    public void setLoggerWindowMaximized(boolean loggerWindowMaximized) {
        this.loggerWindowMaximized = loggerWindowMaximized;
    }

    public Dimension getLoggerWindowSize() {
        return loggerWindowSize;
    }

    public void setLoggerWindowSize(Dimension loggerWindowSize) {
        this.loggerWindowSize = loggerWindowSize;
    }

    public String getLoggerProfileFilePath() {
        return loggerProfileFilePath;
    }

    public void setLoggerProfileFilePath(String loggerProfileFilePath) {
        this.loggerProfileFilePath = loggerProfileFilePath;
    }

    public void setLoggerOutputDirPath(String loggerOutputDirPath) {
        this.loggerOutputDirPath = loggerOutputDirPath;
    }

    public String getEcuDefsURL() {
        return ecuDefsURL;
    }

    public void setEcuDefsURL(String ecuDefsURL) {
        this.ecuDefsURL = ecuDefsURL;
    }

    public String getFileLoggingControllerSwitchId() {
        return fileLoggingControllerSwitchId;
    }

    public void setFileLoggingControllerSwitchId(String fileLoggingControllerSwitchId) {
        checkNotNullOrEmpty(fileLoggingControllerSwitchId, "fileLoggingControllerSwitchId");
        this.fileLoggingControllerSwitchId = fileLoggingControllerSwitchId;
    }

    public boolean isFileLoggingControllerSwitchActive() {
        return fileLoggingControllerSwitchActive;
    }

    public void setFileLoggingControllerSwitchActive(boolean fileLoggingControllerSwitchActive) {
        this.fileLoggingControllerSwitchActive = fileLoggingControllerSwitchActive;
    }

    public boolean isFileLoggingAbsoluteTimestamp() {
        return fileLoggingAbsoluteTimestamp;
    }

    public void setFileLoggingAbsoluteTimestamp(boolean fileLoggingAbsoluteTimestamp) {
        this.fileLoggingAbsoluteTimestamp = fileLoggingAbsoluteTimestamp;
    }

    public ConnectionProperties getLoggerConnectionProperties() {
        return loggerConnectionProperties;
    }

    public void setLoggerConnectionProperties(ConnectionProperties loggerConnectionProperties) {
        this.loggerConnectionProperties = loggerConnectionProperties;
    }

    public Map<String, EcuDefinition> getLoggerEcuDefinitionMap() {
        return loggerEcuDefinitionMap;
    }

    public void setLoggerEcuDefinitionMap(Map<String, EcuDefinition> loggerEcuDefinitionMap) {
        this.loggerEcuDefinitionMap = loggerEcuDefinitionMap;
    }
}