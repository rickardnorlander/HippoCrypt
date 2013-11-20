package HippoCrypt;

import java.io.*;
import java.sql.*;
import java.util.*;

import util.NullHelper;

/**
 * A cache for email and email folders. Backed by SQLite
 */
public class Cache {
	private static Cache cache;
	private final File folder;
	private final Connection connection;

	private Statement stmt;
	final private PreparedStatement storePS;
	final private PreparedStatement updateBodyPS;
	final private PreparedStatement emailForUidPS;
	final private PreparedStatement largestUidPS;
	final private PreparedStatement emailsForFolderPS;
	final private PreparedStatement foldersPS;
	final private PreparedStatement forgetAllFoldersPS;
	final private PreparedStatement setFoldersPS;
	
	private Cache () throws ClassNotFoundException, SQLException, IOException {
		folder = new File (System.getProperty("user.home"), "HippoCrypt");
		folder.mkdirs ();
		Class.forName ("org.sqlite.JDBC");
		connection = DriverManager.getConnection ("jdbc:sqlite:"+new File(folder, "emailcache.db").getCanonicalPath ());
		connection.setAutoCommit (false);
		stmt = connection.createStatement ();
		
		// body can be null if we haven't fetched it yet
		stmt.executeUpdate ("CREATE TABLE IF NOT EXISTS Emails (uid bigint, subject text, sentDate integer, body text, folder text, CONSTRAINT pk_uidFolder PRIMARY KEY (uid,folder))");
		stmt.executeUpdate ("CREATE TABLE IF NOT EXISTS Folders (name text PRIMARY KEY, parent text)");
		stmt.executeUpdate ("CREATE INDEX IF NOT EXISTS uidOnEmails ON Emails (uid)");
		stmt.executeUpdate ("CREATE INDEX IF NOT EXISTS folderOnEmails ON Emails (folder)");
		stmt.executeUpdate ("CREATE INDEX IF NOT EXISTS parentOnFolders ON Folders (parent)");
		connection.commit ();
		
		storePS = connection.prepareStatement ("INSERT INTO Emails (uid, subject, sentDate, body, folder) VALUES (?, ?, ?, ?, ?)");
		updateBodyPS = connection.prepareStatement ("UPDATE Emails SET body = ? WHERE uid = ?");
		emailForUidPS = connection.prepareStatement("SELECT * from Emails where uid = ?");
		emailsForFolderPS = connection.prepareStatement("SELECT * from Emails where folder = ? ORDER BY uid");
		largestUidPS = connection.prepareStatement("SELECT max(uid) from Emails where folder = ?");
		foldersPS = connection.prepareStatement ("SELECT * FROM Folders ORDER BY name");
		forgetAllFoldersPS = connection.prepareStatement ("DELETE FROM Folders");
		setFoldersPS = connection.prepareStatement ("INSERT INTO Folders (name, parent) VALUES (?, ?)");
	}
	
	public synchronized void setBody (long uid, String text) throws SQLException {
		updateBodyPS.setString (1, text);
		updateBodyPS.setLong (2, uid);
		updateBodyPS.execute ();
		connection.commit ();
	}
	
	public synchronized List<Email> getEmailsForFolder (String folder) throws SQLException {
		emailsForFolderPS.setString (1, folder);
		ResultSet rs = emailsForFolderPS.executeQuery ();
		List<Email> ret = new ArrayList<>();
		while (rs.next ()) {
			ret.add (getEmailFromRow (rs));
		}
		return ret;
	}
	
	public synchronized void setFolders (List<FolderDesc> folders) throws SQLException {
		forgetAllFoldersPS.execute ();
		for (FolderDesc fs : folders) {
			setFoldersPS.setString (1, fs.fullName);
			setFoldersPS.setString (2, fs.fullNameParent);
			setFoldersPS.addBatch ();
		}
		setFoldersPS.executeBatch ();
		connection.commit ();
	}
	
	public synchronized List<FolderDesc> getFolders () throws SQLException {
		ResultSet rs = foldersPS.executeQuery ();
		List<FolderDesc> ret = new ArrayList<>();
		while (rs.next ()) {
			FolderDesc fd = new FolderDesc ();
			fd.fullName = rs.getString (1);
			fd.fullNameParent = rs.getString (2);
			ret.add(fd);
		}
		return ret;
	}
	
	public synchronized long getLargestUid (String folder) throws SQLException {
		largestUidPS.setString(1, folder);
		ResultSet rs = largestUidPS.executeQuery ();
		rs.next();
		long res = rs.getLong (1); // For empty table rs[1] == null so getLong returns zero, which is what we want
		return res;
	}
	
	private static Email getEmailFromRow (ResultSet rs) throws SQLException {
		Email ret = new Email ();
		ret.uid = rs.getLong (1);
		ret.subject = rs.getString (2);
		long longDate = rs.getLong (3);
		ret.sentDate = rs.wasNull () ? null : new java.util.Date (longDate);
		ret.body = rs.getString (4);
		ret.folder = rs.getString (5);
		return ret;
	}
	
	public synchronized Email getEmailForUid (long uid) throws SQLException {
		emailForUidPS.setLong (1, uid);
		ResultSet rs = emailForUidPS.executeQuery ();
		if (!rs.next ()) return null;
		return getEmailFromRow (rs);
	}
	
	public synchronized void store (List<Email> emails) throws SQLException {
		for (Email email : emails) {
			storePS.setLong (1, email.uid);
			storePS.setString (2, email.subject);
			if (email.sentDate != null) {
				storePS.setLong (3, email.sentDate.getTime ());
			} else {
				storePS.setNull (3, Types.BIGINT);
			}
			storePS.setString (4, email.body);
			storePS.setString (5, email.folder);
			storePS.addBatch ();
		}
		storePS.executeBatch ();
		connection.commit ();
	}
	
	public static Cache getInstance () throws ClassNotFoundException, SQLException, IOException {
		synchronized (Cache.class) {
    		if( cache == null)
    			cache = new Cache ();
		}
		return cache;
	}
//	
//	public static void main (String args []) throws ClassNotFoundException, SQLException, IOException {
//		Cache cache = Cache.getInstance ();
//		Email email = new Email ();
//		System.out.println (cache.getLargestUid());
//		System.out.println (cache.getEmailForUid(6));
//		email.body = "a";
//		email.folder ="b";
//		email.from = "c";
//		email.sentDate = new java.util.Date();
//		email.subject = "d";
//		email.uid = 60000000000000003L;
//		cache.store (Collections.singletonList (email));
//		cache.setBody (6, "haha");
//		System.out.println (cache.getEmailForUid(email.uid));
//		System.out.println (cache.getLargestUid());
//		
//		
//		for (String s : cache.getFolders ()) {
//			System.out.println("folder: "+s);
//		}
//		List<String> f = new ArrayList<> ();
//		f.add ("b");
//		f.add ("a");
//		for (String s : cache.getFolders ()) {
//			System.out.println("folder: "+s);
//		}
//		cache.setFolders (f);
//		for (String s : cache.getFolders ()) {
//			System.out.println("folder: "+s);
//		}
//	}
}
