package util;

public class NullHelper {
	public static <T> T help (T t, T t2) {
		return t != null ? t : t2;
	}
}
