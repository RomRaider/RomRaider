package enginuity.xml;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import enginuity.Settings;
import enginuity.swing.JProgressPane;

import javax.imageio.metadata.IIOMetadataNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public final class DOMSettingsBuilder {

    public void buildSettings(Settings settings, File output, JProgressPane progress, String versionNumber) throws IOException {

        IIOMetadataNode settingsNode = new IIOMetadataNode("settings");

        // create settings
        progress.update("Saving window settings...", 15);
        settingsNode.appendChild(buildWindow(settings));
        progress.update("Saving file settings...", 30);
        settingsNode.appendChild(buildFiles(settings));
        progress.update("Saving options...", 45);
        settingsNode.appendChild(buildOptions(settings, versionNumber));
        progress.update("Saving display settings...", 60);
        settingsNode.appendChild(buildTableDisplay(settings));
        progress.update("Saving logger settings...", 75);
        settingsNode.appendChild(buildLogger(settings));

        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setIndenting(true);

        progress.update("Writing to file...", 90);

        FileOutputStream fos = new FileOutputStream(output);
        try {
            XMLSerializer serializer = new XMLSerializer(fos, of);
            serializer.serialize(settingsNode);
            fos.flush();
        } finally {
            fos.close();
        }
    }

    private IIOMetadataNode buildWindow(Settings settings) {
        IIOMetadataNode windowSettings = new IIOMetadataNode("window");

        // window size
        IIOMetadataNode size = new IIOMetadataNode("size");
        size.setAttribute("x", String.valueOf(((int) settings.getWindowSize().getHeight())));
        size.setAttribute("y", String.valueOf(((int) settings.getWindowSize().getWidth())));
        windowSettings.appendChild(size);

        // window location
        IIOMetadataNode location = new IIOMetadataNode("location");
        location.setAttribute("x", String.valueOf(((int) settings.getWindowLocation().getX())));
        location.setAttribute("y", String.valueOf(((int) settings.getWindowLocation().getY())));
        windowSettings.appendChild(location);

        // splitpane location
        IIOMetadataNode splitpane = new IIOMetadataNode("splitpane");
        splitpane.setAttribute("location", String.valueOf(settings.getSplitPaneLocation()));
        windowSettings.appendChild(splitpane);

        return windowSettings;
    }

    private IIOMetadataNode buildFiles(Settings settings) {
        IIOMetadataNode files = new IIOMetadataNode("files");

        // image directory
        IIOMetadataNode imageDir = new IIOMetadataNode("image_dir");
        imageDir.setAttribute("path", settings.getLastImageDir().getAbsolutePath());
        files.appendChild(imageDir);

        // ecu definition files
        Vector<File> defFiles = settings.getEcuDefinitionFiles();

        for (File defFile : defFiles) {
            IIOMetadataNode ecuDef = new IIOMetadataNode("ecudefinitionfile");
            ecuDef.setAttribute("name", defFile.getAbsolutePath());
            files.appendChild(ecuDef);
        }

        return files;
    }

    private IIOMetadataNode buildOptions(Settings settings, String versionNumber) {
        IIOMetadataNode options = new IIOMetadataNode("options");

        // obsolete warning
        IIOMetadataNode obsoleteWarning = new IIOMetadataNode("obsoletewarning");
        obsoleteWarning.setAttribute("value", String.valueOf(settings.isObsoleteWarning()));
        options.appendChild(obsoleteWarning);

        // calcultion conflicting warning
        IIOMetadataNode calcConflictWarning = new IIOMetadataNode("calcconflictwarning");
        calcConflictWarning.setAttribute("value", String.valueOf(settings.isCalcConflictWarning()));
        options.appendChild(calcConflictWarning);

        // debug mode
        IIOMetadataNode debug = new IIOMetadataNode("debug");
        debug.setAttribute("value", String.valueOf(settings.isDebug()));
        options.appendChild(debug);

        // userlevel
        IIOMetadataNode userLevel = new IIOMetadataNode("userlevel");
        userLevel.setAttribute("value", String.valueOf(settings.getUserLevel()));
        options.appendChild(userLevel);

        // table click count
        IIOMetadataNode tableClickCount = new IIOMetadataNode("tableclickcount");
        tableClickCount.setAttribute("value", String.valueOf(settings.getTableClickCount()));
        options.appendChild(tableClickCount);

        // last version used
        IIOMetadataNode version = new IIOMetadataNode("version");
        version.setAttribute("value", versionNumber);
        options.appendChild(version);

        // save debug level tables
        IIOMetadataNode saveDebugTables = new IIOMetadataNode("savedebugtables");
        saveDebugTables.setAttribute("value", String.valueOf(settings.isSaveDebugTables()));
        options.appendChild(saveDebugTables);

        // display tables higher than userlevel
        IIOMetadataNode displayHighTables = new IIOMetadataNode("displayhightables");
        displayHighTables.setAttribute("value", String.valueOf(settings.isDisplayHighTables()));
        options.appendChild(displayHighTables);

        // warning when exceeding limits
        IIOMetadataNode valueLimitWarning = new IIOMetadataNode("valuelimitwarning");
        valueLimitWarning.setAttribute("value", String.valueOf(settings.isValueLimitWarning()));
        options.appendChild(valueLimitWarning);

        return options;
    }

    private IIOMetadataNode buildTableDisplay(Settings settings) {
        IIOMetadataNode tableDisplay = new IIOMetadataNode("tabledisplay");

        // font
        IIOMetadataNode font = new IIOMetadataNode("font");
        font.setAttribute("face", settings.getTableFont().getName());
        font.setAttribute("size", String.valueOf(settings.getTableFont().getSize()));
        font.setAttribute("decoration", String.valueOf(settings.getTableFont().getStyle()));
        tableDisplay.appendChild(font);

        // table cell size
        IIOMetadataNode cellSize = new IIOMetadataNode("cellsize");
        cellSize.setAttribute("height", String.valueOf((int) settings.getCellSize().getHeight()));
        cellSize.setAttribute("width", String.valueOf(((int) settings.getCellSize().getWidth())));
        tableDisplay.appendChild(cellSize);

        // colors
        IIOMetadataNode colors = new IIOMetadataNode("colors");
        // max
        IIOMetadataNode max = new IIOMetadataNode("max");
        max.setAttribute("r", String.valueOf(settings.getMaxColor().getRed()));
        max.setAttribute("g", String.valueOf(settings.getMaxColor().getGreen()));
        max.setAttribute("b", String.valueOf(settings.getMaxColor().getBlue()));
        colors.appendChild(max);
        // min
        IIOMetadataNode min = new IIOMetadataNode("min");
        min.setAttribute("r", String.valueOf(settings.getMinColor().getRed()));
        min.setAttribute("g", String.valueOf(settings.getMinColor().getGreen()));
        min.setAttribute("b", String.valueOf(settings.getMinColor().getBlue()));
        colors.appendChild(min);
        // highlight
        IIOMetadataNode highlight = new IIOMetadataNode("highlight");
        highlight.setAttribute("r", String.valueOf(settings.getHighlightColor().getRed()));
        highlight.setAttribute("g", String.valueOf(settings.getHighlightColor().getGreen()));
        highlight.setAttribute("b", String.valueOf(settings.getHighlightColor().getBlue()));
        colors.appendChild(highlight);
        // increased cell border
        IIOMetadataNode increaseBorder = new IIOMetadataNode("increaseborder");
        increaseBorder.setAttribute("r", String.valueOf(settings.getIncreaseBorder().getRed()));
        increaseBorder.setAttribute("g", String.valueOf(settings.getIncreaseBorder().getGreen()));
        increaseBorder.setAttribute("b", String.valueOf(settings.getIncreaseBorder().getBlue()));
        colors.appendChild(increaseBorder);
        // decreased cell border
        IIOMetadataNode decreaseBorder = new IIOMetadataNode("decreaseborder");
        decreaseBorder.setAttribute("r", String.valueOf(settings.getDecreaseBorder().getRed()));
        decreaseBorder.setAttribute("g", String.valueOf(settings.getDecreaseBorder().getGreen()));
        decreaseBorder.setAttribute("b", String.valueOf(settings.getDecreaseBorder().getBlue()));
        colors.appendChild(decreaseBorder);
        // axis cells
        IIOMetadataNode axis = new IIOMetadataNode("axis");
        axis.setAttribute("r", String.valueOf(settings.getAxisColor().getRed()));
        axis.setAttribute("g", String.valueOf(settings.getAxisColor().getGreen()));
        axis.setAttribute("b", String.valueOf(settings.getAxisColor().getBlue()));
        colors.appendChild(axis);
        // warning cells
        IIOMetadataNode warning = new IIOMetadataNode("warning");
        warning.setAttribute("r", String.valueOf(settings.getWarningColor().getRed()));
        warning.setAttribute("g", String.valueOf(settings.getWarningColor().getGreen()));
        warning.setAttribute("b", String.valueOf(settings.getWarningColor().getBlue()));
        colors.appendChild(warning);

        tableDisplay.appendChild(colors);

        return tableDisplay;
    }

    private IIOMetadataNode buildLogger(Settings settings) {
        IIOMetadataNode loggerSettings = new IIOMetadataNode("logger");

        // window size
        IIOMetadataNode size = new IIOMetadataNode("size");
        size.setAttribute("x", String.valueOf(((int) settings.getLoggerWindowSize().getHeight())));
        size.setAttribute("y", String.valueOf(((int) settings.getLoggerWindowSize().getWidth())));
        loggerSettings.appendChild(size);

        // window location
        IIOMetadataNode location = new IIOMetadataNode("location");
        location.setAttribute("x", String.valueOf(((int) settings.getLoggerWindowLocation().getX())));
        location.setAttribute("y", String.valueOf(((int) settings.getLoggerWindowLocation().getY())));
        loggerSettings.appendChild(location);

        // profile path
        IIOMetadataNode profile = new IIOMetadataNode("profile");
        profile.setAttribute("path", settings.getLoggerProfileFilePath());
        loggerSettings.appendChild(profile);

        // file logging
        IIOMetadataNode filelogging = new IIOMetadataNode("filelogging");
        filelogging.setAttribute("path", settings.getLoggerOutputDirPath());
        filelogging.setAttribute("switchid", settings.getFileLoggingControllerSwitchId());
        filelogging.setAttribute("active", String.valueOf(settings.isFileLoggingControllerSwitchActive()));
        filelogging.setAttribute("absolutetimestamp", String.valueOf(settings.isFileLoggingAbsoluteTimestamp()));
        loggerSettings.appendChild(filelogging);

        return loggerSettings;
    }
}