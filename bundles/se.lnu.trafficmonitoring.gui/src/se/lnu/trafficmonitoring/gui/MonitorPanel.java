package se.lnu.trafficmonitoring.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * SplitPane for monitoring both the Environment and Agents.
 */
public class MonitorPanel extends JPanel {

	private static final long serialVersionUID = -1342541828908475028L;

	public static final RoadViewPanel monitorReality = new RoadViewPanel("Environment");
	public static final RoadViewPanel monitored = new RoadViewPanel("Cameras");
	public static final RoadViewPanel organization = new RoadViewPanel("Organization");

	public MonitorPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		add(monitorReality);
		add(monitored);
		add(organization);
	}

}
