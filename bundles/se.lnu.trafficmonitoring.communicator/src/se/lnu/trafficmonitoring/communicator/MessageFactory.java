package se.lnu.trafficmonitoring.communicator;

public interface MessageFactory {
	public Message createMessage(int receiverId, int bundleId, String type);
}
