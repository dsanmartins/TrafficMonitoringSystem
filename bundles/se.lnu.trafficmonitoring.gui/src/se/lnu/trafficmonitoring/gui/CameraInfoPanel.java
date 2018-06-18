package se.lnu.trafficmonitoring.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class CameraInfoPanel extends JPanel {

	private static final long serialVersionUID = 6005892410019011130L;
	private JLabel trafLabel;
	private JPanel innerPanel;
	private JLabel camLabel;

	public CameraInfoPanel() {
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		setPreferredSize(new Dimension(70, 60));
		setLayout(new BorderLayout());
		
		innerPanel = new JPanel();
		innerPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
		camLabel = new JLabel("Cam: ?");
		trafLabel = new JLabel("Traf: ?");

		innerPanel.add(camLabel);
		innerPanel.add(trafLabel);

		add(innerPanel, BorderLayout.CENTER);
	}

	public void setCamera(int cameraId) {
		camLabel = new JLabel("Cam: " + cameraId);

		innerPanel.removeAll();
		innerPanel.add(camLabel);
		innerPanel.add(trafLabel);
	}

	public void setTraffic(int trrafic) {
		String label = Integer.toString(trrafic);
		
		if (trrafic == -1) {
			label = "?";
		} else {
			label = Integer.toString(trrafic);
		}
		
		trafLabel = new JLabel("Traf: " + label);
		innerPanel.removeAll();
		innerPanel.add(trafLabel);
		innerPanel.add(camLabel);
	}

	public void setCongestion(boolean congestion, int cameraId, int masterId) {
		if(congestion && (cameraId == masterId)){
			innerPanel.setBackground(Color.decode("#FF6600"));
		}
		else if (congestion) {
			innerPanel.setBackground(Color.decode("#FACB47"));
		} else {
			innerPanel.setBackground(Color.decode("#7DC24B"));
		}
	}
	
	public void setOrganization(OrgPosition position, boolean organization) {
		
		Color borderColor = Color.decode("#333333");
		
		if (organization && position == OrgPosition.MIDDLE) {
		
			setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, borderColor));
			
			
		} else if (organization && position == OrgPosition.FIRST) {
			Border compound;
			Border left = BorderFactory.createMatteBorder(0, 3, 0, 0, borderColor);
			Border topBottom = BorderFactory.createMatteBorder(2, 0, 2, 0, borderColor);
			compound = BorderFactory.createCompoundBorder(left,topBottom);				
			setBorder(compound);

		} else if (organization && position == OrgPosition.LAST) {
			Border compound;
			Border right = BorderFactory.createMatteBorder(0, 0, 0, 3, borderColor);
			Border topBottom = BorderFactory.createMatteBorder(2, 0, 2 , 0, borderColor);
			compound = BorderFactory.createCompoundBorder(right,topBottom);				
			setBorder(compound);
			
		}

		else {
		
			setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, borderColor));
		}

	}


}
