package enginuity.NewGUI.data;

import java.text.DecimalFormat;

import enginuity.NewGUI.interfaces.TuningEntity;

public class TableMetaData {

	public static final int DATA1D = 0;
	public static final int DATA2D = 1;
	public static final int DATA3D = 3;
	public static final int CATEGORY = 4;
	public static final int RESERVED_ROOT = 5;
	
	
	
	private double maxValue;
	private double minValue;
	private String[] columnLabels;
	private String[] rowLabels;
	private Object[] ignoredValues;
	private boolean isInvertedColoring;
	private String tableName;
	private String tableIdentifier;
	private String tableGroup;
	private int dimensions;
	private TuningEntity parentTuningEntity;
	private DecimalFormat formatter = new DecimalFormat( "#.0" );
	
	public TableMetaData(int dimensions, double minValue, double maxValue, Object[] ignoredValues, String[] columnLabels, String[] rowLabels, boolean isInvertedColoring, String tableName, String tableIdentifier, String tableGroup, TuningEntity parentTuningEntity) {
		this.dimensions = dimensions;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.ignoredValues = ignoredValues;
		this.columnLabels = columnLabels;
		this.rowLabels = rowLabels;
		this.isInvertedColoring = isInvertedColoring;
		this.tableName = tableName;
		this.tableIdentifier = tableIdentifier;
		this.tableGroup = tableGroup;
		this.parentTuningEntity = parentTuningEntity;
		
		// System.out.println("Min:"+this.minValue+ " Max:"+this.maxValue + " Name:"+this.tableName+ " Inv:"+this.isInvertedColoring);
	}

	public Object[] getIgnoredValues() {
		return ignoredValues;
	}

	public boolean isInvertedColoring() {
		return isInvertedColoring;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public String getTableName() {
		return tableName;
	}

	public int getNodeType() {
		return dimensions;
	}

	public String getTableIdentifier() {
		return tableIdentifier;
	}

	public String[] getColumnLabels() {
		return columnLabels;
	}

	public String[] getRowLabels() {
		return rowLabels;
	}

	public TuningEntity getParentTuningEntity() {
		return parentTuningEntity;
	}

	public String getTableGroup() {
		return tableGroup;
	}

	
	// Getters and setters for not so commonly used items, not included in constructor
	
	public DecimalFormat getFormatter() {
		return formatter;
	}

	public void setFormatter(DecimalFormat formatter) {
		this.formatter = formatter;
	}
}
