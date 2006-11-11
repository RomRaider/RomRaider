package enginuity.util;

public final class AxisRange {
    private final int startIndex;
    private final int endIndex;

    public AxisRange(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

}
