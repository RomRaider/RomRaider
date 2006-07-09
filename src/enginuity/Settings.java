package enginuity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;

public class Settings implements Serializable {
    
    private Dimension windowSize            = new Dimension(800, 600);
    private Point windowLocation            = new Point();
    private int   splitPaneLocation         = 150;
    private boolean windowMaximized         = false;
    
    private String romRevisionURL           = "http://www.scoobypedia.co.uk/index.php/Knowledge/ECUVersionCompatibilityList";
    private String supportURL               = "http://www.enginuity.org";
    private String releaseNotes             = "./release notes.txt";
    private String recentVersion            = "x";
    
    private Vector<File> ecuDefinitionFiles = new Vector<File>();
    private File  lastImageDir              = new File("images");
    private boolean obsoleteWarning         = true;
    private boolean calcConflictWarning     = true;
    private boolean debug                   = false;
    private int userLevel                   = 1;
    private boolean saveDebugTables         = false;
    private boolean displayHighTables       = true;
    
    private Font tableFont                  = new Font("Arial", Font.BOLD, 12);
    private Dimension cellSize              = new Dimension(42, 18);
    private Color maxColor                  = new Color(255, 155, 155);
    private Color minColor                  = new Color(255, 255, 155);
    private Color highlightColor            = new Color(155, 155, 255);
    private Color increaseBorder            = new Color(255, 0, 0);
    private Color decreaseBorder            = new Color(0, 0, 255);
    private Color axisColor                 = new Color(255, 255, 255);
    private int   tableClickCount           = 2; // number of clicks to open table
    
    public Settings() {
        //center window by default
        addEcuDefinitionFile(new File("ecu_defs.xml"));
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        windowLocation.move(((int)(screenSize.getWidth() - windowSize.getWidth()) / 2),
                ((int)(screenSize.getHeight() - windowSize.getHeight()) / 2));
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
    
    public File getEcuDefinitionFile() {
        return ecuDefinitionFiles.elementAt(0);
    }
    
    public void addEcuDefinitionFile(File ecuDefinitionFile) {
        if (ecuDefinitionFiles.size() == 0)
            ecuDefinitionFiles.add(ecuDefinitionFile);
        else ecuDefinitionFiles.set(0, ecuDefinitionFile);
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
        this.userLevel = userLevel;
        if (userLevel > 5) userLevel = 5;
        else if (userLevel < 1) userLevel = 1;
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
}