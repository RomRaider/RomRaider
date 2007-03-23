package enginuity.logger.utec.gui;

import static javax.swing.BorderFactory.createLineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableNodeMetaData;
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
	private TuningEntity parentEntity;
	
   // private ECUEditor parent;
    private JButton openImage = new JButton(new ImageIcon("./graphics/icon-open.png"));
    private JButton saveImage = new JButton(new ImageIcon("./graphics/icon-save.png"));
    private JButton refreshImage = new JButton(new ImageIcon("./graphics/icon-refresh.png"));
    private JButton closeImage = new JButton(new ImageIcon("./graphics/icon-close.png"));

    public JutecToolBar(TuningEntityListener theTEL, TuningEntity parentEntity){
    	this.theTEL = theTEL;
    	this.parentEntity = parentEntity;
    	
        this.setFloatable(false);
        this.add(openImage);
        this.add(saveImage);
        this.add(closeImage);
        this.add(refreshImage);

        openImage.setMaximumSize(new Dimension(58, 50));
        openImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        saveImage.setMaximumSize(new Dimension(50, 50));
        saveImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        closeImage.setMaximumSize(new Dimension(50, 50));
        closeImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));
        refreshImage.setMaximumSize(new Dimension(50, 50));
        refreshImage.setBorder(createLineBorder(new Color(150, 150, 150), 0));

        updateButtons();

        openImage.addActionListener(this);
        saveImage.addActionListener(this);
        closeImage.addActionListener(this);
        refreshImage.addActionListener(this);
    }

    public void updateButtons() {
        String file = "";//getLastSelectedRomFileName();

        openImage.setToolTipText("Open Image");
        saveImage.setToolTipText("Save " + file);
        refreshImage.setToolTipText("Refresh " + file + " from saved copy");
        closeImage.setToolTipText("Close " + file);

        if ("".equals(file)) {
            saveImage.setEnabled(false);
            refreshImage.setEnabled(false);
            closeImage.setEnabled(false);
        } else {
            saveImage.setEnabled(true);
            refreshImage.setEnabled(true);
            closeImage.setEnabled(true);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openImage) {
            try {
                //((ECUEditorMenuBar) parent.getJMenuBar()).openImageDialog();
            	System.out.println("Load Map From File");

				String saveFileName = null;
				fileChosen = fileChooser.showSaveDialog(null);
				UtecMapData mapData = null;
				if (fileChosen == JFileChooser.APPROVE_OPTION) {
					saveFileName = fileChooser.getSelectedFile().getPath();
					mapData = new UtecMapData(saveFileName);
					UtecDataManager.setCurrentMap(mapData);
				}
				
				if(mapData != null){

					// Initialise tree
					ETreeNode root = new ETreeNode("UTEC:"+UtecDataManager.getCurrentMapData().getMapName()+", "+UtecDataManager.getCurrentMapData().getMapComment(), new TableNodeMetaData(TableNodeMetaData.CATEGORY,0.0,0.0,new Object[0],false,"","", this.parentEntity));
					
					Object[] ignored = {new Double(-100.0)};
					ETreeNode fuel = new ETreeNode("Fuel", new TableNodeMetaData(TableNodeMetaData.DATA3D, Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.fuelMapMax")[0]), ignored, false, "Fuel" , "Fuel:"+mapData.getMapName(), this.parentEntity));
					
					Object[] ignored2 = {new Double(-100.0)};
					ETreeNode timing = new ETreeNode("Timing", new TableNodeMetaData(TableNodeMetaData.DATA3D, Double.parseDouble(UtecProperties.getProperties("utec.timingMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.timingMapMax")[0]), ignored, false, "Timing" , "Timing:"+mapData.getMapName(), this.parentEntity));
					
					Object[] ignored3 = {new Double(-100.0)};
					ETreeNode boost = new ETreeNode("Boost", new TableNodeMetaData(TableNodeMetaData.DATA3D, Double.parseDouble(UtecProperties.getProperties("utec.boostMapMin")[0]), Double.parseDouble(UtecProperties.getProperties("utec.boostMapMax")[0]), ignored, false, "Boost" , "Boost:"+mapData.getMapName(), this.parentEntity));
					root.add(fuel);
					root.add(timing);
					root.add(boost);
					
					this.theTEL.TreeStructureChanged(root);
				}

            } catch (Exception ex) {
                //JOptionPane.showMessageDialog(parent, new DebugPanel(ex,parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == saveImage) {
            try {
                //((ECUEditorMenuBar) parent.getJMenuBar()).saveImage(parent.getLastSelectedRom());
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
