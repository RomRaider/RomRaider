/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.swing.JProgressPane;
import com.romraider.xml.TableNotFoundException;
import org.apache.log4j.Logger;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Rom implements Serializable {
    private static final long serialVersionUID = 7865405179738828128L;
    private static final Logger LOGGER = Logger.getLogger(Rom.class);
    private RomID romID = new RomID();
    private String fileName = "";
    private File fullFileName = new File(".");
    private Vector<Table> tables = new Vector<Table>();
    private ECUEditor container;
    private byte[] binData;
    private String parent = "";
    private boolean isAbstract = false;

    public Rom() {
    }

    public void addTable(Table table) {
        boolean found = false;
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(table.getName())) {
                tables.remove(i);
                tables.add(i, table);
                found = true;
                break;
            }
        }
        if (!found) {
            tables.add(table);
        }
    }

    public Table getTable(String tableName) throws TableNotFoundException {
        for (Table table : tables) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        throw new TableNotFoundException();
    }

    public List<Table> findTables(String regex) {
        List<Table> result = new ArrayList<Table>();
        for (Table table : tables) {
            String name = table.getName();
            if (name.matches(regex)) result.add(table);
        }
        return result;
    }

    public void removeTable(String tableName) {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(tableName)) {
                tables.remove(i);
            }
        }
    }

    public void populateTables(byte[] binData, JProgressPane progress) {
        this.binData = binData;
        for (int i = 0; i < getTables().size(); i++) {

            // update progress
            int currProgress = (int) ((double) i / (double) getTables().size() * 40);
            progress.update("Populating tables...", 40 + currProgress);

            Table table = tables.get(i);
            try {
                // if storageaddress has not been set (or is set to 0) omit table
                if (table.getStorageAddress() != 0) {
                    try {
                        table.populateTable(binData);
                        TableUpdateHandler.getInstance().registerTable(table);
                    } catch (ArrayIndexOutOfBoundsException ex) {

                        LOGGER.error(table.getName() +
                                " type " + table.getType() + " start " +
                                table.getStorageAddress() + " " + binData.length + " filesize", ex);

                        // table storage address extends beyond end of file
                        JOptionPane.showMessageDialog(container, "Storage address for table \"" + table.getName() +
                                "\" is out of bounds.\nPlease check ECU definition file.", "ECU Definition Error", JOptionPane.ERROR_MESSAGE);
                        tables.removeElementAt(i);
                        i--;

                    }

                } else {
                    tables.remove(i);
                    // decrement i because length of vector has changed
                    i--;
                }

            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(container, "There was an error loading table " + table.getName(), "ECU Definition Error", JOptionPane.ERROR_MESSAGE);
                tables.removeElementAt(i);
            }
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

    public String toString() {
        String output = "";
        output = output + "\n---- Rom ----" + romID.toString();
        for (int i = 0; i < getTables().size(); i++) {
            output = output + getTables().get(i);
        }
        output = output + "\n---- End Rom ----";

        return output;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Vector<Table> getTables() {
        return tables;
    }

    public ECUEditor getContainer() {
        return container;
    }

    public void setContainer(ECUEditor container) {
        this.container = container;
        // apply settings to tables
        for (Table table : tables) {
            table.applyColorSettings(container.getSettings());
            //tables.get(i).resize();
            table.getFrame().pack();
        }
    }

    public byte[] saveFile() {
        for (Table table : tables) {
            table.saveFile(binData);
        }
        return binData;
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
        for (Table table : tables) {
            table.getFrame().updateFileName();
        }
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
}