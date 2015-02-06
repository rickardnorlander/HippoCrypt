package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class AddressUtils {

	public static String addrArrToStr(InternetAddress [] addrs) {
		return addrArrToStr(Arrays.asList(addrs));
	}

	public static String addrArrToStr(List<InternetAddress> addrs) {
		if (addrs == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (Address a : addrs) {
			sb.append(sb.length() > 0 ? ", " : "");
			sb.append(a.toString());
		}
		return sb.toString();
	}

	
	public static List<InternetAddress> mergeAddresses (List<InternetAddress> ...in) throws AddressException {
		Set<String> emails = new HashSet<>();
		List<InternetAddress> ret = new ArrayList<InternetAddress> ();
		for (List<InternetAddress> la : in) {
			for (InternetAddress a : la) {
				if(emails.add(a.getAddress())) {
					ret.add(a);
				}
			}
		}
		return ret;
	}
}
