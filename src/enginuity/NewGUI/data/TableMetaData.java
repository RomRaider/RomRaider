package enginuity.NewGUI.data;

import java.awt.Dimension;
import java.text.DecimalFormat;

import enginuity.NewGUI.interfaces.TuningEntity;

public class TableMetaData {
	// Type of table
	public static final int DATA_1D = 0;
	public static final int DATA_2D = 1;
	public static final int DATA_3D = 3;
	public static final int MAP_SET_ROOT = 4;
	public static final int RESERVED_ROOT = 5;
	public static final int CATEGORY = 6;
	
	//Frame Dimensions
	private static final Dimension Data3DDimension = new Dimension(500, 500);
	private static final Dimension Data2DDimension = new Dimension(500, 200);
	private static final Dimension Data1DDimension = new Dimension(500, 60);
	
	
	// **********************
	// Constructor set values
	// **********************
	
	// Table data related
	private double maxValue;
	private double minValue;
	private Object[] ignoredValues;
	private boolean isInvertedColoring;
	
	// Table labels etc
	private String tableName;
	private String[] columnLabels;
	private String[] rowLabels;
	private String xAxisLabel;
	private String yAxisLabel;
	private String tableIdentifier;
	private String tableGroup;
	
	// Is this a table1d, table2d, table3d
	private int dimensions;
	
	// What tuning entity should table decribed by this meta data refer to?
	private TuningEntity parentTuningEntity;
	
	// Optional setters
	private DecimalFormat formatter = new DecimalFormat( "#.0" );
	
	public TableMetaData(int dimensions, double minValue, double maxValue, Object[] ignoredValues, String[] columnLabels, String[] rowLabels, boolean isInvertedColoring, String tableName, String xAxisLabel, String yAxisLabel, String tableIdentifier, String tableGroup, TuningEntity parentTuningEntity) {
		this.dimensions = dimensions;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.ignoredValues = ignoredValues;
		this.columnLabels = columnLabels;
		this.rowLabels = rowLabels;
		this.isInvertedColoring = isInvertedColoring;
		this.tableName = tableName;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		this.tableIdentifier = tableIdentifier;
		this.tableGroup = tableGroup;
		this.parentTuningEntity = parentTuningEntity;	
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

	public String getXAxisLabel() {
		return xAxisLabel;
	}

	public String getYAxisLabel() {
		return yAxisLabel;
	}

	public static Dimension getData1DDimension() {
		return Data1DDimension;
	}

	public static Dimension getData2DDimension() {
		return Data2DDimension;
	}

	public static Dimension getData3DDimension() {
		return Data3DDimension;
	}
}
