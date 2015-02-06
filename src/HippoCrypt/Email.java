package HippoCrypt;

import java.util.*;

import javax.mail.Part;
import javax.mail.internet.InternetAddress;

import util.NullHelper;

public class Email {
	public long uid;
	public String from;
	public String subject;
	public Date sentDate;
	public String body;

	public String folder;
	public List<Attachment> attachments;
	List<InternetAddress> to;
	List<InternetAddress> cc;
	List<InternetAddress> replyTo;

	public static class Attachment {
		String filename;
		Part part;
		boolean encrypted;
	}

	
	@Override public String toString () {
		String s1 = NullHelper.help (subject, "<No subject>")+" "+NullHelper.help (sentDate, "<No date>");
		return "<html><body>"+s1.replaceAll ("<", "&lt;").replaceAll (">", "&gt;")+"</body></html>";
	}
}
