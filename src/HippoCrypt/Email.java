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
		return NullHelper.help (subject, "<No subject>")+" "+NullHelper.help (sentDate, "<No date>");
	}
}
