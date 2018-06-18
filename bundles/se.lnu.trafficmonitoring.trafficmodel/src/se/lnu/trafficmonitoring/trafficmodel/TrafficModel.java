package se.lnu.trafficmonitoring.trafficmodel;

public interface TrafficModel {
	void setTotalCameras(int totalCameras);

	void setTotalTimeSteps(int totalSteps);

	void setLocalTraffic(int camera, int timestep, int traffic);

	/**
	 * Returns the local traffic conditions for a particular camera at a
	 * particular timestep
	 */
	int getLocalTraffic(int camera, int timestep);
}
