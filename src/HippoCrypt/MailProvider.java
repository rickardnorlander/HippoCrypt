package HippoCrypt;

import java.util.Properties;

public class MailProvider {
	public static class RetInfo {
		public String imapServer;
		public String username;
		public RetInfo (String imapServer, String username) {
			this.imapServer = imapServer;
			this.username = username;
		}
	}

	public static RetInfo getProvider (String email, Properties props) {
		if (email.endsWith ("@gmail.com")) {
			props.put("mail.store.protocol", "imaps");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			return new RetInfo ("imap.gmail.com", email);
		}
		if (email.endsWith ("@hotmail.com")) {
			props.put("mail.store.protocol", "imaps");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp-mail.outlook.com");
			props.put("mail.smtp.port", "587");
			return new RetInfo ("imap-mail.outlook.com", email);
		}
		if (email.endsWith ("@kth.se")) {
			props.put("mail.store.protocol", "imaps");
			props.put("mail.transport.protocol", "smtps");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.host", "smtp.kth.se");
			props.put("mail.smtp.port", "465");
			return new RetInfo ("webmail.kth.se", email.substring (0, email.length () - 7));
		}
		return null;
	}

}
