package enginuity.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ObjectCloner {
    
    private ObjectCloner() {
    }

    // returns a deep copy of an object
    public static Object deepCopy(Object obj) throws Exception {
        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            try {
                ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
                try {
                    ObjectInputStream ois = new ObjectInputStream(bin);
                    try {
                        // serialize and pass the object
                        oos.writeObject(obj);
                        oos.flush();

                        // return the new object
                        return ois.readObject();

                    } finally {
                        ois.close();
                    }
                } finally {
                    bin.close();
                }
            } finally {
                oos.close();
            }
        } finally {
            bos.close();
        }*/
        
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();        
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deepCopy = ois.readObject();
        return deepCopy;*/
        
        //obj2DeepCopy must be serializable
        ObjectOutputStream outStream = null; 
        ObjectInputStream inStream = null; 

        try { 
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream(); 
            outStream = new ObjectOutputStream(byteOut); 
            outStream.writeObject(obj); 
            outStream.flush(); 
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            inStream = new ObjectInputStream(byteIn); 

            // read the serialized, and deep copied, object and return it 
            return inStream.readObject(); 
            
        } catch(Exception e) { 
            throw(e);
            
        } finally { 
            //always close your streams in finally clauses
            outStream.close(); 
            inStream.close(); 
        } 
        
        
    }
}