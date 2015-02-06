package util;

import java.io.*;

import org.apache.commons.lang3.StringEscapeUtils;

public class Quote {
	
	/**
	 * Try to get the text out of a html. Want to preserve new lines, paragraphs.
	 */
	public static String unhtmlify (String _html) {
		String html = _html.replaceAll("(?s)<.*?>","\\<$0");
		html = html.replaceAll("<<br/?>", "<br>");
		html = html.replaceAll("<<p>", "<p>");
		html = html.replaceAll("(?s)<<.*?>","");
		html = html.replaceAll("\\s+", " ");
		html = html.replaceAll("(?s)<br>","\n");
		html = html.replaceAll("(?s)\\s*\n\\s*","\n");
		html = html.replaceAll("(?s)<p>","\n\n");
		html = html.replaceAll("(?s)\\s*\n\n\\s*","\n\n");
		html = StringEscapeUtils.unescapeHtml4(html);
		
		return html.trim();
	}

	/**
	 * Quote an email with ">" arrows 
	 */
	public static String quote (String in) {
		try {
			String cleaned = unhtmlify(in);
			BufferedReader bufReader = new BufferedReader (new StringReader (cleaned));
			StringBuilder sb = new StringBuilder ();
			String line = null;
			while ((line = bufReader.readLine ()) != null) {
				sb.append ("> ");
				sb.append (line);
				sb.append ("\n");
			}
			return sb.toString ();
		} catch (IOException e) {
		}
		throw new RuntimeException ("This cannot happen");
	}
}
