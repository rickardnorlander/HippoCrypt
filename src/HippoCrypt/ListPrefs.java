package HippoCrypt;

import java.util.prefs.*;

public class ListPrefs {
	public static void main (String args []) throws BackingStoreException {
		Preferences p = Preferences.userNodeForPackage (HippoCrypt.class);
		for (String s : p.keys ()) {
			System.out.println (s+ " "+p.get (s, null));
		}
		p.put ("key-rickardnorlander@gmail.com", "3C9DD01D");
		p.remove ("key-klingande@gmail.com");
	}
}
