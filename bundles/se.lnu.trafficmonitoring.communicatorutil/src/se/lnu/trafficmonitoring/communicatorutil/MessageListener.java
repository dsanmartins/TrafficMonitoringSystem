package se.lnu.trafficmonitoring.communicatorutil;

import se.lnu.trafficmonitoring.communicator.Message;

/**
 * Interface implemented by classes interesed in receiving messages from a
 * MessageConsumer.
 */
public interface MessageListener {
	public void receiveMessage(Message message);
}
