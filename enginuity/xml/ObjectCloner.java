package enginuity.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectCloner {
	// so that nobody can accidentally create an ObjectCloner object
	private ObjectCloner() {}

	// returns a deep copy of an object
	public static Object deepCopy(Object oldObj) throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos
					.toByteArray());
			ois = new ObjectInputStream(bin);
			// return the new object
			return ois.readObject();
		}
		finally {
			if (oos != null) {
				oos.close();
			}
			if (ois != null) {
				ois.close();
			}
		}
	}
}