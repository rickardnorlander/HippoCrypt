package HippoCrypt;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GenPGPDialog extends JFrame {

	private JPanel contentPane;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main (String[] args) {
		EventQueue.invokeLater (new Runnable () {
			public void run () {
				try {
					GenPGPDialog frame = new GenPGPDialog ();
					frame.setVisible (true);
				} catch (Exception e) {
					e.printStackTrace ();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GenPGPDialog () {
		setResizable(false);
		setTitle("Please wait - Generating keys");
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		setBounds (100, 100, 326, 93);
		contentPane = new JPanel ();
		contentPane.setBorder (new EmptyBorder (5, 5, 5, 5));
		setContentPane (contentPane);
		contentPane.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setBounds(81, 18, 146, 24);
		contentPane.add(progressBar);
	}
}
