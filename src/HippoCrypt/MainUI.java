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
	private JList<Email> emailList;
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
		setBounds (100, 100, 857, 617);
		contentPane = new JPanel ();
		contentPane.setBackground(Color.GRAY);
		contentPane.setBorder (new EmptyBorder (5, 5, 5, 5));
		setContentPane (contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JPanel topPanel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, topPanel, -5, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, topPanel, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, topPanel, 29, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, topPanel, 0, SpringLayout.EAST, contentPane);
		contentPane.add(topPanel);
		SpringLayout sl_topPanel = new SpringLayout();
		topPanel.setLayout(sl_topPanel);
		
		JButton button = new JButton("Compose");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCompose("", "", "");
			}
		});
		sl_topPanel.putConstraint(SpringLayout.NORTH, button, 5, SpringLayout.NORTH, topPanel);
		sl_topPanel.putConstraint(SpringLayout.WEST, button, 0, SpringLayout.WEST, topPanel);
		sl_topPanel.putConstraint(SpringLayout.EAST, button, 89, SpringLayout.WEST, topPanel);
		topPanel.add(button);
		
		progressBar = new JProgressBar();
		sl_topPanel.putConstraint(SpringLayout.NORTH, progressBar, 5, SpringLayout.NORTH, topPanel);
		sl_topPanel.putConstraint(SpringLayout.SOUTH, progressBar, 28, SpringLayout.NORTH, topPanel);
		sl_topPanel.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, topPanel);
		topPanel.add(progressBar);
		
		JSplitPane splitPane = new JSplitPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.SOUTH, topPanel);
		sl_contentPane.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, topPanel);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, contentPane);
		splitPane.setDividerLocation (160);
		splitPane.setDividerSize (3);
		contentPane.add(splitPane);
		
		cardPanel = new JPanel();
		splitPane.setRightComponent(cardPanel);
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
		showMailPanel.setBackground(Color.GRAY);
		cardPanel.add(showMailPanel, "showMailPanel");
		SpringLayout sl_showMailPanel = new SpringLayout();
		showMailPanel.setLayout(sl_showMailPanel);
		
		fromLabelLabel = new JLabel("From");
		sl_showMailPanel.putConstraint(SpringLayout.NORTH, fromLabelLabel, 11, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.WEST, fromLabelLabel, 10, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, fromLabelLabel, 61, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(fromLabelLabel);
		
		JButton forwardButton = new JButton("Forward");
		sl_showMailPanel.putConstraint(SpringLayout.WEST, forwardButton, 218, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.SOUTH, forwardButton, -10, SpringLayout.SOUTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, forwardButton, 307, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(forwardButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		sl_showMailPanel.putConstraint(SpringLayout.NORTH, scrollPane_1, 55, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.WEST, scrollPane_1, 10, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.SOUTH, scrollPane_1, -50, SpringLayout.SOUTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, scrollPane_1, -10, SpringLayout.EAST, showMailPanel);
		showMailPanel.add(scrollPane_1);
		
		bodyIn = new JTextPane();
		scrollPane_1.setViewportView(bodyIn);
		bodyIn.setEditable(false);
		bodyIn.putClientProperty("html.disable", Boolean.TRUE);
		
		dateLabel = new JLabel("Date");
		sl_showMailPanel.putConstraint(SpringLayout.NORTH, dateLabel, 11, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.WEST, dateLabel, 254, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, dateLabel, 415, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(dateLabel);
		dateLabel.putClientProperty("html.disable", Boolean.TRUE);
		
		
		JButton replyAllButton = new JButton("Reply all");
		sl_showMailPanel.putConstraint(SpringLayout.WEST, replyAllButton, 119, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.SOUTH, replyAllButton, -10, SpringLayout.SOUTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, replyAllButton, 208, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(replyAllButton);
		
		JButton replyButton = new JButton("Reply");
		sl_showMailPanel.putConstraint(SpringLayout.WEST, replyButton, 20, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.SOUTH, replyButton, -10, SpringLayout.SOUTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, replyButton, 109, SpringLayout.WEST, showMailPanel);
		replyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCompose("Re: "+subjectLabelIn.getText (), Quote.quote (bodyIn.getText ()), fromLabel.getText ());
			}
		});
		showMailPanel.add(replyButton);
		
		subjectLabelLabel = new JLabel("Subject");
		sl_showMailPanel.putConstraint(SpringLayout.NORTH, subjectLabelLabel, 30, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.WEST, subjectLabelLabel, 10, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, subjectLabelLabel, 61, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(subjectLabelLabel);
		
		fromLabel = new JLabel("");
		sl_showMailPanel.putConstraint(SpringLayout.NORTH, fromLabel, 11, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.WEST, fromLabel, 63, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.SOUTH, fromLabel, 25, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, fromLabel, 233, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(fromLabel);
		fromLabel.putClientProperty("html.disable", Boolean.TRUE);
		
		subjectLabelIn = new JLabel("");
		sl_showMailPanel.putConstraint(SpringLayout.NORTH, subjectLabelIn, 30, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.WEST, subjectLabelIn, 63, SpringLayout.WEST, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.SOUTH, subjectLabelIn, 44, SpringLayout.NORTH, showMailPanel);
		sl_showMailPanel.putConstraint(SpringLayout.EAST, subjectLabelIn, 233, SpringLayout.WEST, showMailPanel);
		showMailPanel.add(subjectLabelIn);
		subjectLabelIn.putClientProperty("html.disable", Boolean.TRUE);
		
		JPanel composeMailPanel = new JPanel();
		composeMailPanel.setBackground(Color.ORANGE);
		cardPanel.add(composeMailPanel, "composeMailPanel");
		SpringLayout sl_composeMailPanel = new SpringLayout();
		composeMailPanel.setLayout(sl_composeMailPanel);
		
		JLabel toLabel = new JLabel("To");
		sl_composeMailPanel.putConstraint(SpringLayout.NORTH, toLabel, 14, SpringLayout.NORTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, toLabel, 10, SpringLayout.WEST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, toLabel, 56, SpringLayout.WEST, composeMailPanel);
		composeMailPanel.add(toLabel);
		
		toField = new JTextField();
		sl_composeMailPanel.putConstraint(SpringLayout.NORTH, toField, 11, SpringLayout.NORTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, toField, 64, SpringLayout.WEST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, toField, 311, SpringLayout.WEST, composeMailPanel);
		toField.setColumns(10);
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
		sl_composeMailPanel.putConstraint(SpringLayout.NORTH, encryptionOutStatus, 14, SpringLayout.NORTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, encryptionOutStatus, 373, SpringLayout.WEST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, encryptionOutStatus, 474, SpringLayout.WEST, composeMailPanel);
		composeMailPanel.add(encryptionOutStatus);
		
		JLabel subjectLabel = new JLabel("Subject");
		sl_composeMailPanel.putConstraint(SpringLayout.NORTH, subjectLabel, 39, SpringLayout.NORTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, subjectLabel, 10, SpringLayout.WEST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, subjectLabel, 56, SpringLayout.WEST, composeMailPanel);
		composeMailPanel.add(subjectLabel);
		
		subjectOutField = new JTextField();
		sl_composeMailPanel.putConstraint(SpringLayout.NORTH, subjectOutField, 36, SpringLayout.NORTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, subjectOutField, 64, SpringLayout.WEST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, subjectOutField, 311, SpringLayout.WEST, composeMailPanel);
		subjectOutField.setColumns(10);
		composeMailPanel.add(subjectOutField);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		sl_composeMailPanel.putConstraint(SpringLayout.NORTH, scrollPane_2, 73, SpringLayout.NORTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, scrollPane_2, 10, SpringLayout.WEST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.SOUTH, scrollPane_2, -50, SpringLayout.SOUTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, scrollPane_2, -10, SpringLayout.EAST, composeMailPanel);
		composeMailPanel.add(scrollPane_2);
		
		bodyOut = new JEditorPane();
		scrollPane_2.setViewportView(bodyOut);
		
		JButton submitButton = new JButton("Submit");
		sl_composeMailPanel.putConstraint(SpringLayout.WEST, submitButton, -89, SpringLayout.EAST, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.SOUTH, submitButton, -10, SpringLayout.SOUTH, composeMailPanel);
		sl_composeMailPanel.putConstraint(SpringLayout.EAST, submitButton, -10, SpringLayout.EAST, composeMailPanel);
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
		SpringLayout sl_mailListPanel = new SpringLayout();
		mailListPanel.setLayout(sl_mailListPanel);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		sl_mailListPanel.putConstraint(SpringLayout.NORTH, scrollPane_3, 10, SpringLayout.NORTH, mailListPanel);
		sl_mailListPanel.putConstraint(SpringLayout.WEST, scrollPane_3, 10, SpringLayout.WEST, mailListPanel);
		sl_mailListPanel.putConstraint(SpringLayout.SOUTH, scrollPane_3, -10, SpringLayout.SOUTH, mailListPanel);
		sl_mailListPanel.putConstraint(SpringLayout.EAST, scrollPane_3, -10, SpringLayout.EAST, mailListPanel);
		mailListPanel.add(scrollPane_3);
		
		emailList = new JList<>();
		scrollPane_3.setViewportView(emailList);
		emailList.setBorder(new LineBorder(new Color(0, 0, 0)));
		emailList.putClientProperty("html.disable", Boolean.TRUE);
		util.Swing.addActionToList (emailList, new AbstractAction () {
			@Override
			public void actionPerformed (ActionEvent e) {
				JList<Email> list = (JList<Email>) e.getSource ();
				final Email val = (Email) list.getSelectedValue ();
				final long id = startSlowThing ();
				new SwingWorker<Email, Object>() {
					@Override
					protected Email doInBackground () throws Exception {
						return hc.loadAnEmail (val.folder, val.uid);
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
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		mailFolderTree = new JTree();
		mailFolderTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("JTree") {
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
				if (tp != null) {
					final long id = startSlowThing ();
					new SwingWorker<java.util.List<Email>, Object> () {
						@Override
						protected List<Email> doInBackground () throws Exception {
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
	
	public void showEmailList (java.util.List<Email> ls) {
		DefaultListModel<Email> lm = new DefaultListModel<> ();
		for (Email s : ls) {
			lm.addElement (s);
		}
		emailList.setModel (lm);
		((CardLayout)cardPanel.getLayout()).show(cardPanel, "mailListPanel");
		
	}
	
	public void setTreeModel (TreeModel tm) {
		mailFolderTree.setModel (tm);
	}
}
