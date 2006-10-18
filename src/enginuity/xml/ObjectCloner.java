package enginuity.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ObjectCloner {
    
    private ObjectCloner() {
    }

    // returns a deep copy of an object
    public static Object deepCopy(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
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
        }
    }
}