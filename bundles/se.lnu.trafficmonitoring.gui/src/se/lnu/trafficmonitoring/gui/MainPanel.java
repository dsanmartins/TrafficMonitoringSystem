package se.lnu.trafficmonitoring.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * The main panel for the Client GUI.
 */
public class MainPanel extends JPanel {

	private static final long serialVersionUID = -3415682574568347490L;

	protected JPanel monitoringPanel = new MonitorPanel();

	public MainPanel() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		add(monitoringPanel, BorderLayout.CENTER);

	}

}
