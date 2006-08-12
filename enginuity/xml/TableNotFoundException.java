package enginuity.xml;

public class TableNotFoundException extends Exception {
    public String getMessage() {
        return "Table not found.";
    }
}