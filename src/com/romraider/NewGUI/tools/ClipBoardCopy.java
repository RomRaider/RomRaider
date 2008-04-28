package com.romraider.NewGUI.tools;

import com.romraider.NewGUI.etable.ETable;
import org.apache.log4j.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The
 * clipboard data format used by the adapter is compatible with the clipboard
 * format used by Excel. This provides for clipboard interoperability between
 * enabled JTables and Excel.
 */
public class ClipBoardCopy implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(ClipBoardCopy.class);
    private String rowstring;
    private String value;
    private Clipboard clipBoard;
    private StringSelection stringSelection;
    private ETable eTable;

    /**
     * The Excel Adapter is constructed with a JTable on which it enables
     * Copy-Paste and acts as a Clipboard listener.
     */
    public ClipBoardCopy(ETable myJTable) {
        eTable = myJTable;
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);

        eTable.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
        eTable.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);

        clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareTo("Copy") == 0) {
            copySelectedTableData();
        }
        if (e.getActionCommand().compareTo("Paste") == 0) {
            pasteTableData();
        }
    }


    public void copySelectedTableData() {
        LOGGER.debug("Excel copy");
        StringBuffer sbf = new StringBuffer();
        // Check to ensure we have selected only a contiguous block of
        // cells
        int numcols = eTable.getSelectedColumnCount();
        int numrows = eTable.getSelectedRowCount();
        int[] rowsselected = eTable.getSelectedRows();
        int[] colsselected = eTable.getSelectedColumns();

        if (rowsselected.length == 0 || colsselected.length == 0) {
            LOGGER.debug("Clipboardcopy empty selection region.");
            return;
        }

        if (!((numrows - 1 == rowsselected[rowsselected.length - 1]
                - rowsselected[0] && numrows == rowsselected.length) && (numcols - 1 == colsselected[colsselected.length - 1]
                - colsselected[0] && numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                sbf.append(eTable.getValueAt(rowsselected[i], colsselected[j]));
                if (j < numcols - 1)
                    sbf.append("\t");
            }
            sbf.append("\n");
        }
        stringSelection = new StringSelection(sbf.toString());
        clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(stringSelection, stringSelection);
    }


    public void pasteTableData() {
        LOGGER.debug("Pasting data");

        if (eTable.getSelectedRowCount() == 0) {
            this.pasteEntireTable();
            return;
        }

        int startRow = (eTable.getSelectedRows())[0];
        int startCol = (eTable.getSelectedColumns())[0];

        try {
            String trstring = (String) (clipBoard.getContents(this).getTransferData(DataFlavor.stringFlavor));
            LOGGER.debug("String is:" + trstring);
            StringTokenizer st1 = new StringTokenizer(trstring, "\n");
            for (int i = 0; st1.hasMoreTokens(); i++) {
                rowstring = st1.nextToken();


                StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                for (int j = 0; st2.hasMoreTokens(); j++) {
                    value = (String) st2.nextToken();

                    if (value.startsWith("[TABLE:")) {
                        this.pasteEntireTable();
                        return;
                    }

                    if (startRow + i < eTable.getRowCount() && startCol + j < eTable.getColumnCount()) {
                        eTable.setValueAt(value, startRow + i, startCol + j);
                    }

                    LOGGER.debug("Putting " + value + "atrow=" + startRow + i + "column=" + startCol + j);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error pasting data", ex);
        }
    }


    private void pasteEntireTable() {
        LOGGER.debug("Trying to Paste Entire Table");
        int startRow = 0;
        int startCol = 0;

        int numcols = eTable.getColumnCount();
        int numrows = eTable.getRowCount();

        if (numcols == 0 || numrows == 0) {
            LOGGER.debug("Nothing to paste");
            return;
        }

        Double[][] tempData = new Double[numcols][numrows];

        try {
            String trstring = (String) (clipBoard.getContents(this).getTransferData(DataFlavor.stringFlavor));

            LOGGER.debug("String is:" + trstring);
            StringTokenizer st1 = new StringTokenizer(trstring, "\n");
            for (int i = 0; st1.hasMoreTokens(); i++) {
                rowstring = st1.nextToken();

                // Skip the first row
                if (i == 0) {
                    if (!rowstring.startsWith("[TABLE:")) {
                        LOGGER.debug("Not an entire tables worth of data in clipboard");
                        return;
                    }
                    rowstring = st1.nextToken();
                }

                StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                for (int j = 0; st2.hasMoreTokens(); j++) {
                    value = (String) st2.nextToken();
                    // Always skip the first column
                    if (j == 0) {
                        value = (String) st2.nextToken();
                    }

                    if (startRow + i < eTable.getRowCount() && startCol + j < eTable.getColumnCount()) {
                        tempData[j][i] = Double.parseDouble(value);
                        //eTable.setValueAt(value, startRow + i, startCol+ j);
                    }

                    LOGGER.debug("Putting " + value + "atrow=" + startRow + i + "column=" + startCol + j);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error pasting table", ex);
        }

        // Set the new data
        eTable.replaceAlltableData(tempData);
    }


    public void copyEntireTable() {
        LOGGER.debug("Excel copy");
        StringBuffer sbf = new StringBuffer();
        // Check to ensure we have selected only a contiguous block of
        // cells
        int numcols = eTable.getColumnCount();
        int numrows = eTable.getRowCount();

        LOGGER.debug("Rows:" + numrows + "  Cols:" + numcols);

        String[] rowLabels = eTable.getTableMetaData().getRowLabels();
        String[] columnLabels = eTable.getTableMetaData().getColumnLabels();
        String tableName = "[TABLE: " + eTable.getTableMetaData().getTableName() + "]";

        // Add table name and column headers
        for (int i = 0; i < numcols + 1; i++) {
            if (i == 0) {
                sbf.append(tableName);
            } else {
                sbf.append(columnLabels[i - 1]);
            }
            if (i < numcols) {
                sbf.append("\t");
            }
        }
        sbf.append("\n");


        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols + 1; j++) {
                if (j == 0) {
                    sbf.append(rowLabels[i]);
                } else {
                    sbf.append(eTable.getValueAt(i, j - 1));
                }
                if (j < numcols) {
                    sbf.append("\t");
                }
            }
            sbf.append("\n");
        }


        stringSelection = new StringSelection(sbf.toString());
        clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(stringSelection, stringSelection);
    }
}
