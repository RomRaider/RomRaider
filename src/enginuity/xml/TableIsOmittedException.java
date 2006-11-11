package enginuity.xml;

public final class TableIsOmittedException extends Exception {

    public TableIsOmittedException() {
    }

    public String getMessage() {
        return "Table omitted.";
    }
}