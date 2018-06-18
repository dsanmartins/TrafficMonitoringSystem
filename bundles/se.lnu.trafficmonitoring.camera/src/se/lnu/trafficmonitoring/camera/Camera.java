package se.lnu.trafficmonitoring.camera;

public interface Camera {
	/** Subscribe for events (GUI) – URL based */
	void addSubscriber(String subscriberUrl);

	/** Observe the local traffic */
	int getLocalTraffic();
}
