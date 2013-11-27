package HippoCrypt;

import java.io.*;
import java.util.Properties;

public class ConfStore {
	File f;
	Properties p;
	boolean autoCommit = true;
	
	ConfStore (File f) throws FileNotFoundException, IOException {
		this.f = f;
		this.p = new Properties ();
		
		try {
    		FileInputStream fis = new FileInputStream (f);
    		p.load (fis);
    		fis.close ();
		} catch (FileNotFoundException fnfe) {
			// This is not a problem, so just eat it
		}
	}
	
	public String get (String key) {
		return p.getProperty (key);
	}
	
	public void commit () throws IOException {
		FileOutputStream fos = new FileOutputStream (f);
		p.store (fos, "");
		fos.close ();
	}
	
	public void remove (String key) throws IOException {
		p.remove (key);
		if (autoCommit) {
			commit ();
		}
	}
	
	public void setAutoCommit (boolean b) {
		autoCommit = b;
	}
	
	public void put (String key, String value) throws IOException {
		p.put (key, value);
		if (autoCommit) {
			commit ();
		}
	}
}
