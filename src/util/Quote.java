package util;

import java.io.*;
import java.util.regex.*;

public class Quote {
	private static Pattern p = null;

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
