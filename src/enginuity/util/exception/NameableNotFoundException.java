package enginuity.util.exception;

public class NameableNotFoundException extends Exception {
    
    private String name;
    
    public NameableNotFoundException(String name) {
        super(name);
        this.name = name;
    }
    
}