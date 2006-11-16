package enginuity.util;

import enginuity.util.exception.NameableNotFoundException;
import java.util.Vector;

public class NamedSet {
    
    Vector<Nameable> objects = new Vector<Nameable>();
    
    public void add(Nameable n) {        
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(n.getName())) {
                objects.remove(i);
                objects.add(i, n);
                return;
            }
        }
        add(n);
    }
    
    public Nameable get(String name) throws NameableNotFoundException {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(name)) {
                return objects.get(i);
            }
        }    
        // Name not found, throw exception
        throw new NameableNotFoundException();
    }
    
    public Nameable get(Nameable n) throws NameableNotFoundException {
        return get(n.getName());
    }
    
    public int size() {
        return objects.size();
    }
    
    public void remove(Nameable n) throws NameableNotFoundException { 
        remove(n.getName());
    }
    
    public void remove(String name) throws NameableNotFoundException {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equalsIgnoreCase(name)) {
                objects.remove(i);
                return;
            }
        }            
        // Name not found, throw exception
        throw new NameableNotFoundException();
    }    
}