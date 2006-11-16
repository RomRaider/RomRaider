package enginuity.newmaps.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class TableFrame extends JFrame {
    // Instance attributes used in this example
    private	JPanel		topPanel;
    private	Table		table;
    private	JScrollPane     scrollPane;

    private	String		columnNames[];
    private	String		dataValues[][];

    // Constructor of main frame
    public TableFrame() {
        // Set the frame characteristics
        setTitle("Advanced Table Application");
        setSize(300, 200);
        setBackground(Color.gray);
        // remove when i make a JInternalFrame
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create a panel to hold all other components
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        getContentPane().add(topPanel);

        // Create columns
        CreateColumns();
        CreateData();

        // Create a new table instance
        table = new Table(dataValues, columnNames);

        // Configure some of Table's paramters
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        // Change the selection colour
        table.setSelectionForeground(Color.white);
        table.setSelectionBackground(Color.red);

        // Add the table to a scrolling pane
        scrollPane = new JScrollPane(table);
        topPanel.add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public void CreateColumns() {
        // Create column string labels
        columnNames = new String[8];

        for( int iCtr = 0; iCtr < 8; iCtr++ ) {
            columnNames[iCtr] = "Col:" + iCtr;
        }
    }

    public void CreateData() {
        // Create data for each element
        dataValues = new String[100][8];

        for (int iY = 0; iY < 100; iY++) {
            for(int iX = 0; iX < 8; iX++) {
                    dataValues[iY][iX] = "" + iX + "," + iY;
            }
        }
    }
      
    public static void main( String args[] ) {
        TableFrame mainFrame = new TableFrame();
    } 
}
