package se.lnu.trafficmonitoring.services;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.lnu.trafficmonitoring.gui.MonitorPanel;
import se.lnu.trafficmonitoring.gui.RoadViewPanel;

/**
 * Handles HTTP requests to /camera. Updates the GUI when such a request has
 * been received.
 */
public class CameraServlet extends HttpServlet {

	private static final long serialVersionUID = 2073642023328633886L;
	private final RoadViewPanel panel = MonitorPanel.monitored;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/plain");
		response.getWriter().write("Camera Service!");
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		ServiceUtil util = new ServiceUtil(panel);
		util.updateGUI(request);

	}
}
