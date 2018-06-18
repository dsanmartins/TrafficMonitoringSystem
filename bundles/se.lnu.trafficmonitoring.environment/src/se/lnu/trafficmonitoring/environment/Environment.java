package se.lnu.trafficmonitoring.environment;

import java.util.List;

public interface Environment {
	/** Subscribe for events (GUI) – URL based */
	void addSubscriber(String subscriberUrl);

	/** Add a camera to the environment with a given id */
	void addCamera(int cameraID);

	/**
	 * Get traffic conditions for particular camera Local traffic can be a
	 * number that represents the cameras in the viewing range or a vector of
	 * Booleans, each representing the occurrence of a car in a car slots
	 */
	int monitorTraffic(int cameraID);

	List<Integer> getAllCameraIDs();

	int getTotalNumberOfCameras();
}
