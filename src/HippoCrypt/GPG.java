package HippoCrypt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;

import javax.swing.JOptionPane;

import util.*;

/**
 * A collection of methods for interfacing with the gpg program.
 *
 */
public abstract class GPG {
	public static class GPGData {
		public String fingerprint;
		public String pass;
	}
	public static class GPGException extends Exception {
		public GPGException () {
			super ();
		}

		public GPGException (String arg0, Throwable arg1, boolean arg2, boolean arg3) {
			super (arg0, arg1, arg2, arg3);
		}

		public GPGException (String arg0, Throwable arg1) {
			super (arg0, arg1);
		}

		public GPGException (String arg0) {
			super (arg0);
		}

		public GPGException (Throwable arg0) {
			super (arg0);
		}
	}

	public static boolean isKill (Process p) {
		try {
			int t = p.exitValue ();
			return true;
		} catch(IllegalThreadStateException e) {
			return false;
		}
	}

	public static String getArmoredPublicKey (String fingerprint) throws GPGException {
		final StringBuffer sb = new StringBuffer ();
		try {
			invokeCMD (new String []{"gpg", "--export", "-a", fingerprint}, "", new MyRunnable<String>() {
				@Override
				public void run (String t) {
					sb.append (t+"\n");
				}
			}, null);
		} catch (GPGException e) {
			throw new GPGException("Couldn't get public key", e);
		}
		String ret = sb.toString ();
		if (!ret.contains ("-----BEGIN PGP PUBLIC KEY BLOCK-----"))
			throw new GPGException ("Couldn't get public key");
		return ret;
	}

	private static void invokeCMD (String [] cmd, String initSend, MyRunnable<String> onOutputLine, MyRunnable<String> onErrorLine) throws GPGException {
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder (cmd);
			pb.environment ().put ("LC_ALL", "C");
			process = pb.start ();
			InputStream isout = process.getInputStream();
			InputStream iserr = process.getErrorStream ();
			OutputStream osin = process.getOutputStream ();

			InputStreamReader isoutr = new InputStreamReader(isout);
			InputStreamReader iserrr = new InputStreamReader(iserr);
			OutputStreamWriter osinw = new OutputStreamWriter(osin);

			BufferedReader brout = new BufferedReader(isoutr);
			BufferedReader brerr = new BufferedReader(iserrr);
			BufferedWriter bwin = new BufferedWriter(osinw);

			bwin.append (initSend);
			bwin.close ();

			while(!isKill(process) || brout.ready() || brerr.ready()) {
				if (brout.ready()) {
					if (onOutputLine != null)
						onOutputLine.run(brout.readLine ());
					continue;
				}
				if (brerr.ready()) {
					if (onErrorLine != null)
						onErrorLine.run (brerr.readLine ());
					continue;
				}
				Thread.sleep (10);
			}
			Thread.sleep (100);
			while(brout.ready() || brerr.ready()) {
				if (brout.ready()) {
					if (onOutputLine != null)
						onOutputLine.run(brout.readLine ());
					continue;
				}
				if (brerr.ready()) {
					if (onErrorLine != null)
						onErrorLine.run (brerr.readLine ());
					continue;
				}
				Thread.sleep (10);
			}
			brout.close ();
			brerr.close ();
		} catch (IOException | InterruptedException e){
			throw new GPGException ("Failed to invoke gpg", e);
		}
	}
	

	public static GPGData genGPG (String name, String email, String password) throws GPGException {
		final GPGData ret = new GPGData ();
		final Pattern p = Pattern.compile ("gpg: key ([A-F0-9]{8}) marked as ultimately trusted");
		String text = "%no-protection\n%no-ask-passphrase\nKey-Type:RSA\nKey-Length:4096\nKey-Usage: auth\nSubkey-Type: RSA\nSubkey-Length: 4096\nSubkey-Usage: encrypt\nPassphrase: "+password+"\nName-Real:"+name+"\nName-Email:"+email+"\n%commit\n%echo done\n";

		try {
			invokeCMD(new String[]{"gpg", "--gen-key", "--batch"}, text,
					new MyRunnable<String>() {
				@Override
				public void run (String line) {
					System.out.println("out: "+line);
				}
			},
			new MyRunnable<String>() {
				@Override
				public void run (String s) {
					System.out.println(s);
					Matcher m = p.matcher (s);
					if (m.matches ())
						ret.fingerprint = m.group (1);
				}
			});
		} catch (GPGException e) {
			throw new GPGException ("Couldn't generate GPG keypair", e);
		}
		ret.pass = "password";

		if (ret.fingerprint == null) {
			throw new GPGException ("Couldn't generate GPG keypair");
		}
		String pubkey = getArmoredPublicKey (ret.fingerprint);

		try {
			invokeCMD(new String []{"gpg", "--import", "--no-default-keyring", "--keyring", "HippoCryptPubRing.gpg"}, pubkey, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					System.out.println("out: "+t);
				}
			},
			new MyRunnable<String>() {
				@Override
				public void run (String t) {
					System.out.println("err: "+t);
				}
			});
		} catch (GPGException e) {
			throw new GPGException ("Couldn't import new public key");
		}
		return ret;
	}
	
	
	public static String importKey (String key) throws GPGException {

		final Pattern p1 = Pattern.compile ("key ([A-F0-9]{8}).*imported");
		final Pattern p2 = Pattern.compile ("key ([A-F0-9]{8}).*not changed");


		final Wrapper<String> keyidWrap = new Wrapper<String> ();
		try {
			invokeCMD(new String []{"gpg", "--import", "--no-default-keyring", "--keyring", "HippoCryptPubRing.gpg"}, key, new MyRunnable<String>() {
				@Override
				public void run (String t) {
				}
			},
			new MyRunnable<String>() {
				@Override
				public void run (String t) {
					if (keyidWrap.t == null) {
						Matcher m = p1.matcher (t);
						if (m.find ()) {
							keyidWrap.t = m.group (1);
						}
					}
					if (keyidWrap.t == null) {
						Matcher m = p2.matcher (t);
						if (m.find ()) {
							keyidWrap.t = m.group (1);
						}
					}
				}
			});
		} catch(GPGException e) {
			throw new GPGException ("Couldn't import key", e);
		}
		if (keyidWrap.t == null) {
			throw new GPGException ("Couldn't import key");
		}
		return keyidWrap.t;
	}

	public static String decrypt (String encrypted, String password) throws GPGException {
		final StringBuffer sb = new StringBuffer ();
		try {
			System.out.println(encrypted);
			invokeCMD (new String []{"gpg", "--decrypt", "--batch", "--passphrase-fd", "0"}, "password\n"+encrypted, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					sb.append (t+"\n");
				}
			}, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					System.out.println(t);
				}
			});
		} catch(GPGException e) {
			throw new GPGException ("Couldn't decrypt", e);
		}
		return sb.toString ();
	}

	public static void decryptFileToFile (String password, File f1, File f2) throws GPGException, IOException {
		try {
			invokeCMD (new String []{"gpg", "-o", f2.getCanonicalPath (), "--decrypt", "--batch", "--passphrase-fd", "0",f1.getCanonicalPath ()}, "password\n", new MyRunnable<String>() {
				@Override
				public void run (String t) {
				}
			}, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					System.out.println(t);
				}
			});
		} catch(GPGException e) {
			throw new GPGException ("Couldn't decrypt", e);
		}
	}

	private static List<String> buildInitial (List<String> keys) {
		List<String> ret = new ArrayList<String>();
		ret.add("gpg");
		ret.add("-ea");
		for (String key : keys) {
			ret.add("-r");
			ret.add(key);
		}
		ret.addAll(Arrays.asList(new String []{"--always-trust", "--no-default-keyring", "--keyring", "HippoCryptPubRing.gpg"}));
		return ret;
	}
	
	public static String encrypt (List<String> keys, String cleartext) throws GPGException {
		// Construct encrypted part
		List<String> cmdl = buildInitial(keys);
		String [] cmd = cmdl.toArray(new String [cmdl.size()]);
		final StringBuffer sb = new StringBuffer ();
		try {
			invokeCMD(cmd, cleartext, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					sb.append (t+"\n");
					System.out.println("out "+t);
				}
			}, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					System.out.println("err "+t);
				}
			}); 
		}
		catch(GPGException e) {
			throw new GPGException ("Couldn't encrypt", e);
		}
		return sb.toString ();
	}
	
	public static String encryptFile (List<String> keys, File f) throws GPGException {
		// Construct encrypted part
		final StringBuffer sb = new StringBuffer ();
		try {
			List<String> cmdl = buildInitial(keys);
			cmdl.add("-o-");
			cmdl.add(f.getCanonicalPath ());
			String [] cmd = cmdl.toArray(new String [cmdl.size()]);
			invokeCMD(cmd, "", new MyRunnable<String>() {
				@Override
				public void run (String t) {
					sb.append (t+"\n");
					System.out.println("out "+t);
				}
			}, new MyRunnable<String>() {
				@Override
				public void run (String t) {
					System.out.println("err "+t);
				}
			}); 
		}
		catch(GPGException | IOException e) {
			throw new GPGException ("Couldn't encrypt file", e);
		}
		return sb.toString ();
	}

}
