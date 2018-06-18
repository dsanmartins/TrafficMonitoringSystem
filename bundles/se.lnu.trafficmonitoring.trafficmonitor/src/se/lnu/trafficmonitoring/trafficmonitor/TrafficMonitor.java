package se.lnu.trafficmonitoring.trafficmonitor;

public interface TrafficMonitor {
	/** Update traffic conditions via the camera */
	void updateLocalTraffic();

	/** Determine whether there is congestion or not. */
	boolean isCongestion();
}
