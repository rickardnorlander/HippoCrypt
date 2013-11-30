package util;

import java.awt.Rectangle;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

public abstract class Swing {
	private static final KeyStroke ENTER = KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0);

	public static void runOnEDTNow (Runnable r) throws InvocationTargetException, InterruptedException {
		if (SwingUtilities.isEventDispatchThread ())
			r.run ();
		else {
			SwingUtilities.invokeAndWait (r);
		}
	}

	public static void showException (String text, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		JOptionPane.showMessageDialog(null, text + "\n\n"+sw.toString());
	}

	public static void addActionToList (final JList<?> list, Action performAction) {
		InputMap im = list.getInputMap ();
		im.put (ENTER, ENTER);
		list.getActionMap ().put (ENTER, performAction);

		list.addMouseListener (new MouseAdapter () {
			@Override
			public void mouseClicked (MouseEvent evt) {

				if (evt.getClickCount () >= 2) {
					Rectangle r = list.getCellBounds (list.getFirstVisibleIndex (), list.getLastVisibleIndex ());
					if (r != null && r.contains (evt.getPoint ())) {
						Action action = list.getActionMap ().get (ENTER);

						if (action != null) {
							ActionEvent event = new ActionEvent (list, ActionEvent.ACTION_PERFORMED, "");
							action.actionPerformed (event);
						}
					}
				}
				
			}
		});
	}
}
