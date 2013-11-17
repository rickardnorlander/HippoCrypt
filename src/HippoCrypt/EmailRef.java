package HippoCrypt;

public class EmailRef {
	public String folder;
	public String subject;
	public int n;
	public String from;
	public String date;
	
	@Override
	public String toString () {return "<html><body>"+(subject+" "+date).replaceAll ("<", "&lt;").replaceAll (">", "&gt;")+"</body></html>";}
}
