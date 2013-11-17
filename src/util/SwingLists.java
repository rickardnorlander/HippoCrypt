package util;

import java.awt.Rectangle;
import java.awt.event.*;

import javax.swing.*;

public abstract class SwingLists {
	private static final KeyStroke ENTER = KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0);

	public static void addAction (final JList<?> list, Action performAction) {
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
