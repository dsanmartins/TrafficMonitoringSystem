package se.lnu.trafficmonitoring.services;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.lnu.trafficmonitoring.gui.OrgPosition;
import se.lnu.trafficmonitoring.gui.RoadViewPanel;

public class ServiceUtil {

	private RoadViewPanel panel;

	public ServiceUtil(RoadViewPanel monitorPanel) {
		this.panel = monitorPanel;
	}

	public void updateGUI(HttpServletRequest request) {
		boolean organization = false;
		int masterId = -1;
		InputStream xml = null;
		
		try {
			xml = request.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try {

			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xml);
			doc.getDocumentElement().normalize();

			Node rootNode = doc.getDocumentElement();
			Element rootElement = (Element) rootNode;

			if (rootElement.hasAttribute("organization")) {
				organization = true;
				masterId = new Integer(rootElement.getAttribute("organization"));
				System.out.println("MASTER ID " + masterId);
			}

			NodeList nList = doc.getElementsByTagName("camera");
			System.out.println("-----------------------");

			int firstCamera = Integer.MAX_VALUE;
			int lastCamera = Integer.MIN_VALUE;

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Element currentElement = (Element) nList.item(temp);
				int current = new Integer(getAttributeValue("position", currentElement));

				if (current < firstCamera) {
					firstCamera = current;
				}

				if (lastCamera < current) {
					lastCamera = current;
				}

			}

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					int cameraId = new Integer(getAttributeValue("id", eElement));
					int position = new Integer(getAttributeValue("position", eElement));
					int traffic = new Integer(getAttributeValue("traffic", eElement));
					boolean congestion = new Boolean(getAttributeValue("congestion", eElement));
					

					OrgPosition orgPosition;

					if (position == firstCamera && position == lastCamera) {
						orgPosition = OrgPosition.NONE;
					} else if (position == firstCamera) {
						orgPosition = OrgPosition.FIRST;
					} else if (position == lastCamera) {
						orgPosition = OrgPosition.LAST;
					} else {
						orgPosition = OrgPosition.MIDDLE;
					}

					SwingUtilities.invokeLater(new UpdateUI(
							position, 
							cameraId, 
							traffic, 
							congestion,
							masterId,
							organization, 
							orgPosition)
					);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getAttributeValue(String sAttr, Element eElement) {
		if (eElement.hasAttribute(sAttr)) {
			String name = eElement.getAttribute(sAttr);
			return name.toString();
		} else {
			return "-1";
		}
	}

	private class UpdateUI implements Runnable {

		private int position;
		private int cameraId;
		private boolean organization;
		private int traffic;
		private boolean congestion;
		private int masterId;
		private OrgPosition orgPosition;

		public UpdateUI(int position, int cameraId, int traffic,boolean congestion,int masterId, boolean organization, OrgPosition orgPosition) {
			this.position = position;
			this.cameraId = cameraId;
			this.traffic = traffic;
			this.congestion = congestion;
			this.organization = organization;
			this.orgPosition = orgPosition;
			this.masterId = masterId;
		}

		@Override
		public void run() {
			panel.setCamera(position, cameraId, traffic, congestion,masterId, organization, orgPosition);
			panel.revalidate();

		}

	}

}
