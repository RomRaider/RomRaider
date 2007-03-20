package enginuity.NewGUI.etable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import enginuity.NewGUI.data.TableNodeMetaData;

public class EInternalFrame extends JInternalFrame implements InternalFrameListener, ActionListener{
	
	private ETable eTable;
	
	public EInternalFrame(TableNodeMetaData tableMetaData, double[][] data, Dimension tableDimensions){
		super(tableMetaData.getTableName(), true, true, true, true);
		
		
		
		eTable = new ETable(tableMetaData, data);
		
		EToolBar toolBar = new EToolBar(tableMetaData, eTable);
		
		JScrollPane scrollPane = new JScrollPane(eTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
    	// Add internal frame
		this.setLayout(new BorderLayout());
		this.add(toolBar, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setSize(tableDimensions);
		this.toFront();
		this.setVisible(true);
		this.addInternalFrameListener(this);
	}
	
	public double[][] getCurrentData(){
		return this.eTable.getTheModel().getData();
	}

	public void internalFrameOpened(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameClosing(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameClosed(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameIconified(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameActivated(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameDeactivated(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
