package enginuity.NewGUI.etable.dataJPanel;

import java.awt.BorderLayout;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import enginuity.NewGUI.data.TableMetaData;

public class DataJPanel1DString extends JPanel implements DataJPanelInterface{
	
	private TableMetaData tableMetaData;
	private String initialStringValue;
	private JTextArea dataTextArea;
	
	public DataJPanel1DString(TableMetaData tableMetaData, Object[][] data) {
		this.tableMetaData = tableMetaData;
		this.initialStringValue = (String)data[0][0];
		
		this.setLayout(new BorderLayout());
		
		dataTextArea = new JTextArea((String)data[0][0]);
		JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
		dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		dataScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		this.add(dataScrollPane, BorderLayout.CENTER);
	}
	
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return new JToolBar();
	}

	public JMenuBar getMenuBar() {
		// TODO Auto-generated method stub
		return new JMenuBar();
	}

	public boolean dataChanged() {
		// TODO Auto-generated method stub
		return this.initialStringValue.equals(this.dataTextArea.getText());
	}

	public void copySelectedTableData() {
		// TODO Auto-generated method stub
		
	}

	public void copyEntireTable() {
		// TODO Auto-generated method stub
		
	}

	public void pasteTableData() {
		// TODO Auto-generated method stub
		
	}

	public void setClosed(boolean value) {
		// TODO Auto-generated method stub
		
	}

	public void revertDataState() {
		// TODO Auto-generated method stub
		
	}

	public void saveDataState() {
		// TODO Auto-generated method stub
		
	}

	public void replaceData(Object[][] newData) {
		// TODO Auto-generated method stub
		this.dataTextArea.setText((String)newData[0][0]);
	}

	public Object[][] getData() {
		Object[][] temp = new Object[1][1];
		temp[0][0] = this.dataTextArea.getText();
		return temp;
	}

}
