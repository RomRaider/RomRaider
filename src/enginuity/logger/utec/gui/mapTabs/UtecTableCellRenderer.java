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
	private Object[] ignoredValues;
	private boolean isInvertedColoring;
	
	public UtecTableCellRenderer(double min, double max, Object[] ignoredValues, boolean isInvertedColoring){
		this.min = min;
		this.max = max;
		this.ignoredValues = ignoredValues;
		this.isInvertedColoring = isInvertedColoring;
	}
	
	/**
	 * Called when table needs cell rendering information. Cell logic on color values goes here.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		
		if(isSelected){
			cell.setBackground(Color.BLUE);
		}else{
			if(value instanceof Double){
				ColorTable.initColorTable(min, max);
				if(this.isInvertedColoring){
					ColorTable.initColorTable(max, min);
				}
				Color3f theColor = ColorTable.getColor((Double)value);
				cell.setBackground(new Color(theColor.x, theColor.y, theColor.z));
				
				// If out of range color cell red
				if((Double)value < min || (Double)value > max){
					cell.setBackground(Color.RED);
				}
			}
			

			// Iterate through the ignored values, paint them gray
			for(int i = 0; i < ignoredValues.length; i++){
				
				// Double ignored values
				if((value instanceof Double) && (ignoredValues[i] instanceof Double)){
					Double doubleValue = (Double)value;
					Double ignoredValue = (Double)ignoredValues[i];
					
					if((doubleValue - ignoredValue) == 0){
						cell.setBackground(Color.GRAY);
					}
				}
				
				// Maybe add string value detection as needed
			}
		}
		
		
		
		
		return cell;
	}
}
