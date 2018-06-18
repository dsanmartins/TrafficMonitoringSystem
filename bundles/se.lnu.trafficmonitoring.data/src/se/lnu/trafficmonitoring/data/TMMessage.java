package se.lnu.trafficmonitoring.data;

/**
 * Traffic Monitoring Messages.
 */
public interface TMMessage {
	
	/* Organization message types. */
	public static final String CREATE_ORGANIZATION = "CreateOrganization";
	public static final String MERGE_LEFT_DELAY = "MergeLeftDelay";
	public static final String MERGE_LEFT_REQUEST = "MergeLeftRequest";
	public static final String MERGE_LEFT_RESPONSE = "MergeLeftResponse";
	public static final String MERGE_RIGHT_REQUEST = "MergeRightRequest";
	public static final String MERGE_RIGHT_RESPONSE = "MergeRightResponse";
	public static final String SET_ORGANIZATION_MASTER = "SetOrganizationMaster";
	public static final String SPLIT_REQUEST = "SplitRequest";
	public static final String SPLIT_RESPONSE = "SplitResponse";
	
	/* Miscellaneous message types. */
	public static final String GET_LOCAL_TRAFFIC = "GetLocalTraffic";
	public static final String SET_ADDRESS_MAP = "SetAddressMap"; 
	public static final String SET_LOCAL_TRAFFIC = "SetLocalTraffic";
	public static final String SET_OWN_PID = "SetOwnPid";
	public static final String SET_PID_TO_ADDRESS = "SetPidToAddress";
	public static final String SET_POSITIONS = "SetPositions";
	public static final String SET_TRAFFIC_CONDITION = "SetTrafficCondition";
}
