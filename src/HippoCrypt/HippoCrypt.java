package HippoCrypt;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JOptionPane;
import javax.swing.tree.*;

import org.apache.commons.io.IOUtils;

import HippoCrypt.GPG.GPGData;
import util.*;

public class HippoCrypt {

	private static final String PASSWORD = "password";
	final static String PREF_EMAIL = "email";
	final static String PREF_GPG_FP = "gpg-fp";
	final static String PREF_GPG_PASS = "gpg-pass";


	Session session = null;
	Store store = null;
	GPGData gpgdata;
	String username;
	Properties props = null;


	private static String askEmail () {
		return JOptionPane.showInputDialog("Email"); 
	}

	public static String getEmail (Preferences prefs) {
		String email = prefs.get(PREF_EMAIL, null);
		if (email != null)
			return email;

		email = askEmail ();
		prefs.put (PREF_EMAIL, email);
		return email;
	}


	public static GPGData getGPGData (String email, Preferences prefs) throws IOException, InterruptedException {
		GPGData ret = new GPGData ();
		ret.fingerprint = prefs.get (PREF_GPG_FP, null);
		if(ret.fingerprint == null) {
			ret = GPG.genGPG ("A", "a@a.com", "password");
			prefs.put (PREF_GPG_FP, ret.fingerprint);
			prefs.put (PREF_GPG_PASS, ret.pass);
			prefs.put ("key-"+email, ret.fingerprint);
			return ret;
		}
		ret.pass = prefs.get (PREF_GPG_FP, null);
		if (ret.pass == null) {
			ret.pass = PasswordDialog.askPass();
		}
		return ret;
	}

	public void sendMail (String to, String subject, String pubkey, String body) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("rickardnorlander@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(subject);

			if (pubkey != null) {

				// Construct encrypted part

				MimeBodyPart pgppart = new MimeBodyPart();
				pgppart.setContent(GPG.encrypt (pubkey, body), "text/pgp; charset=utf-8");

				// Construct message for incompatible readers

				MimeBodyPart textPart = new MimeBodyPart();
				textPart.setText("This is an encrypted email sent with HippoCrypt.", "utf-8");

				// Construct multipart/alternative with the previous parts

				Multipart alternative = new MimeMultipart("alternative");
				alternative.addBodyPart(textPart);
				alternative.addBodyPart(pgppart);
				MimeBodyPart alternativeBody = new MimeBodyPart ();
				alternativeBody.setContent (alternative);

				// Add our public key

				MimeBodyPart attachment = new MimeBodyPart ();
				attachment.setContent(GPG.getArmoredPublicKey (gpgdata.fingerprint), "application/octet-stream");
				attachment.setFileName ("publickey.asc");

				// Create the a multipart/mixed containing message and attachment

				Multipart outer = new MimeMultipart("mixed");
				outer.addBodyPart (alternativeBody);
				outer.addBodyPart (attachment);

				message.setContent(outer);
			} else {
				Multipart multiPart = new MimeMultipart("mixed");

				MimeBodyPart textPart = new MimeBodyPart ();
				textPart.setText(body, "utf-8");

				MimeBodyPart attachment = new MimeBodyPart ();
				attachment.setContent(GPG.getArmoredPublicKey (gpgdata.fingerprint), "application/octet-stream");
				attachment.setFileName ("publickey.asc");

				multiPart.addBodyPart (textPart);
				multiPart.addBodyPart (attachment);

				message.setContent(multiPart);
			}

			Transport.send(message);
			System.out.println("Done");
		} catch (MessagingException | IOException | InterruptedException e) {
		}
	}

	public DefaultMutableTreeNode recursiveList (Folder f) throws MessagingException {
		if (!f.exists ())
			return null;
		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode (f.getName());

		int type = f.getType ();
		if ((type & Folder.HOLDS_FOLDERS) != 0) {
			for (Folder sub : f.list ()) {
				DefaultMutableTreeNode subnode = recursiveList (sub);
				if (subnode != null)
					dmtn.add (subnode);
			}
		}
		return dmtn;
	}


	public class Attachment {
		String filename;
		InputStream contentStream;
	}

	public class MyMessage {
		List<String> text;
		List<Attachment> attachments;
		List<Boolean> isEncrypted;
	}

	/**
	 * Return the primary text content of the message.
	 */
	private MyMessage parseMessage(Part p) throws
	MessagingException, IOException {
		if (Part.ATTACHMENT.equalsIgnoreCase (p.getDisposition ())) {
			Attachment a = new Attachment ();
			a.filename = new String(NullHelper.help (p.getFileName (), "NONAME").toString().getBytes(), "UTF-8");
			a.contentStream = p.getInputStream ();

			MyMessage ret = new MyMessage ();
			ret.attachments = Collections.singletonList (a);
			ret.text = Collections.EMPTY_LIST;
			ret.isEncrypted = Collections.EMPTY_LIST;
			return ret;
		}
		if (p.isMimeType("text/pgp")) {

			MyMessage ret = new MyMessage ();
			ret.text = Collections.singletonList (IOUtils.toString((InputStream) p.getContent(), "UTF-8"));
			ret.isEncrypted = Collections.singletonList (true);
			ret.attachments = Collections.EMPTY_LIST;
			return ret;
		}
		if (p.isMimeType("text/plain")) {
			MyMessage ret = new MyMessage ();
			ret.text = Collections.singletonList (p.getContent().toString ());
			ret.isEncrypted = Collections.singletonList (false);
			ret.attachments = Collections.EMPTY_LIST;
			return ret;
		}

		if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart)p.getContent();
			for (int i = mp.getCount()-1; i >=0; i--) {
				MyMessage ret = parseMessage(mp.getBodyPart (i));
				if (ret != null)
					return ret;
			}
			return null;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart)p.getContent();

			MyMessage ret = new MyMessage ();
			ret.text = new ArrayList<>();
			ret.isEncrypted = new ArrayList<>();
			ret.attachments = new ArrayList<>();
			for (int i = 0; i < mp.getCount(); i++) {
				MyMessage sub = parseMessage(mp.getBodyPart(i));
				if (sub != null) {
					ret.text.addAll (sub.text);
					ret.isEncrypted.addAll (sub.isEncrypted);
					ret.attachments.addAll (sub.attachments);
				}
			}
			return ret;
		}
		return null;
	}

	public Email loadAnEmail (String folder, int n) {
		Email ret = new Email ();
		Folder f = null;
		try {
			f = store.getFolder (folder);
			f.open (Folder.READ_ONLY);

			Message m = f.getMessage (n);

			ret.from = util.Lists.listToString (Arrays.asList (m.getFrom ()));
			ret.sentDate = m.getSentDate ().toString ();
			ret.subject = m.getSubject ();

			MyMessage mm = parseMessage (m);

			StringBuffer body = new StringBuffer ();
			for (int i = 0; i < mm.text.size (); ++i) {
				System.out.println("Part "+i);
				if (mm.isEncrypted.get (i)) {
					body.append (GPG.decrypt(mm.text.get (i), PASSWORD));
				} else {
					body.append (mm.text.get (i));
				}
				body.append ("\n");
			}
			ret.body = body.toString ();
			for (int i = 0; i < mm.attachments.size (); ++i) {
				if (mm.attachments.get (i).filename.equals ("publickey.asc")) {
					System.out.println("Has public key!!");

					String key = IOUtils.toString (mm.attachments.get (i).contentStream, "UTF-8");
					for (Address a: m.getFrom ()) {
						if (a instanceof InternetAddress) {
							String fromemail = ((InternetAddress) a).getAddress ();
							maybeAddPublicKey (fromemail, key);
						}
					}
				}
			}
		} catch (IOException | MessagingException | InterruptedException e) {
		}
		if (f != null && f.isOpen ()) {
			try {
				f.close (false);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private void maybeAddPublicKey (String fromemail, String key) throws IOException, InterruptedException {
		Preferences prefs = Preferences.userNodeForPackage(HippoCrypt.class);
		String prevFingerprint = prefs.get("key-"+fromemail, null);
		if (prevFingerprint != null) {
			System.out.println(fromemail+" was not updated: already have key");
			return;
		}
		String fingerprint = GPG.importKey (key);
		if (fingerprint != null)
			prefs.put ("key-"+fromemail, fingerprint);
	}

	public List<EmailRef> loadSomeHeaders (String folderName) {
		Folder f = null;
		List<EmailRef> ret = new ArrayList<> ();
		try {

			f = store.getFolder (folderName);
			f.open (Folder.READ_ONLY);
			int mode = f.getMode ();
			if ((mode & Folder.HOLDS_MESSAGES) == 0) {
				f.close (false);
				return ret;
			}
			int n = f.getMessageCount ();
			for (int i = Math.max (n-10, 1); i <= n; ++i) {
				Message message = f.getMessage (i);
				EmailRef a = new EmailRef ();
				a.date = message.getSentDate ().toString ();
				a.subject = message.getSubject ();
				a.from = util.Lists.listToString (Arrays.asList (message.getFrom ()));
				a.n = i;
				a.folder = folderName;

				ret.add (a);
			}
			f.close (false);
		} catch (MessagingException e) {
			try {
				if (f != null && f.isOpen ()) {
					f.close (false);
				}
			} catch (MessagingException e1) {
				e1.printStackTrace();
			}
		}
		return ret;
	}


	public void doStuff () throws IOException, InterruptedException, MessagingException {
		Preferences prefs = Preferences.userNodeForPackage(HippoCrypt.class);

		username = getEmail (prefs);
		gpgdata = getGPGData (username, prefs);

		DefaultMutableTreeNode root = null;

		String password_prompt = "Password"; 
		while (true) {
			try {
				final String password = PasswordDialog.askPass(password_prompt);
				if (password == null)
					return;
				if (props == null) {
					props = System.getProperties();

					props.put("mail.store.protocol", "imaps");
					props.put("mail.smtp.auth", "true");
					props.put("mail.smtp.starttls.enable", "true");
					props.put("mail.smtp.host", "smtp.gmail.com");
					props.put("mail.smtp.port", "587");
				}
				if (session == null) {
					session = Session.getInstance(props, 
							new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password);
						}
					});
				}
				if (store == null)
					store = session.getStore("imaps");
				if (!store.isConnected ())
					store.connect("imap.gmail.com", username, password);
				root = recursiveList (store.getDefaultFolder ());	
			} catch (AuthenticationFailedException e) {
				password_prompt = "Wrong password, try again";
				session = null;
				continue;
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
			break;
		}
		DefaultTreeModel dtm = new DefaultTreeModel (root);
		MainUI window2 = new MainUI (this);
		window2.setTreeModel (dtm);
		window2.setVisible (true);

	}

	public static void main(String[] args) throws IOException, InterruptedException, MessagingException {
		HippoCrypt main = new HippoCrypt ();
		main.doStuff ();
	}
}