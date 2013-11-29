package HippoCrypt;

import java.util.Properties;

public class MailProvider {
	public String imapServer;
	public String storeProtocol;
	public String smtpAuth;
	public String smtpStartTls;
	public String smtpServer;
	public String smtpPort;

	private MailProvider (String imapServer, String storeProtocol, String smtpAuth, String smtpStartTls, String smtpServer, String smtpPort) {
		this.imapServer = imapServer;
		this.storeProtocol = storeProtocol;
		this.smtpAuth = smtpAuth;
		this.smtpStartTls = smtpStartTls;
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
	}

	public static MailProvider getProvider (String email) {        
		if (email.endsWith ("@gmail.com")) {
    		return new MailProvider ("imap.gmail.com",  "imaps",  "true", "true", "smtp.gmail.com", "587");
		}
		if (email.endsWith ("@hotmail.com")) {
    		return new MailProvider ("imap-mail.outlook.com",  "imaps",  "true", "true", "smtp-mail.outlook.com", "587");
		}
		return null;
	}

}
