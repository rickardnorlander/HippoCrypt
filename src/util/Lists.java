package util;

import java.util.List;

public class Lists {
	public static String listToString (List<?> strings) {
		String str = "[";
		for(int i = 0; i < strings.size(); i++){
			str += strings.get(i);
			if(i < strings.size()-1) str += ", "
		}
		str += "]";
		return str;
	}
}
