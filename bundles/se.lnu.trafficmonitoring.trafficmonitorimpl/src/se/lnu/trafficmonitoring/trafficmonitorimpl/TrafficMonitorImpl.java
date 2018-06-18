package se.lnu.trafficmonitoring.trafficmonitorimpl;

import se.lnu.trafficmonitoring.camera.Camera;
import se.lnu.trafficmonitoring.camera.CameraUser;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.orgcontroller.OrgController;
import se.lnu.trafficmonitoring.orgcontroller.OrgControllerUser;
import se.lnu.trafficmonitoring.trafficmonitor.TrafficMonitor;

/**
 * Default TrafficMonitor implementation. Decides whether or not the degree of
 * traffic reported by a camera results in a congestion.
 */
public class TrafficMonitorImpl implements TrafficMonitor, CameraUser, OrgControllerUser {
	
	private Camera camera;
	private OrgController orgController;
	private int localTraffic;

	@Override
	public synchronized void updateLocalTraffic() {
		if (!isInitialized()) {
			throw new IllegalStateException("TrafficMonitor not yet initialized.");
		}

		localTraffic = camera.getLocalTraffic();
		
		if (isCongestion()) {
			orgController.setCongestedTraffic();
		} else {
			orgController.setFreeflowTraffic();
		}
	}

	@Override
	public synchronized boolean isCongestion() {
		return TMConstants.TRAFFIC_CONGESTION <= localTraffic;
	}
	
	public boolean isInitialized() {
		return camera != null && orgController != null;
	}

	@Override
	public synchronized void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public synchronized void setOrgController(OrgController orgController) {
		this.orgController = orgController;
	}
}
