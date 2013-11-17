package HippoCrypt;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.LineBorder;

import util.*;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;


public class MainUI extends JFrame {

	private JPanel contentPane;
	public JTree mailFolderTree;
	HippoCrypt hc;
	private JTextField toField;
	private JTextField subjectOutField;
	private JPanel cardPanel;
	private static final String ENCRYPTED = "<html><font color=green>Encrypted</font></html>";
	private static final String NOT_ENCRYPTED = "Not encrypted";
	private String pgp = null;
	private JLabel encryptionOutStatus;
	private JEditorPane bodyOut;
	private JList emailList;
	private JLabel dateLabel;
	private JLabel subjectLabelLabel;
	private JTextPane bodyIn;
	private JLabel fromLabelLabel;
	private JLabel subjectLabelIn;
	private JLabel fromLabel;

	/**
	 * Create the frame.
	 */
	public MainUI (HippoCrypt _hc) {
		this.hc = _hc;
		setTitle("HippoCrypt");
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		setBounds (100, 100, 659, 447);
		contentPane = new JPanel ();
		contentPane.setBackground(Color.GRAY);
		contentPane.setBorder (new EmptyBorder (5, 5, 5, 5));
		setContentPane (contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 55, 135, 343);
		contentPane.add(scrollPane);
		
		mailFolderTree = new JTree();
		mailFolderTree.setRootVisible(false);
		mailFolderTree.setShowsRootHandles(true);
		scrollPane.setViewportView(mailFolderTree);
		mailFolderTree.addMouseListener (new MouseAdapter () {
			@Override
			public void mouseClicked (MouseEvent arg0) {
				TreePath tp = mailFolderTree.getPathForLocation (arg0.getX (), arg0.getY ());
				if (tp != null) {
					showEmailList(MainUI.this.hc.loadSomeHeaders (pathToString(tp)));
				}
			}
		});
		mailFolderTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		JButton composeButton = new JButton("Compose");
		composeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCompose ();
			}
		});
		composeButton.setBounds(10, 11, 89, 23);
		contentPane.add(composeButton);
		
		cardPanel = new JPanel();
		cardPanel.setBounds(156, 11, 477, 387);
		contentPane.add(cardPanel);
		cardPanel.setLayout(new CardLayout(0, 0));
		
		JPanel showMailPanel = new JPanel();
		showMailPanel.setBackground(Color.BLUE);
		cardPanel.add(showMailPanel, "showMailPanel");
		showMailPanel.setLayout(null);
		
		fromLabelLabel = new JLabel("From");
		fromLabelLabel.setBounds(10, 11, 51, 14);
		showMailPanel.add(fromLabelLabel);
		
		JButton forwardButton = new JButton("Forward");
		forwardButton.setBounds(218, 353, 89, 23);
		showMailPanel.add(forwardButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 55, 457, 287);
		showMailPanel.add(scrollPane_1);
		
		bodyIn = new JTextPane();
		bodyIn.setEditable(false);
		scrollPane_1.setViewportView(bodyIn);
		bodyIn.setText("A");
		
		dateLabel = new JLabel("Date");
		dateLabel.setBounds(254, 11, 161, 14);
		showMailPanel.add(dateLabel);
		
		JButton replyAllButton = new JButton("Reply all");
		replyAllButton.setBounds(119, 353, 89, 23);
		showMailPanel.add(replyAllButton);
		
		JButton replyButton = new JButton("Reply");
		replyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCompose("Re: "+subjectLabelIn.getText (), Quote.quote (bodyIn.getText ()), fromLabel.getText ());
			}
		});
		replyButton.setBounds(20, 353, 89, 23);
		showMailPanel.add(replyButton);
		
		subjectLabelLabel = new JLabel("Subject");
		subjectLabelLabel.setBounds(10, 30, 51, 14);
		showMailPanel.add(subjectLabelLabel);
		
		fromLabel = new JLabel("");
		fromLabel.setBounds(63, 11, 170, 14);
		showMailPanel.add(fromLabel);
		
		subjectLabelIn = new JLabel("");
		subjectLabelIn.setBounds(63, 30, 170, 14);
		showMailPanel.add(subjectLabelIn);
		
		JPanel composeMailPanel = new JPanel();
		composeMailPanel.setBackground(Color.ORANGE);
		cardPanel.add(composeMailPanel, "composeMailPanel");
		composeMailPanel.setLayout(null);
		
		JLabel toLabel = new JLabel("To");
		toLabel.setBounds(10, 14, 46, 14);
		composeMailPanel.add(toLabel);
		
		toField = new JTextField();
		toField.setColumns(10);
		toField.setBounds(64, 11, 247, 20);
		toField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				showCanEncrypt();
			}
			public void removeUpdate(DocumentEvent e) {
				showCanEncrypt();
			}
			public void insertUpdate(DocumentEvent e) {
				showCanEncrypt();
			}

			public void showCanEncrypt() {
				String str = toField.getText();

				Preferences prefs = Preferences.userNodeForPackage(HippoCrypt.class);
				pgp = prefs.get ("key-"+str, null);
				encryptionOutStatus.setText(pgp == null ? NOT_ENCRYPTED : ENCRYPTED);
			}
		});
		composeMailPanel.add(toField);
		
		encryptionOutStatus = new JLabel("Not encrypted");
		encryptionOutStatus.setBounds(373, 14, 101, 14);
		composeMailPanel.add(encryptionOutStatus);
		
		JLabel subjectLabel = new JLabel("Subject");
		subjectLabel.setBounds(10, 39, 46, 14);
		composeMailPanel.add(subjectLabel);
		
		subjectOutField = new JTextField();
		subjectOutField.setColumns(10);
		subjectOutField.setBounds(64, 36, 247, 20);
		composeMailPanel.add(subjectOutField);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 73, 443, 264);
		composeMailPanel.add(scrollPane_2);
		
		bodyOut = new JEditorPane();
		scrollPane_2.setViewportView(bodyOut);
		
		JButton submitButton = new JButton("Submit");
		submitButton.setBounds(364, 358, 89, 23);
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				hc.sendMail (toField.getText (), subjectOutField.getText (), pgp, bodyOut.getText ());
			}
		});
		composeMailPanel.add(submitButton);
		
		JPanel mailListPanel = new JPanel();
		mailListPanel.setBackground(Color.GREEN);
		cardPanel.add(mailListPanel, "mailListPanel");
		mailListPanel.setLayout(null);
		
		emailList = new JList();
		emailList.setBounds(10, 11, 457, 365);
		emailList.setBorder(new LineBorder(new Color(0, 0, 0)));
		mailListPanel.add(emailList);
		util.SwingLists.addAction (emailList, new AbstractAction () {
			@Override
			public void actionPerformed (ActionEvent e) {
				JList list = (JList) e.getSource ();
				EmailRef val = (EmailRef) list.getSelectedValue ();
				showEmail (hc.loadAnEmail (val.folder, val.n));
			}
		});
	}

	public static String pathToString (TreePath tp) {
		if (tp.getPathCount () < 1)
			throw new RuntimeException ("Invalid path");
		StringBuffer sb = new StringBuffer ();
		for (int i = 1; i < tp.getPathCount (); ++i) {
			if(i > 1)
				sb.append ("/");
			sb.append (tp.getPathComponent (i).toString ());
		}
		return sb.toString ();

	}
	
	public void showCompose (String subjectOut, String bodyOutText, String to) {
		subjectOutField.setText (subjectOut);
		bodyOut.setText (bodyOutText);
		toField.setText (to);
		((CardLayout)cardPanel.getLayout()).show(cardPanel, "composeMailPanel");
	}

	public void showCompose () {
		showCompose("", "", "");
	}
	
	public void showEmail (Email email) {
		fromLabel.setText (email.from);
		subjectLabelIn.setText (email.subject);
		dateLabel.setText ("Date: "+email.sentDate);
		bodyIn.setText (email.body);
		((CardLayout)cardPanel.getLayout()).show(cardPanel, "showMailPanel");
	}
	
	public void showEmailList (java.util.List<EmailRef> ls) {
		DefaultListModel lm = new DefaultListModel<> ();
		for (EmailRef s : ls) {
			lm.addElement (s);
		}
		emailList.setModel (lm);
		((CardLayout)cardPanel.getLayout()).show(cardPanel, "mailListPanel");
		
	}
	
	public void setTreeModel (TreeModel tm) {
		mailFolderTree.setModel (tm);
	}
}
