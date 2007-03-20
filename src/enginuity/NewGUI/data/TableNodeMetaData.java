package enginuity.NewGUI.data;

public class TableNodeMetaData {

	public static final int DATA1D = 0;
	public static final int DATA2D = 1;
	public static final int DATA3D = 3;
	public static final int CATEGORY = 4;
	public static final int RESERVED_ROOT = 5;
	
	
	
	private double maxValue;
	private double minValue;
	private Object[] ignoredValues;
	private boolean isInvertedColoring;
	private String tableName;
	private int dimensions;
	
	public TableNodeMetaData(int dimensions, double minValue, double maxValue, Object[] ignoredValues, boolean isInvertedColoring, String tableName) {
		this.dimensions = dimensions;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.ignoredValues = ignoredValues;
		this.isInvertedColoring = isInvertedColoring;
		this.tableName = tableName;
		
		System.out.println("Min:"+this.minValue+ " Max:"+this.maxValue + " Name:"+this.tableName+ " Inv:"+this.isInvertedColoring);
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
}
