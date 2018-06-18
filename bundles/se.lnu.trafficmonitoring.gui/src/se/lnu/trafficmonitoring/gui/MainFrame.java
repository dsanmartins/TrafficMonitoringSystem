package se.lnu.trafficmonitoring.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Main JFrame for the Client GUI.
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1862859330238113740L;
	private final int PANEL_WIDTH = 800;
	private final int PANEL_HEIGHT = 640;

	public MainFrame() {
		setTitle("Traffic Monitoring System");
		JPanel mainPanel = new MainPanel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		pack();
		new RevalidatorThread().start();
		setVisible(true);
	}
	
	/** Ensures that the GUI is not corrupted for long. */
	private class RevalidatorThread extends Thread {
		public RevalidatorThread() {
			setDaemon(true);
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					sleep(2000);
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							repaint();
						}
					});
					
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
}
