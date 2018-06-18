package se.lnu.trafficmonitoring.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays the road and its status as a grid of JPanels.
 */
public class RoadViewPanel extends JPanel {

	private static final long serialVersionUID = -3216005794329115329L;

	private JPanel containerPanel;
	private JPanel middlePanel;
	private JPanel wraperPanel;
	private JPanel gridPanel;
	private JLabel infoLabel;
	private JLabel camerasInfo;
	private List<CameraInfoPanel> panelList = new ArrayList<CameraInfoPanel>();

	public RoadViewPanel(String title) {
		setLayout(new BorderLayout());
		infoLabel = new JLabel(title);
		infoLabel.setFont(new Font("Monospace", Font.PLAIN, 20));
		camerasInfo = new JLabel("Waiting for the cameras...");
		containerPanel = new JPanel(new BorderLayout());
		middlePanel = new JPanel();
		wraperPanel = new JPanel(new BorderLayout());

		gridPanel = new JPanel();

		containerPanel.add(infoLabel, BorderLayout.NORTH);
		wraperPanel.add(camerasInfo, BorderLayout.NORTH);
		containerPanel.add(middlePanel, BorderLayout.CENTER);
		middlePanel.add(wraperPanel);
		wraperPanel.add(gridPanel, BorderLayout.CENTER);

		add(containerPanel);
	}

	public void createCameras(int position) {
		if (panelList.size() <= position) {
			gridPanel.removeAll();
			gridPanel = new JPanel(new GridLayout(position / 10 + 1, 10, 0, 0));
			for (JPanel jPanel : panelList) {
				gridPanel.add(jPanel);
			}

			while (panelList.size() <= position) {

				CameraInfoPanel camPanel = new CameraInfoPanel();

				gridPanel.add(camPanel);
				panelList.add(camPanel);
			}

			wraperPanel.removeAll();
			wraperPanel.add(gridPanel, BorderLayout.CENTER);
		}
	}

	public void setCamera(int position, int cameraId, int localTraffic, boolean congestion, int masterId, boolean organization, OrgPosition orgPosition) {
		createCameras(position);
		
		CameraInfoPanel jPanel = panelList.get(position);

		jPanel.setTraffic(localTraffic);
		jPanel.setCamera(cameraId);
		jPanel.setCongestion(congestion, cameraId, masterId);		
		jPanel.setOrganization(orgPosition, organization);

	}

}
