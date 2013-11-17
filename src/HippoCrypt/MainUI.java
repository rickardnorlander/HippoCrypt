package HippoCrypt;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.LineBorder;

import util.*;

import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
	private JList<EmailRef> emailList;
	private JLabel dateLabel;
	private JLabel subjectLabelLabel;
	private JTextPane bodyIn;
	private JLabel fromLabelLabel;
	private JLabel subjectLabelIn;
	private JLabel fromLabel;
	private JPanel welcomePanel;
	private long slowId = 0;
	
	private Set<Long> slowRunning = new HashSet<>();
	private JProgressBar progressBar;

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
		mailFolderTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Root") {
				{
					add(new DefaultMutableTreeNode("Loading folders..."));
				}
			}
		));
		mailFolderTree.setRootVisible(false);
		mailFolderTree.setShowsRootHandles(true);
		scrollPane.setViewportView(mailFolderTree);
		mailFolderTree.addMouseListener (new MouseAdapter () {
			@Override
			public void mouseClicked (MouseEvent arg0) {
				final TreePath tp = mailFolderTree.getPathForLocation (arg0.getX (), arg0.getY ());
				final long id = startSlowThing ();
				if (tp != null) {
					new SwingWorker<java.util.List<EmailRef>, Object> () {
						@Override
						protected List<EmailRef> doInBackground () throws Exception {
							return MainUI.this.hc.loadSomeHeaders (pathToString(tp));
						}
						@Override
						protected void done () {
							try {
								showEmailList(get());
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							} finally {
								finishedSlowThing (id);
							}
						}
					}.execute ();
				}
			}
		});
		mailFolderTree.putClientProperty("html.disable", Boolean.TRUE);
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
		cardPanel.setBounds(156, 55, 477, 343);
		contentPane.add(cardPanel);
		cardPanel.setLayout(new CardLayout(0, 0));
		
		welcomePanel = new JPanel();
		cardPanel.add(welcomePanel, "name_147962662971102");
		welcomePanel.setLayout(null);
		
		JTextPane txtpnhello = new JTextPane();
		txtpnhello.setContentType("text/html");
		txtpnhello.setEditable(false);
		txtpnhello.setText("<html><font size=25>Welcome to HippoCrypt</font><br>Email with privacy</html>");
		txtpnhello.setBounds(10, 11, 457, 365);
		welcomePanel.add(txtpnhello);
		
		JPanel showMailPanel = new JPanel();
		showMailPanel.setBackground(Color.BLUE);
		cardPanel.add(showMailPanel, "showMailPanel");
		showMailPanel.setLayout(null);
		
		fromLabelLabel = new JLabel("From");
		fromLabelLabel.setBounds(10, 11, 51, 14);
		showMailPanel.add(fromLabelLabel);
		
		JButton forwardButton = new JButton("Forward");
		forwardButton.setBounds(218, 288, 89, 23);
		showMailPanel.add(forwardButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 55, 457, 222);
		showMailPanel.add(scrollPane_1);
		
		bodyIn = new JTextPane();
		scrollPane_1.setViewportView(bodyIn);
		bodyIn.setEditable(false);
		bodyIn.putClientProperty("html.disable", Boolean.TRUE);
		
		dateLabel = new JLabel("Date");
		dateLabel.setBounds(254, 11, 161, 14);
		showMailPanel.add(dateLabel);
		dateLabel.putClientProperty("html.disable", Boolean.TRUE);
		
		
		JButton replyAllButton = new JButton("Reply all");
		replyAllButton.setBounds(119, 288, 89, 23);
		showMailPanel.add(replyAllButton);
		
		JButton replyButton = new JButton("Reply");
		replyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCompose("Re: "+subjectLabelIn.getText (), Quote.quote (bodyIn.getText ()), fromLabel.getText ());
			}
		});
		replyButton.setBounds(20, 288, 89, 23);
		showMailPanel.add(replyButton);
		
		subjectLabelLabel = new JLabel("Subject");
		subjectLabelLabel.setBounds(10, 30, 51, 14);
		showMailPanel.add(subjectLabelLabel);
		
		fromLabel = new JLabel("");
		fromLabel.setBounds(63, 11, 170, 14);
		showMailPanel.add(fromLabel);
		fromLabel.putClientProperty("html.disable", Boolean.TRUE);
		
		subjectLabelIn = new JLabel("");
		subjectLabelIn.setBounds(63, 30, 170, 14);
		showMailPanel.add(subjectLabelIn);
		subjectLabelIn.putClientProperty("html.disable", Boolean.TRUE);
		
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
		scrollPane_2.setBounds(10, 73, 443, 225);
		composeMailPanel.add(scrollPane_2);
		
		bodyOut = new JEditorPane();
		scrollPane_2.setViewportView(bodyOut);
		
		JButton submitButton = new JButton("Submit");
		submitButton.setBounds(364, 309, 89, 23);
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runInThreadWithProgressBar (new Runnable () {
					@Override public void run () {
						hc.sendMail (toField.getText (), subjectOutField.getText (), pgp, bodyOut.getText ());
					}
				});
			}
		});
		composeMailPanel.add(submitButton);
		
		JPanel mailListPanel = new JPanel();
		mailListPanel.setBackground(Color.GREEN);
		cardPanel.add(mailListPanel, "mailListPanel");
		mailListPanel.setLayout(null);
		
		emailList = new JList<>();
		emailList.setBounds(10, 11, 457, 321);
		emailList.setBorder(new LineBorder(new Color(0, 0, 0)));
		mailListPanel.add(emailList);
		emailList.putClientProperty("html.disable", Boolean.TRUE);
		util.Swing.addActionToList (emailList, new AbstractAction () {
			@Override
			public void actionPerformed (ActionEvent e) {
				JList<EmailRef> list = (JList<EmailRef>) e.getSource ();
				final EmailRef val = (EmailRef) list.getSelectedValue ();
				final long id = startSlowThing ();
				new SwingWorker<Email, Object>() {
					@Override
					protected Email doInBackground () throws Exception {
						return hc.loadAnEmail (val.folder, val.n);
					}
					@Override
					public void done () {
						try {
							showEmail(get());
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						} finally {
							finishedSlowThing (id);
						}
					}
					
				}.execute ();
			}
		});
		
		progressBar = new JProgressBar();
		progressBar.setBounds(487, 11, 146, 23);
		contentPane.add(progressBar);
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
	
	public void runInThreadWithProgressBar (final Runnable r) {
		new Thread () {
			@Override public void run () {
				Long id = null;
				try {
					id = startSlowThing ();
					r.run ();
				} finally {
					if (id != null)
						finishedSlowThing (id);
				}
			}
		}.start ();
	}
	
	public void finishedSlowThing(final long id) {
		try {
			Swing.runOnEDTNow (new Runnable() {
				@Override
				public void run () {
					if (!slowRunning.remove (id)) {
						throw new RuntimeException ();
					}
					if (slowRunning.isEmpty ())
						progressBar.setIndeterminate (false);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public long startSlowThing () {
		final Wrapper<Long> res = new Wrapper<>();
		try {
			Swing.runOnEDTNow (new Runnable() {
				@Override
				public void run () {
					res.t = slowId++;
					slowRunning.add (res.t);
					progressBar.setIndeterminate (true);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		return res.t;
	}
	
	public void showEmailList (java.util.List<EmailRef> ls) {
		DefaultListModel<EmailRef> lm = new DefaultListModel<> ();
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
