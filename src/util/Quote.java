package util;

import java.io.*;

public class Quote {
	public static String quote (String in) {
		try {
			BufferedReader bufReader = new BufferedReader (new StringReader (in));
			StringBuilder sb = new StringBuilder ();
			String line = null;
			while ((line = bufReader.readLine ()) != null) {
				sb.append ("> ");
				sb.append (line);
				sb.append ("\n");
			}
			return sb.toString ();
		} catch (IOException e) {
		}
		throw new RuntimeException ("This cannot happen");
	}
}
