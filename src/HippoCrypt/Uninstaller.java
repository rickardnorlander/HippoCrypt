package HippoCrypt;

import java.io.File;
import java.util.prefs.*;

public class Uninstaller {
	
	public static void recursiveDelete (File f) {
		if (f.isDirectory ()) {
			for (File f2 : f.listFiles ())
				recursiveDelete (f2);
		}
		f.delete ();
	}
	
	public static void main (String [] args) {
		File folder = new File (System.getProperty("user.home"), "HippoCrypt");
		recursiveDelete(folder);
		
		try {
			Preferences p = Preferences.userNodeForPackage (HippoCrypt.class);
			p.removeNode ();
		} catch (SecurityException | BackingStoreException se) {
		}
		
		System.out.println ("The keyring HippoCryptPubRing.gpg can be removed, as well the private key in your default keyring, "
				+ "but this is not recommended\n");
	}
}
