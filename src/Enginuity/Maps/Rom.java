//base ECU version

package Enginuity.Maps;

import Enginuity.ECUEditor;
import Enginuity.XML.TableNotFoundException;
import java.util.Vector;
import javax.swing.JOptionPane;

//import Enginuity.

public class Rom {
    
    private RomID romID = new RomID();
    private String fileName = "";
    private Vector<Table> tables = new Vector<Table>();
    private ECUEditor container;
    private byte[] binData;
    
    public Rom() {
    }
    
    public void addTable(Table table) {
        boolean tableExists = false;
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(table.getName())) {
                tables.remove(i);
                tables.add(table);
                tableExists = true;
                break;
            }
        }
        if (!tableExists) {
            tables.add(table);
        }
    }
    
    public Table getTable(String tableName) throws TableNotFoundException {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(tableName)) {
                return tables.get(i);
            }
        }
        throw new TableNotFoundException();
    }
    
    public void populateTables(byte[] binData) {
        this.binData = binData;
        for (int i = 0; i < getTables().size(); i++) {
            try {
                tables.get(i).populateTable(binData);
            } catch (ArrayIndexOutOfBoundsException ex) {                
                new JOptionPane().showMessageDialog(container, "Storage address for table \"" + tables.get(i).getName() + "\" is out of bounds.\nPlease check ECU definition file.", "ECU Definition Error", JOptionPane.ERROR_MESSAGE);
                tables.remove(i);
            } catch (NullPointerException ex) {
                new JOptionPane().showMessageDialog(container, "There was an error loading table " + tables.get(i).getName(), "ECU Definition Error", JOptionPane.ERROR_MESSAGE);
                tables.remove(i);
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
    }
    
    public byte[] saveFile() {
        for (int i = 0; i < tables.size(); i++) {
            tables.get(i).saveFile(binData);
        }
        return binData;
    }
    
    public void closeImage() {
        for (int i = 0; i < tables.size(); i++) {
            tables.get(i).getFrame().dispose();
        }
    }
}