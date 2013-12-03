package HippoCrypt;

import java.io.File;
import java.util.*;

import javax.swing.*;

public class AttachmentHandler {
	private List<File> files;
	private JComboBox<String> cb;
	private JButton jb;
	private int n;

	public static final String defaultText = "<no files added>";

	public AttachmentHandler (JComboBox<String> cb, JButton jb) {
		this.cb = cb;
		this.jb = jb;
		this.n = 0;
		this.files = new ArrayList <> ();

		((DefaultComboBoxModel<String>)this.cb.getModel ()).addElement(defaultText);
	}

	public List<File> getFiles () { return files;}

	public void add (String s, File f) {
		if (n == 0) {
			cb.setEnabled (true);
			jb.setEnabled (true);
			cb.setModel (new DefaultComboBoxModel<String> ());
		}
		DefaultComboBoxModel<String> dcbm = (DefaultComboBoxModel<String>)cb.getModel ();
		dcbm.addElement (s);
		files.add (f);
		n++;
	}
	
	public void clear () {
		DefaultComboBoxModel<String> dcbm = (DefaultComboBoxModel<String>)cb.getModel ();
		dcbm.removeAllElements ();
		files.clear ();
		n = 0;
		dcbm.addElement (defaultText);
		cb.setEnabled (false);
		jb.setEnabled (false);
	}

	public void delete () {
		int index = cb.getSelectedIndex ();
		DefaultComboBoxModel<String> dcbm = (DefaultComboBoxModel<String>)cb.getModel ();
		dcbm.removeElementAt (index);
		files.remove(index);
		n--;
		if(n == 0) {
			dcbm.addElement (defaultText);
			cb.setEnabled (false);
			jb.setEnabled (false);
		}
	}
}
