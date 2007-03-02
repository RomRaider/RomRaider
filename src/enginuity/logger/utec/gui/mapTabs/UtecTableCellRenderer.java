package enginuity.logger.utec.gui.mapTabs;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.vecmath.Color3f;

import com.ecm.graphics.tools.ColorTable;

public class UtecTableCellRenderer extends DefaultTableCellRenderer{
	private double min;
	private double max;
	
	public UtecTableCellRenderer(double min, double max){
		this.min = min;
		this.max = max;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		
		if(isSelected){
			cell.setBackground(Color.BLUE);
		}else{
			if(value instanceof Double){
				//System.out.println("Amount:"+(Double)value);
			}
			ColorTable.initColorTable(min, max);
			Color3f theColor = ColorTable.getColor((Double)value);
			
			cell.setBackground(new Color(theColor.x, theColor.y, theColor.z));
		}
		
		
		
		return cell;
	}
}
