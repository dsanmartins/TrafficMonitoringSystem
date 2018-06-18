package se.lnu.trafficmonitoring.services;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.lnu.trafficmonitoring.gui.MonitorPanel;
import se.lnu.trafficmonitoring.gui.RoadViewPanel;

/**
 * Servlet for receiving requests to /environment. Updates the GUI when a
 * request has been received.
 */
public class EnvironmentServlet extends HttpServlet {

	private static final long serialVersionUID = -1245655680474550886L;
	private RoadViewPanel panel = MonitorPanel.monitorReality;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/plain");
		response.getWriter().write("Environment Service!");
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		ServiceUtil util = new ServiceUtil(panel);
		util.updateGUI(request);
	}
}
