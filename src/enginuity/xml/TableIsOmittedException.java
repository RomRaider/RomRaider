package enginuity.xml;

public class TableIsOmittedException extends Exception {

    public TableIsOmittedException() {
    }

    public String getMessage() {
        return "Table omitted.";
    }
}