package se.lnu.trafficmonitoring.services;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.lnu.trafficmonitoring.gui.MonitorPanel;
import se.lnu.trafficmonitoring.gui.RoadViewPanel;

public class OrganizationServlet extends HttpServlet {

	private static final long serialVersionUID = 2405368394059966937L;
	private RoadViewPanel panel = MonitorPanel.organization;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/plain");
		response.getWriter().write("Organization Service!");
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		ServiceUtil util = new ServiceUtil(panel);
		util.updateGUI(request);
		
	}

}
