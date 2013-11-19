package HippoCrypt;

import java.io.*;
import java.util.regex.*;

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

	public static boolean isKill (Process p) {
		try {
			int t = p.exitValue ();
			return true;
		} catch(IllegalThreadStateException e) {
			return false;
		}
	}

	public static String getArmoredPublicKey (String fingerprint) throws IOException, InterruptedException {
		final StringBuffer sb = new StringBuffer ();
		invokeCMD ("gpg --export -a "+fingerprint, "", new MyRunnable<String>() {
			@Override
			public void run (String t) {
				sb.append (t+"\n");
			}
		}, null);
		return sb.toString ();
	}

	public static void invokeCMD (String cmd, String initSend, MyRunnable<String> onOutputLine, MyRunnable<String> onErrorLine) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(cmd);
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
	}
	

	public static GPGData genGPG (String name, String email, String password) throws IOException, InterruptedException {
		final GPGData ret = new GPGData ();
		final Pattern p = Pattern.compile ("gpg: key ([A-F0-9]{8}) marked as ultimately trusted");
		String text = "%no-protection\n%no-ask-passphrase\nKey-Type:RSA\nKey-Length:4096\nKey-Usage: auth\nSubkey-Type: RSA\nSubkey-Length: 4096\nSubkey-Usage: encrypt\nPassphrase: "+password+"\nName-Real:"+name+"\nName-Email:"+email+"\n%commit\n%echo done\n";
		String cmd = "gpg --gen-key --batch";

		invokeCMD(cmd, text,
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
		ret.pass = "password";

		if (ret.fingerprint == null) {
			throw new IOException();
		}
		String pubkey = getArmoredPublicKey (ret.fingerprint);

		invokeCMD("gpg --import --no-default-keyring --keyring HippoCryptPubRing.gpg", pubkey, new MyRunnable<String>() {
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
		return ret;
	}
	
	
	public static String importKey (String key) throws IOException, InterruptedException {

		final Pattern p1 = Pattern.compile ("key ([A-F0-9]{8}).*imported");
		final Pattern p2 = Pattern.compile ("key ([A-F0-9]{8}).*not changed");
		

		final Wrapper<String> keyidWrap = new Wrapper<String> ();
		invokeCMD("gpg --import --no-default-keyring --keyring HippoCryptPubRing.gpg", key, new MyRunnable<String>() {
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
		return keyidWrap.toString ();
	}

	public static String decrypt (String encrypted, String password) throws IOException, InterruptedException {
		final StringBuffer sb = new StringBuffer ();
		invokeCMD ("gpg --decrypt --batch --passphrase " + password, encrypted, new MyRunnable<String>() {
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
		return sb.toString ();
	}
	
	public static String encrypt (String pubkey, String cleartext) throws IOException, InterruptedException {
		// Construct encrypted part
		String cmd = "gpg -ear "+pubkey+" --always-trust --no-default-keyring --keyring HippoCryptPubRing.gpg";
		final StringBuffer sb = new StringBuffer ();
		invokeCMD(cmd, cleartext, new MyRunnable<String>() {
			@Override
			public void run (String t) {
				sb.append (t+"\n");
				System.out.println("out "+t);
			}
		}, new MyRunnable<String>() {
			@Override
			public void run (String t) {
				sb.append (t+"\n");
				System.out.println("err "+t);
			}
		});
		return sb.toString ();
	}

}
