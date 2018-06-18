package se.lnu.trafficmonitoring.communicatorimpl;

import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicator.MessageFactory;

/**
 * Default implementation of MessageFactory.
 */
public class MessageFactoryImpl implements MessageFactory {

	@Override
	public Message createMessage(int receiverId, int bundleId, String type) {
		return new MessageImpl(receiverId, bundleId, type);
	}
}
