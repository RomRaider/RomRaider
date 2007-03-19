package enginuity.NewGUI.data;

public class TableNodeMetaData {
	private double maxValue;
	private double minValue;
	private Object[] ignoredValues;
	private boolean isInvertedColoring;
	private String tableName;
	
	public TableNodeMetaData(double minValue, double maxValue, Object[] ignoredValues, boolean isInvertedColoring, String tableName) {
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
}
