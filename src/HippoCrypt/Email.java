package HippoCrypt;

import java.util.Date;

public class Email {
	public long uid;
	public String from;
	public String subject;
	public Date sentDate;
	public String body;

	public String folder;
	
	@Override public String toString () {
		return subject+" "+sentDate;
	}
}
