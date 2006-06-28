package Enginuity;

import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;

public class Settings implements Serializable {
    
    private int[] windowSize = new int[2];
    private int[] windowLocation = new int[2];
    private File  ecuDefinitionFile = new File("./ecu_defs.xml");
    private File  lastImageDir = new File("./images");
    private boolean debug = true;
    
    public Settings() {
        windowSize[0] = 800;
        windowSize[1] = 600;
        
        //center window by default
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        windowLocation[0] = (int)(screenSize.getWidth() - windowSize[0]) / 2;
        windowLocation[1] = (int)(screenSize.getHeight() - windowSize[1]) / 2;
    }
    
    public int[] getWindowSize() {
        return windowSize;
    }
    
    public int[] getWindowLocation() {
        return windowLocation;
    }
    
    public void setWindowSize(int x, int y) {
        windowSize[0] = x;
        windowSize[1] = y;    
    }
    
    public void setWindowLocation(int x, int y) {
        windowLocation[0] = x;
        windowLocation[1] = y;    
    }
    
    public File getEcuDefinitionFile() {
        return ecuDefinitionFile;
    }
    
    public void setEcuDefinitionFile(File ecuDefinitionFile) {
        this.ecuDefinitionFile = ecuDefinitionFile;
    }

    public File getLastImageDir() {
        return lastImageDir;
    }

    public void setLastImageDir(File lastImageDir) {
        this.lastImageDir = lastImageDir;
    }
}