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

package com.romraider.maps;

import static com.romraider.maps.RomChecksum.calculateRomChecksum;
import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.HexUtil.asHex;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.maps.checksum.ChecksumManager;
import com.romraider.swing.CategoryTreeNode;
import com.romraider.swing.JProgressPane;
import com.romraider.swing.TableFrame;
import com.romraider.swing.TableTreeNode;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public class Rom extends DefaultMutableTreeNode implements Serializable  {
    private static final long serialVersionUID = 7865405179738828128L;
    private static final Logger LOGGER = Logger.getLogger(Rom.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            Rom.class.getName());

    private RomID romID;
    private File definitionPath;
    private String fileName = "";
    private File fullFileName = new File(".");
    private byte[] binData;
    private Document doc;

    //This keeps track of DataCells on a byte level
    //This might also be possible to achieve by using the same Data Tables
    protected HashMap<Integer, LinkedList<DataCell>> byteCellMapping = new HashMap<Integer, LinkedList<DataCell>>();

    private boolean isAbstract = false;

    private final HashMap<String, TableTreeNode> tableNodes = new HashMap<String, TableTreeNode>();
    private LinkedList<ChecksumManager> checksumManagers = new LinkedList<ChecksumManager>();

    public Rom(RomID romID) {
    	this.romID = romID;
    }

    //This makes sure we automatically sort the tables by name
    public void sortedAdd(DefaultMutableTreeNode currentParent, DefaultMutableTreeNode newNode) {
        boolean found = false;
        for(int k = 0; k < currentParent.getChildCount(); k++){
        	TreeNode n = currentParent.getChildAt(k);

        	//Category nodes should be placed at the top
        	if(newNode instanceof CategoryTreeNode && !(n instanceof CategoryTreeNode)) {
        		found = true;
        	}
        	else if(!(newNode instanceof CategoryTreeNode) && n instanceof CategoryTreeNode) {
        		continue;
        	}
        	else if(n.toString().compareToIgnoreCase(newNode.toString()) >= 0) {
        		found = true;
        	}

        	if(found) {
        		currentParent.insert(newNode, k);
        		break;
        	}
        }

        if(!found) {
        	currentParent.add(newNode);
        }
    }

    public void refreshDisplayedTables() {
        // Remove all nodes from the ROM tree node.
        super.removeAllChildren();

        Settings settings = SettingsManager.getSettings();

        // Add nodes to ROM tree.
        for (TableTreeNode tableTreeNode : tableNodes.values()) {
            Table table = tableTreeNode.getTable();

            String[] categories = table.getCategory().split("//");

            if (settings.isDisplayHighTables() || settings.getUserLevel() >= table.getUserLevel()) {

                DefaultMutableTreeNode currentParent = this;

                for(int i=0; i < categories.length; i++) {
                    boolean categoryExists = false;

	                for (int j = 0; j < currentParent.getChildCount(); j++) {
	                    if (currentParent.getChildAt(j).toString().equalsIgnoreCase(categories[i])) {
	                    	categoryExists = true;
	                    	currentParent = (DefaultMutableTreeNode) currentParent.getChildAt(j);
	                        break;
	                    }
	                }

	                if(!categoryExists) {
	                    CategoryTreeNode categoryNode = new CategoryTreeNode(categories[i]);
	                    sortedAdd(currentParent,categoryNode);
	                    currentParent = categoryNode;
	                }

                	if(i == categories.length - 1){
                		sortedAdd(currentParent, tableTreeNode);
                	}
                }
            }
        }
    }

    public void addTableByName(Table table) {
        table.setRom(this);
        tableNodes.put(table.getName().toLowerCase(), new TableTreeNode(table));
    }

    public void removeTableByName(Table table) {
    	if(tableNodes.containsKey(table.getName().toLowerCase())) {
    		tableNodes.remove(table.getName().toLowerCase());
    	}
    }

    public Table getTableByName(String tableName) {
    	TableTreeNode node = getTableNodeByName(tableName);
    	
    	if(node != null)
    		return node.getTable();
    	return null;
    }
    
    public TableTreeNode getTableNodeByName(String tableName) {
        if(!tableNodes.containsKey(tableName.toLowerCase())) {
            return null;
        }
        else {
        	return tableNodes.get(tableName.toLowerCase());
        }
    }

    public List<Table> findTables(String regex) {
        List<Table> result = new ArrayList<Table>();
        for (TableTreeNode tableNode : tableNodes.values()) {
            String name = tableNode.getTable().getName();
            if (name.matches(regex)) result.add(tableNode.getTable());
        }
        return result;
    }

    // Table storage address extends beyond end of file
    private void showBadTablePopup(Table table, Exception ex) {
        LOGGER.error(table.getName() +
                " type " + table.getType() + " start " +
                table.getStorageAddress() + " " + binData.length + " filesize", ex);

        JOptionPane.showMessageDialog(null,
                MessageFormat.format(rb.getString("ADDROUTOFBNDS"), table.getName()),
                rb.getString("ECUDEFERROR"), JOptionPane.ERROR_MESSAGE);
    }

    private void showNullExceptionPopup(Table table, Exception ex) {
        LOGGER.error("Error Populating Table", ex);
        JOptionPane.showMessageDialog(null,
                MessageFormat.format(rb.getString("TABLELOADERR"), table.getName()),
                rb.getString("ECUDEFERROR"), JOptionPane.ERROR_MESSAGE);
    }

    public void populateTables(byte[] binData, JProgressPane progress) {
        this.binData = binData;
        int size = tableNodes.size();
        int i = 0;
        Vector <String> badKeys = new Vector<String>();

        for(String name: tableNodes.keySet()) {
            // update progress
            int currProgress = (int) (i / (double) size * 100);
            progress.update(rb.getString("POPTABLES"), currProgress);

            Table table = tableNodes.get(name.toLowerCase()).getTable();

            try {
                if (table.getStorageAddress() >= 0) {
                    try {
                        table.populateTable(this);
                        TableUpdateHandler.getInstance().registerTable(table);

                        if (null != table.getName() && table.getName().equalsIgnoreCase("Checksum Fix")){
                            setEditStamp(binData, table.getStorageAddress());
                        }

                        i++;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    	showBadTablePopup(table, ex);
                    	badKeys.add(table.getName());
                    	size--;
                    } catch (IndexOutOfBoundsException iex) {
                    	showBadTablePopup(table, iex);
                    	badKeys.add(table.getName());
                    	size--;
                    }
                } else {
                	tableNodes.remove(table.getName().toLowerCase());
                	size--;
                }

            } catch (NullPointerException ex) {
            	showNullExceptionPopup(table, ex);
            	badKeys.add(table.getName());
            	size--;
            }
        }

        for(String s: badKeys) {
        	tableNodes.remove(s.toLowerCase());
        }
    }

    private void setEditStamp(byte[] binData, int address) {
        byte[] stampData = new byte[4];
        System.arraycopy(binData, address+204, stampData, 0, stampData.length);
        String stamp = asHex(stampData);
        if (stamp.equalsIgnoreCase("FFFFFFFF")) {
            romID.setEditStamp("");
        }
        else {
            StringBuilder niceStamp = new StringBuilder(stamp);
            niceStamp.replace(6, 9, String.valueOf(0xFF & stampData[3]));
            niceStamp.insert(6, " v");
            niceStamp.insert(4, "-");
            niceStamp.insert(2, "-");
            niceStamp.insert(0, "20");
            romID.setEditStamp(niceStamp.toString());
        }
    }

    public void setRomID(RomID romID) {
        this.romID = romID;
    }

    public RomID getRomID() {
        return romID;
    }

    public String getRomIDString() {
        return romID.getXmlid();
    }

    public byte[] getBinary() {
    	return binData;
    }

    public void setDocument(Document d) {
    	this.doc = d;
    }
    public Document getDocument() {
    	return this.doc;
    }

    public void setDefinitionPath(File s) {
    	definitionPath = s;
    }

    public File getDefinitionPath() {
    	return definitionPath;
    }

    @Override
    public String toString() {
        String output = "";
        output = output + "\n---- Rom ----" + romID.toString();
        for(String s : tableNodes.keySet()) {
            output = output + tableNodes.get(s).getTable();
        }
        output = output + "\n---- End Rom ----";

        return output;
    }

    public String getFileName() {
        return fileName;
    }

    public Vector<Table> getTables() {
        Vector<Table> tables = new Vector<Table>();
        for(TableTreeNode tableNode : tableNodes.values()) {
            tables.add(tableNode.getTable());
        }
        Collections.sort(tables);
        return tables;
    }

    public HashMap<String, TableTreeNode> getTableNodes() {
        return this.tableNodes;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private void showChecksumFixPopup(TableTreeNode checksum) {
    	 Object[] options = {rb.getString("YES"), rb.getString("NO")};
         final String message = rb.getString("CHKSUMINVALID");

         int answer = showOptionDialog(
                 SwingUtilities.windowForComponent(checksum.getTable().getTableView()),
                 message,
                 rb.getString("CHECKSUMFIX"),
                 DEFAULT_OPTION,
                 QUESTION_MESSAGE,
                 null,
                 options,
                 options[0]);
         if (answer == 0) {
             //TODO: Move to Subaru checksum
             calculateRomChecksum(
                     binData,
                     checksum.getTable().getStorageAddress(),
                     checksum.getTable().getDataSize()
             );
         }
    }

    //Most of this function is useless now, since each Datacell is now responsible for each memory region
    //It is only used to correct the Subaru Checksum. Should be moved somewhere else TODO
    public byte[] saveFile() {

        final List<TableTreeNode> checksumTables = new ArrayList<TableTreeNode>();
        if(tableNodes.containsKey("checksum fix")) {
        	checksumTables.add(tableNodes.get("checksum fix"));
        }

        if (checksumTables.size() == 1) {
            final TableTreeNode checksum = checksumTables.get(0);
            byte count = binData[checksum.getTable().getStorageAddress() + 207];
            if (count == -1) {
                count = 1;
            }
            else {
                count++;
            }
            String currentDate = new SimpleDateFormat("yyMMdd").format(new Date());
            String stamp = String.format("%s%02x", currentDate, count);
            byte[] romStamp = asBytes(stamp);
            System.arraycopy(
                    romStamp,
                    0,
                    binData,
                    checksum.getTable().getStorageAddress() + 204,
                    4);
            setEditStamp(binData, checksum.getTable().getStorageAddress());
        }

        for (TableTreeNode checksum : checksumTables) {
            if (!checksum.getTable().isLocked()) {
                //TODO: Move to Subaru checksum
                calculateRomChecksum(
                        binData,
                        checksum.getTable().getStorageAddress(),
                        checksum.getTable().getDataSize()
                );
            }
            else if (checksum.getTable().isLocked() &&
                    !checksum.getTable().isButtonSelected()) {
                	showChecksumFixPopup(checksum);
            }
        }

        updateChecksum();
        return binData;
    }

    public void clearData() {
        super.removeAllChildren();

        // Hide and dispose all frames.
        for(TableTreeNode tableTreeNode : tableNodes.values()) {
            TableFrame frame = tableTreeNode.getFrame();

            TableUpdateHandler.getInstance().deregisterTable(tableTreeNode.getTable());

            // Quite slow and doesn't seem to be necessary after testing,
            // uncomment if you disagree

            //tableTreeNode.getTable().clearData();

            if(frame != null) {

	            frame.setVisible(false);

	            try {
	                frame.setClosed(true);
	            } catch (PropertyVetoException e) {
	                 // Do nothing.
	            }
	            frame.dispose();

	            if(frame.getTableView() != null) {
	            	frame.getTableView().setVisible(false);
		            frame.getTableView().setData(null);
	                frame.getTableView().setTable(null);
	                frame.setTableView(null);
	            }
            }

            tableTreeNode.setUserObject(null);
        }

        clearByteMapping();
        checksumManagers.clear();
        tableNodes.clear();
        binData = null;
        doc = null;
    }

    public void clearByteMapping() {
    	for(List<?> l: byteCellMapping.values())l.clear();

    	byteCellMapping.clear();
    	byteCellMapping = null;
    }

    public int getRealFileSize() {
        return binData.length;
    }

    public File getFullFileName() {
        return fullFileName;
    }

    public void setFullFileName(File fullFileName) {
        this.fullFileName = fullFileName;
        this.setFileName(fullFileName.getName());
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    @Override
    public DefaultMutableTreeNode getChildAt(int i) {
        return (DefaultMutableTreeNode) super.getChildAt(i);
    }

    public void addChecksumManager(ChecksumManager checksumManager) {
    	this.checksumManagers.add(checksumManager);
    }

    public int getNumChecksumsManagers() {
    	return checksumManagers.size();
    }

    public int validateChecksum() {
        int correctChecksums = 0;
        boolean valid = true;

        if (!checksumManagers.isEmpty()) {
            for(ChecksumManager cm: checksumManagers) {
            	int localCorrectCs = cm.validate(binData);

            	if (cm == null || cm.getNumberOfChecksums() != localCorrectCs) {
            		valid = false;
            	}
            	else {
            		correctChecksums+=localCorrectCs;
            	}
            }
        }

        if(!valid) {
        	showMessageDialog(null,
        			rb.getString("INVLAIDCHKSUM"),
                    rb.getString("CHKSUMFAIL"),
                    WARNING_MESSAGE);
        }

        return correctChecksums;
    }

    public int updateChecksum() {
    	int updatedCs = 0;

	    for(ChecksumManager cm: checksumManagers) {
	    	updatedCs+=cm.update(binData);
	    }

	    ECUEditorManager.getECUEditor().getStatusPanel().setStatus(
	    		String.format(rb.getString("CHECKSUMFIXED"), updatedCs, getTotalAmountOfChecksums()));

	    return updatedCs;
    }

    public int getTotalAmountOfChecksums() {
    	int cs = 0;

	    for(ChecksumManager cm: checksumManagers) {
	    	cs+=cm.getNumberOfChecksums();
	    }

	    return cs;
    }
}