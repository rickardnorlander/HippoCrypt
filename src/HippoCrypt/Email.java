package HippoCrypt;

import java.util.Date;

import util.NullHelper;

public class Email {
	public long uid;
	public String from;
	public String subject;
	public Date sentDate;
	public String body;

	public String folder;
	
	@Override public String toString () {
		String s1 = NullHelper.help (subject, "<No subject>")+" "+NullHelper.help (sentDate, "<No date>");
		return "<html><body>"+s1.replaceAll ("<", "&lt;").replaceAll (">", "&gt;")+"</body></html>";
	}
}
