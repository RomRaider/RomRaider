package enginuity.logger.utec.gui.mapTabs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class FuelJPanel extends JPanel{

	public FuelJPanel(){
		super(new BorderLayout());
		init();
		
	}
	
	public void init(){
		
        JTable table = new JTable(new UtecTableModel());
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //Add the scroll pane to this panel.
        this.add(scrollPane, BorderLayout.CENTER);

	}
	
	
}
