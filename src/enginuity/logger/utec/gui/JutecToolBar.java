package enginuity.logger.utec.gui;

import static javax.swing.BorderFactory.createLineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableMetaData;
import enginuity.NewGUI.interfaces.TuningEntity;
import enginuity.NewGUI.interfaces.TuningEntityListener;
import enginuity.NewGUI.tree.ETreeNode;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;
import enginuity.logger.utec.mapData.UtecMapData;
import enginuity.logger.utec.properties.UtecProperties;

public class JutecToolBar  extends JToolBar implements ActionListener {

	private TuningEntityListener theTEL;
	private int fileChosen;
	private JFileChooser fileChooser =  new JFileChooser();
	private TuningEntity parentTuningEntity;
	
    private JButton openImage = new JButton(new ImageIcon("./graphics/icon-open.png"));
    private JButton saveImage = new JButton(new ImageIcon("./graphics/icon-save.png"));
    private JButton closeImage = new JButton(new ImageIcon("./graphics/icon-close.png"));

    public JutecToolBar(TuningEntityListener theTEL, TuningEntity parentTuningEntity){
    	this.theTEL = theTEL;
    	this.parentTuningEntity = parentTuningEntity;
    	
        this.setFloatable(false);
        this.add(openImage);
        this.add(saveImage);
        this.add(closeImage);

        openImage.setMaximumSize(new Dimension(58, 50));
        openImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        saveImage.setMaximumSize(new Dimension(50, 50));
        saveImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        closeImage.setMaximumSize(new Dimension(50, 50));
        closeImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));

        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        closeImage.addActionListener(this);
        
        // Set tool tips
        openImage.setToolTipText("Open Utec Map");
        saveImage.setToolTipText("Save Utec Map");
        closeImage.setToolTipText("Close Utec Map");
        
        
        // Set initial button state
        this.openImage.setEnabled(true);
        this.saveImage.setEnabled(false);
        this.closeImage.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            try {
                //((ECUEditorMenuBar) parent.getJMenuBar()).openImageDialog();
            	System.out.println("Load Map From File");

				String openFileName = null;
				fileChosen = fileChooser.showOpenDialog(null);
				UtecMapData mapData = null;
				if (fileChosen == JFileChooser.APPROVE_OPTION) {
					openFileName = fileChooser.getSelectedFile().getPath();
					mapData = new UtecMapData(openFileName);
					
					if(mapData != null){
						// Add map to collection of maps
						UtecDataManager.addMap(mapData);
						
						// Enable the save option
						this.saveImage.setEnabled(true);
					}
				}

            } catch (Exception ex) {
                //JOptionPane.showMessageDialog(parent, new DebugPanel(ex,parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == saveImage) {
            try {
            	String temp = ApplicationStateManager.getSelectedTuningGroup();
            	System.out.println("Jutec Tuning group:"+temp);
            	UtecMapData mapData = null;
            	Iterator mapIterate = UtecDataManager.getAllMaps().iterator();
            	while(mapIterate.hasNext()){
            		mapData = (UtecMapData)mapIterate.next();
            		if(mapData.getMapName().equals(temp)){
            			break;
            		}
            	}
            	
            	int count = this.theTEL.getMapChangeCount(this.parentTuningEntity, mapData.getMapName());
                System.out.println("Maps Changed:"+count);
                if(count > 0){
                	this.theTEL.saveMaps();
                }
            	
                // Kick off the saving file to disk
            	System.out.println("Saving map to file.");
            	String saveFileName = null;
				fileChosen = fileChooser.showSaveDialog(null);
				if (fileChosen == JFileChooser.APPROVE_OPTION) {
					saveFileName = fileChooser.getSelectedFile().getPath();
					mapData.writeMapToFile(saveFileName);
				}
                
            } catch (Exception ex) {
            }
        } else if (e.getSource() == closeImage) {
        	System.out.println("Jutec Tool Bar: close image");
        	
        	String tuningGroup = ApplicationStateManager.getSelectedTuningGroup();
        	int mapChangeCount = this.theTEL.getMapChangeCount(ApplicationStateManager.getCurrentTuningEntity(),tuningGroup);
        	
        	if(mapChangeCount > 0){
        		int returnValue = JOptionPane.showConfirmDialog(this, "Tuning Group contains changes, save before continuing?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
        		
        		if(returnValue == 0){
        			this.theTEL.saveMaps();

                    // Kick off the saving file to disk
        			String temp = ApplicationStateManager.getSelectedTuningGroup();
        			UtecMapData mapData = null;
                	Iterator mapIterate = UtecDataManager.getAllMaps().iterator();
                	while(mapIterate.hasNext()){
                		mapData = (UtecMapData)mapIterate.next();
                		if(mapData.getMapName().equals(temp)){
                			break;
                		}
                	}
                	
                	String saveFileName = null;
    				fileChosen = fileChooser.showSaveDialog(null);
    				if (fileChosen == JFileChooser.APPROVE_OPTION) {
    					saveFileName = fileChooser.getSelectedFile().getPath();
    					mapData.writeMapToFile(saveFileName);
    				}
    				
                	this.theTEL.removeTuningGroup(tuningGroup);
        		}else if(returnValue == 1){
        			this.theTEL.removeTuningGroup(tuningGroup);
        		}else if(returnValue == 2){
        			return;
        		}
        	}else{
        		this.theTEL.removeTuningGroup(tuningGroup);
        	}
        }
    }
}
