package se.lnu.trafficmonitoring.environmentimpl;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * The main JFrame for the Environment GUI.
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 2884802660048529285L;
	
	public MainFrame(EnvironmentImpl environmentImpl) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setTitle("Environment");
		JPanel managementPanel = new ManagementPanel(environmentImpl);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(managementPanel, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}
}
