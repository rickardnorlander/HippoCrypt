package util;

import java.awt.event.*;

import javax.swing.*;

public class PasswordDialog {

	public static String askPass (String prompt) {
		final JPasswordField jpf = new JPasswordField();
		JOptionPane jop = new JOptionPane(jpf,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = jop.createDialog(prompt);
		dialog.addComponentListener(new ComponentAdapter(){
			@Override
			public void componentShown(ComponentEvent e){
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run(){
						jpf.requestFocusInWindow();
					}
				});
			}
		});
		dialog.setVisible(true);
		int result = (Integer)jop.getValue();
		dialog.dispose();
		if(result == JOptionPane.OK_OPTION){
			return new String (jpf.getPassword());
		}
		return null;
	}
	
	public static String askPass () {
		return askPass("Password:");
	}
}