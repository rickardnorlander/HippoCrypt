package util;

import java.util.List;

public class Lists {
	public static String listToString (List<?> strings) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < strings.size(); i++){
			sb.append(strings.get(i));
			if(i < strings.size()-1) sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
}
