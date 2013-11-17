package util;

import java.util.List;

public class Lists {
	public static String listToString (List<?> strings) {
		StringBuffer sb = new StringBuffer ();
		sb.append ("[");
		boolean v = false;
		for (Object s : strings) {
			if (v) sb.append (",");
			sb.append (s.toString ());
		}
		sb.append ("]");
		return sb.toString ();
	}
}
