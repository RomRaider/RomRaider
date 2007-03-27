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
	
   // private ECUEditor parent;
    private JButton openImage = new JButton(new ImageIcon("./graphics/icon-open.png"));
    private JButton saveImage = new JButton(new ImageIcon("./graphics/icon-save.png"));
    private JButton refreshImage = new JButton(new ImageIcon("./graphics/icon-refresh.png"));
    private JButton closeImage = new JButton(new ImageIcon("./graphics/icon-close.png"));

    public JutecToolBar(TuningEntityListener theTEL, TuningEntity parentTuningEntity){
    	this.theTEL = theTEL;
    	this.parentTuningEntity = parentTuningEntity;
    	
        this.setFloatable(false);
        this.add(openImage);
        this.add(saveImage);
        this.add(closeImage);
        //this.add(refreshImage);

        openImage.setMaximumSize(new Dimension(58, 50));
        openImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        saveImage.setMaximumSize(new Dimension(50, 50));
        saveImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        closeImage.setMaximumSize(new Dimension(50, 50));
        closeImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        refreshImage.setMaximumSize(new Dimension(50, 50));
        refreshImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));

        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        closeImage.addActionListener(this);
        refreshImage.addActionListener(this);
        
        // Set tool tips

        openImage.setToolTipText("Open Utec Map");
        saveImage.setToolTipText("Save Utec Map");
        //refreshImage.setToolTipText("Refresh " + file + " from saved copy");
        closeImage.setToolTipText("Close Utec Map");
        
        
        // Set initial button state
        this.openImage.setEnabled(true);
        this.saveImage.setEnabled(false);
        //this.refreshImage.setEnabled(false);
        this.closeImage.setEnabled(false);
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
					
					//UtecDataManager.setCurrentMap(mapData);
					UtecDataManager.addMap(mapData);
				}
				
				if(mapData != null){

					// Define columnLabels
					String[] columnLabels = new String[11];
					for(int i = 0; i < columnLabels.length ; i++){
						columnLabels[i] = i+"";
					}

					String[] rowLabels = new String[40];
					for(int i = 0; i < rowLabels.length ; i++){
						rowLabels[i] = i+"";
					}
					
					
					// Initialise tree
					ETreeNode root = new ETreeNode("UTEC:"+mapData.getMapName()+", "+mapData.getMapComment(), new TableMetaData(TableMetaData.MAP_SET_ROOT,0.0,0.0,new Object[0],null,null,false,"","", mapData.getMapName(), this.parentTuningEntity));
					
					Object[] ignored = {new Double(-100.0)};
					ETreeNode fuel = new ETreeNode("Fuel", new TableMetaData(TableMetaData.DATA_3D, Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMax")[0]), ignored,columnLabels,rowLabels, false, "Fuel" , "Fuel:"+mapData.getMapName(), mapData.getMapName(),this.parentTuningEntity));
					
					Object[] ignored2 = {new Double(-100.0)};
					ETreeNode timing = new ETreeNode("Timing", new TableMetaData(TableMetaData.DATA_3D, Double.parseDouble(UtecProperties.getProperties("utec.timingMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.timingMapMax")[0]), ignored,columnLabels,rowLabels, false, "Timing" , "Timing:"+mapData.getMapName(), mapData.getMapName(),this.parentTuningEntity));
					
					Object[] ignored3 = {new Double(-100.0)};
					ETreeNode boost = new ETreeNode("Boost", new TableMetaData(TableMetaData.DATA_3D, Double.parseDouble(UtecProperties.getProperties("utec.boostMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.boostMapMax")[0]), ignored, columnLabels,rowLabels,false, "Boost" , "Boost:"+mapData.getMapName(), mapData.getMapName(), this.parentTuningEntity));
					root.add(fuel);
					root.add(timing);
					root.add(boost);
					
					this.theTEL.addNewTuningGroup(root);
					
					// Enable the save option
					this.saveImage.setEnabled(true);
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
               // JOptionPane.showMessageDialog(parent, new DebugPanel(ex,parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == closeImage) {
            //((ECUEditorMenuBar) parent.getJMenuBar()).closeImage();
        } else if (e.getSource() == refreshImage) {
            try {
                //((ECUEditorMenuBar) parent.getJMenuBar()).refreshImage();
            } catch (Exception ex) {
                //JOptionPane.showMessageDialog(parent, new DebugPanel(ex,parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
