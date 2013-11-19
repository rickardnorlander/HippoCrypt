package HippoCrypt;

import java.util.prefs.*;

/**
 * List java preferences. For testing
 */
public class ListPrefs {
	public static void main (String args []) throws BackingStoreException {
		Preferences p = Preferences.userNodeForPackage (HippoCrypt.class);
		for (String s : p.keys ()) {
			System.out.println (s+ " "+p.get (s, null));
		}
//		p.put ("gpg-fp", "3C9DD01D");
//		p.remove ("key-rickardnorlander@gmail.com");
//		p.remove ("key-klingande@gmail.com");
	}
}
