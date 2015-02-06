package HippoCrypt;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import javax.swing.JOptionPane;
import javax.swing.tree.*;

import org.apache.commons.io.*;
import org.apache.commons.io.output.TeeOutputStream;

import com.sun.mail.imap.IMAPFolder;

import HippoCrypt.Email.Attachment;
import HippoCrypt.GPG.GPGData;
import HippoCrypt.GPG.GPGException;
import HippoCrypt.MailProvider.RetInfo;
import util.*;

public class HippoCrypt {

	private static final String PASSWORD = "password";
	private final static String PREF_EMAIL = "email";
	private final static String PREF_GPG_FP = "gpg-fp";
	private final static String PREF_GPG_PASS = "gpg-pass";


	ConfStore prefs;
	private Session session = null;
	private Store store = null;
	private String storeGuard = "guard"; // For synchronization
	
	private GPGData gpgdata;
	private String email;
	private String username;
	private Properties props = null;


	private GPGData getGPGData (String email) throws GPGException, IOException {
		GPGData ret = new GPGData ();
		ret.fingerprint = prefs.get (PREF_GPG_FP);
		if(ret.fingerprint == null) {
			GenPGPDialog d = new GenPGPDialog ();
			d.setVisible (true);
			ret = GPG.genGPG ("A", "a@a.com", "password");
			d.dispose ();
			
			prefs.setAutoCommit (false);
			prefs.put (PREF_GPG_FP, ret.fingerprint);
			prefs.put (PREF_GPG_PASS, ret.pass);
			prefs.put ("key-"+email, ret.fingerprint);
			prefs.commit ();
			prefs.setAutoCommit (true);
			
			return ret;
		}
		ret.pass = prefs.get (PREF_GPG_FP);
		if (ret.pass == null) {
			ret.pass = PasswordDialog.askPass();
		}
		return ret;
	}

	public String getMyEmailAddress() {
		return email;
	}

	public List<String> getAllFingerprintsOrFail (InternetAddress [] ads) {
		List<String> ret = new ArrayList<>();
		for(InternetAddress ia : ads) {
			String fp = prefs.get ("key-"+ia.getAddress());
			if (fp == null)
				return null;
			ret.add(fp);
		}
		return ret;
	}
	
	
	public void sendMail (String to, String subject, String body, List<File> attachments) throws MessagingException, GPGException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email));
		InternetAddress[] sendto = InternetAddress.parse(to);
		message.setRecipients(Message.RecipientType.TO, sendto);
		
		List<String> fps = getAllFingerprintsOrFail(sendto);
		
		if (fps != null) {
			message.setSubject("HippoCrypt encrypted email");
			fps.add(gpgdata.fingerprint);
			String encSubject = GPG.encrypt (fps, subject);

			// Strip the pgp headers
			int ind1 = encSubject.indexOf ('\n');
			if (ind1 == -1) {
				throw new RuntimeException ();
			}
			int ind2 = encSubject.indexOf ('\n', ind1+1);
			if (ind2 == -1) {
				throw new RuntimeException ();
			}
			int lastInd = encSubject.lastIndexOf ('\n');
			if (lastInd == -1) {
				throw new RuntimeException ();
			}
			int lastInd2 = encSubject.lastIndexOf ('\n', lastInd-1);
			if (lastInd2 == -1) {
				throw new RuntimeException ();
			}
			encSubject = encSubject.substring (ind2+2, lastInd2);
			encSubject = encSubject.replaceAll ("\n", "");

			message.setHeader ("PGP-Subject", encSubject);

			// Construct encrypted part

			MimeBodyPart pgppart = new MimeBodyPart();
			pgppart.setContent(GPG.encrypt (fps, body), "text/pgp; charset=utf-8");

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

			MimeBodyPart ourKeyAttachment = new MimeBodyPart ();
			ourKeyAttachment.setContent(GPG.getArmoredPublicKey (gpgdata.fingerprint), "application/octet-stream");
			ourKeyAttachment.setFileName ("publickey.asc");
			ourKeyAttachment.setDisposition (Part.ATTACHMENT);

			// Create a multipart/mixed containing message and attachment

			Multipart outer = new MimeMultipart("mixed");
			outer.addBodyPart (alternativeBody);
			outer.addBodyPart (ourKeyAttachment);

			if (!attachments.isEmpty ()) {
				MimeBodyPart fileAttachment = new MimeBodyPart ();
				StringBuilder sb = new StringBuilder ();
				for (File f : attachments) {
					sb.append(f.getName ());
					sb.append("\n");
				}
				fileAttachment.setContent(GPG.encrypt (fps, sb.toString ()), "text/pgp");
				fileAttachment.setDisposition (Part.ATTACHMENT);
				fileAttachment.setFileName ("filelist.gpg");
				outer.addBodyPart (fileAttachment);
			}

			int i = 1;
			for (File f : attachments) {
				MimeBodyPart fileAttachment = new MimeBodyPart ();
				fileAttachment.setContent(GPG.encryptFile(fps, f), "text/pgp");
				fileAttachment.setDisposition (Part.ATTACHMENT);
				fileAttachment.setFileName ("attachment"+i+".gpg");
				outer.addBodyPart (fileAttachment);
				++i;
			}

			message.setContent(outer);
		} else {
			message.setSubject(subject);
			Multipart multiPart = new MimeMultipart("mixed");

			MimeBodyPart textPart = new MimeBodyPart ();
			textPart.setText(body, "utf-8");

			MimeBodyPart attachment = new MimeBodyPart ();
			attachment.setContent(GPG.getArmoredPublicKey (gpgdata.fingerprint), "application/octet-stream");
			attachment.setDisposition (Part.ATTACHMENT);
			attachment.setFileName ("publickey.asc");

			multiPart.addBodyPart (textPart);
			multiPart.addBodyPart (attachment);

			for (File f : attachments) {
				MimeBodyPart fileAttachment = new MimeBodyPart ();
				DataSource source = new FileDataSource(f);
				fileAttachment.setDataHandler(new DataHandler(source));
				fileAttachment.setDisposition (Part.ATTACHMENT);
				fileAttachment.setFileName (f.getName ());
				multiPart.addBodyPart (fileAttachment);
			}

			message.setContent(multiPart);
		}

		Transport.send(message);
		System.out.println("Done");
	}

	private void recursiveList (Folder [] fs, List<FolderDesc> ret) throws MessagingException {
		for (Folder f : fs) {
			FolderDesc fd = new FolderDesc ();
			fd.fullName = f.getFullName ();
			Folder parent = f.getParent ();
			if (parent != null && !parent.getFullName ().isEmpty ())
				fd.fullNameParent = parent.getFullName ();
			ret.add (fd);
		}
	}

	private class MyMessage {
		List<String> text;
		List<Attachment> attachments;
		List<Boolean> isEncrypted;
	}

	/**
	 * Return the primary text content of the message.
	 */
	private MyMessage parseMessage(Part p) throws
	MessagingException, IOException {
		if ((p.getDisposition () == null && p.getFileName () != null) || Part.ATTACHMENT.equalsIgnoreCase (p.getDisposition ())) {
			Attachment a = new Attachment ();
			a.filename = new String(NullHelper.help (p.getFileName (), "NONAME").toString().getBytes(), "UTF-8");
			a.part = p;
			a.encrypted = p.isMimeType ("text/pgp");

			MyMessage ret = new MyMessage ();
			ret.attachments = Collections.singletonList (a);
			ret.text = Collections.emptyList();
			ret.isEncrypted = Collections.emptyList();
			return ret;
		}
		if (p.isMimeType("text/pgp")) {

			MyMessage ret = new MyMessage ();
			ret.text = Collections.singletonList (IOUtils.toString((InputStream) p.getContent(), "UTF-8"));
			ret.isEncrypted = Collections.singletonList (true);
			ret.attachments = Collections.emptyList();
			return ret;
		}
		if (p.isMimeType("text/plain")) {
			MyMessage ret = new MyMessage ();

			String text = p.getContent().toString ();
			text = "<html><body>"+text.replaceAll ("<", "&lt;").replaceAll (">", "&gt;")+"</body></html>";
			ret.text = Collections.singletonList (text);

			ret.isEncrypted = Collections.singletonList (false);
			ret.attachments = Collections.emptyList();
			return ret;
		}
		if (p.isMimeType("text/html")) {
			MyMessage ret = new MyMessage ();

			String text = p.getContent().toString ();
			ret.text = Collections.singletonList (text);

			ret.isEncrypted = Collections.singletonList (false);
			ret.attachments = Collections.emptyList();
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
	
	public void writeAttachmentToFile (Attachment att, File f) throws MessagingException, IOException, GPGException {
		if (att.encrypted) {
			File temp = File.createTempFile ("hippocrypt", "b");
			temp.deleteOnExit ();
			FileOutputStream fos = new FileOutputStream (temp);
			IOUtils.copy (att.part.getInputStream (), fos);
			fos.close ();
			
			GPG.decryptFileToFile (PASSWORD, temp, f);
		} else {
			FileOutputStream fos = new FileOutputStream (f);
			IOUtils.copy (att.part.getInputStream (), fos);
			fos.close ();
		}
	}

	private static List<InternetAddress> getRec (Address[] recipients) throws MessagingException {
		if (recipients == null)
			return Collections.emptyList();
		return Arrays.asList((InternetAddress[])recipients);
	}
	
	public Email loadAnEmail (String folder, long uid) {
		Email ret = new Email ();
		IMAPFolder f = null;
		synchronized (storeGuard) {
    		try {
    			f = (IMAPFolder)store.getFolder (folder);
    			f.open (Folder.READ_ONLY);
    
    			Message m = f.getMessageByUID (uid);
    			
    			ret.folder = folder;
    			ret.from = getFromString (m);
    			ret.sentDate = m.getSentDate ();
    			ret.subject = getSubjectFromMessage (m);
    			ret.to = getRec(m.getRecipients(RecipientType.TO));
    			ret.cc = getRec(m.getRecipients(RecipientType.CC));
    			ret.replyTo = getRec(m.getReplyTo());
    
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
    			ret.attachments = new ArrayList<> ();
    			List<String> names = null;
    			for (int i = 0; i < mm.attachments.size (); ++i) {
    				Attachment att = mm.attachments.get (i);
    				if (att.filename.equals ("publickey.asc")) {
    					System.out.println("Has public key!!");

    					String key = IOUtils.toString (att.part.getInputStream (), "UTF-8");
    					for (Address a: m.getFrom ()) {
    						if (a instanceof InternetAddress) {
    							String fromemail = ((InternetAddress) a).getAddress ();
    							maybeAddPublicKey (fromemail, key);
    						}
    					}
    				} else if (att.filename.equals ("filelist.gpg") && names == null) {
    					String data = IOUtils.toString (att.part.getInputStream (), "UTF-8");
    					if (att.encrypted) {
    						data = GPG.decrypt (data, PASSWORD);
    					}
    					names = Arrays.asList (data.split ("\n"));
    				} else {
    					ret.attachments.add (att);
    				}
    			}
    			Pattern p = Pattern.compile("attachment(\\d{0,6}).gpg");
    			for (int i = 0; i < ret.attachments.size (); ++i) {
    				Attachment att = ret.attachments.get (i);
    				Matcher matcher = p.matcher (att.filename);
    				if (matcher.matches ()) {
    					int n = Integer.parseInt (matcher.group (1)) - 1;
    					if (n >= 0 && n <names.size ())
    						att.filename = names.get (n);
    				}
    			}
    		} catch (IOException | MessagingException | GPGException e) {
    			Swing.showException ("Failed to load email", e);
    			e.printStackTrace ();
    		}
		}
		return ret;
	}

	private void maybeAddPublicKey (String fromemail, String key) throws GPGException, IOException {
		String prevFingerprint = prefs.get("key-"+fromemail);
		if (prevFingerprint != null) {
			System.out.println(fromemail+" was not updated: already have key");
			return;
		}
		String fingerprint = GPG.importKey (key);
		if (fingerprint != null)
			prefs.put ("key-"+fromemail, fingerprint);
	}

	public List<Email> getHeadersForFolder (String folderName, boolean onlyCache) throws SQLException, ClassNotFoundException, IOException, MessagingException {
		IMAPFolder f = null;
		Cache cache = Cache.getInstance ();
		synchronized(storeGuard) {
			try {

				if (!onlyCache) {
					f = (IMAPFolder)store.getFolder (folderName);
					f.open (Folder.READ_ONLY);

					int mode = f.getMode ();
					if ((mode & Folder.HOLDS_MESSAGES) == 0) {
						f.close (false);
						return Collections.emptyList();
					}

					long requestFrom = cache.getLargestUid (folderName) + 1; // uids start from one
					long requestTo = f.getUIDNext ();

					Message [] messages = f.getMessagesByUID (requestFrom, requestTo);


					FetchProfile fp = new FetchProfile();
					fp.add(FetchProfile.Item.ENVELOPE);
					fp.add ("PGP-Subject");

					f.fetch (messages, fp);


					List<Email> newEmails = new ArrayList<>();
					for (Message message : messages) {
						Email a = new Email ();
						a.sentDate = message.getSentDate ();
						a.subject = getSubjectFromMessage (message);
						a.from = getFromString (message);
						a.folder = folderName;
						a.uid = f.getUID (message);

						newEmails.add (a);
					}

					cache.store (newEmails);

					f.close (false);
				}
				return cache.getEmailsForFolder (folderName);
			} finally {
    			try {
    				if (f != null && f.isOpen ()) {
    					f.close (false);
    				}
    			} catch (MessagingException e1) {
    				e1.printStackTrace();
    			}
    		}
		}
	}

	private String getFromString (Message message) throws MessagingException {
		Address[] from = message.getFrom ();
		StringBuilder sb = new StringBuilder ();
		for (int i = 0; i < from.length; ++i) {
			if (i != 0) {
				sb.append (", ");
			}
			sb.append (from[i].toString ());
		}
		return sb.toString ();
	}

	private String getSubjectFromMessage (Message message) throws MessagingException {
		String s = message.getSubject ();
		String [] pgpSubject = message.getHeader ("PGP-Subject");
		if (pgpSubject != null && pgpSubject.length > 0) {
			try {
				s = GPG.decrypt ("-----BEGIN PGP MESSAGE-----\n\n\n"+pgpSubject[0]+"\n-----END PGP MESSAGE-----", PASSWORD);
			} catch (GPGException e) {
				e.printStackTrace ();
				// I think we want to just print a trace and go on, but idk
			}
		}
		return s;
	}

	// Assumes we have sorted on fullnameparent
	private static TreeModel getModelFromFolderDescs (List<FolderDesc> lfd) {
		HashMap<String, DefaultMutableTreeNode> lm = new HashMap <>();
		lm.put (null, new DefaultMutableTreeNode ("Root"));
		for (FolderDesc fd : lfd) {
			DefaultMutableTreeNode parent = lm.get (fd.fullNameParent);
			String [] parts = fd.fullName.split ("/");
			DefaultMutableTreeNode me = new DefaultMutableTreeNode (parts[parts.length-1]);
			parent.add (me);
			lm.put(fd.fullName, me);
		}
		return new DefaultTreeModel (lm.get (null));
	}

	public void doStuff () throws IOException, InterruptedException, MessagingException, ClassNotFoundException, SQLException {
		File folder = new File (System.getProperty("user.home"), "HippoCrypt");
		folder.mkdirs ();
		System.setOut (new PrintStream(new TeeOutputStream (System.out, new FileOutputStream (new File(folder, "stdout.txt")))));
		System.setErr (new PrintStream(new TeeOutputStream (System.err, new FileOutputStream (new File(folder, "stderr.txt")))));
		
		prefs = new ConfStore(new File(folder, "configuration.props"));

		final MainUI window2 = new MainUI (this);
		Cache cache = Cache.getInstance ();
		window2.setTreeModel (getModelFromFolderDescs (cache.getFolders ()));
		Long slowId = null;
		final List<FolderDesc> l = new ArrayList<>();
		
		synchronized (storeGuard) {
    		window2.setVisible (true);
    
    		String imapserver = null;
    		email = prefs.get(PREF_EMAIL);
    		do {
    			if (email == null) {
    				email = JOptionPane.showInputDialog("Please enter your email address. Currently only a few providers are supported");
    				prefs.put (PREF_EMAIL, email);
    			}

    			props = System.getProperties();

    			RetInfo ri = MailProvider.getProvider (email, props);
    			if (ri == null) {
    				email = null;
    				prefs.remove (PREF_EMAIL);
    			} else {
    				imapserver = ri.imapServer;
    				username = ri.username;
    				break;
    			}
    		} while(email == null);


    		try {
    			gpgdata = getGPGData (email);
    		} catch (GPGException e) {
    			Swing.showException ("Failed to get keypair. Maybe you need to install gpg?", e);
    			e.printStackTrace ();
    			System.exit (1);
    		}

    		String password_prompt = "Email account password"; 
    		while (true) {
    			try {
    				final String password = PasswordDialog.askPass(password_prompt);
    				if (password == null)
    					System.exit (0);
    				if (slowId == null)
    					slowId = window2.startSlowThing ();
    				if (session == null) {
    					session = Session.getInstance(props, 
    							new javax.mail.Authenticator() {
    						protected PasswordAuthentication getPasswordAuthentication() {
    							return new PasswordAuthentication(username, password);
    						}
    					});
    					session.setDebug (true);
    				}
    				if (store == null)
    					store = session.getStore("imaps");
    				if (!store.isConnected ()) {
    					store.connect(imapserver, username, password);
    				}
    				if (l.isEmpty ())
    					recursiveList (store.getDefaultFolder ().list("*"), l);
    			} catch (AuthenticationFailedException e) {
    				password_prompt = "Wrong password, try again";
    				session = null;
    				continue;
    			} catch (MessagingException e) {
    				throw new RuntimeException(e);
    			}
    			break;
    		}
    		cache.setFolders (l);
		}
		final TreeModel dtm = getModelFromFolderDescs (l);
		try {
			Swing.runOnEDTNow (new Runnable(){
				@Override
				public void run () {
					window2.setTreeModel (dtm);
				}});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		window2.finishedSlowThing (slowId);
	}

	public static void main(String[] args) throws IOException, InterruptedException, MessagingException, ClassNotFoundException, SQLException {
		HippoCrypt main = new HippoCrypt ();
		main.doStuff ();
	}
}