package se.lnu.trafficmonitoring.communicator;

import java.util.concurrent.BlockingQueue;

public interface Communicator {
	/**
	 * Create a queue for a given bundle Queue implements methods to collect
	 * messages
	 */
	BlockingQueue<Message> registerService(int bundleId);

	void send(Message message);
}
