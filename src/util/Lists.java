package util;

import java.util.List;

public class Lists {
	public static String listToString (List<?> strings) {
		StringBuilder sb = new StringBuilder ();
		sb.append ("[");
		boolean v = false;
		for (Object s : strings) {
			if (v) sb.append (",");
			sb.append (s.toString ());
			v = true;
		}
		sb.append ("]");
		return sb.toString ();
	}
}
