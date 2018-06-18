package se.lnu.trafficmonitoring.data;

import java.io.File;
import java.net.SocketAddress;

/**
 * Traffic Monitoring Constants.
 */
public interface TMConstants {
	
	/* Bundle identifiers. */
	public static final int CAMERA_BID = "se.lnu.trafficmonitoring.camera".hashCode();
	public static final int COMMUNICATOR_BID = "se.lnu.trafficmonitoring.communicator".hashCode();
	public static final int ENVIRONMENT_BID = "se.lnu.trafficmonitoring.environment".hashCode();
	public static final int ORG_CONTROLLER_BID = "se.lnu.trafficmonitoring.orgcontroller".hashCode();
	public static final int POSITION_BID = "se.lnu.trafficmonitoring.position".hashCode();
	public static final int TRAFFIC_MONITOR_BID = "se.lnu.trafficmonitoring.trafficmonitor".hashCode();
	
	/* Process identifiers. */
	public static final int ENVIRONMENT_PID = -1;
	public static final int UNKNOWN_PID = Integer.MIN_VALUE;
	
	/* Servlet addresses. */
	public static final String CAMERA_SERVLET = "http://localhost:8080/camera";
	public static final String ENVIRONMENT_SERVLET = "http://localhost:8080/environment";
	public static final String ORGANIZATION_SERVLET = "http://localhost:8080/organization";
	
	/* Traffic modeling. */
	public static final int TRAFFIC_CONGESTION = 5;
	public static final int TRAFFIC_STOP = 10;
	public static final boolean TRAFFIC_RANDOM = true;
	
	/* Miscellaneous settings. */
	public static final int BASE_PORT = 35200;
	public static final File LAUNCHER = new File("NO DEFAULT LAUNCHER");
	public static final boolean FORCE_NOTIFY_CLIENT = false;
	public static final boolean MANUAL_TIME_STEPS = false;
	public static final boolean ORGANIZATION_DEBUG = false;
	public static final boolean INITIALIZATION_DEBUG = false;
	
	/* The default address of the process. */
	public static final String DEFAULT_HOST = null;
	public static final Integer DEFAULT_PORT = null;
	
	/* Agent addresses indexed by PID. */
	public static final SocketAddress[] AGENT_ADDRESSES = {};
}
