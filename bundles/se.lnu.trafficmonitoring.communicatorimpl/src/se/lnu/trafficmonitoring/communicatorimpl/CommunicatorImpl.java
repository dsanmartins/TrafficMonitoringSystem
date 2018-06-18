package se.lnu.trafficmonitoring.communicatorimpl;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.Message;

/**
 * Default implementation of Communicator. Does nothing but use
 * CommunicatorInput and CommunicatorOutput.
 */
public class CommunicatorImpl implements Communicator {
	private CommunicatorInput input;
	private CommunicatorOutput output;
	
	public CommunicatorImpl(int port, AddressMapper addressMapper) throws IOException {
		input = new CommunicatorInput(port);
		output = new CommunicatorOutput(addressMapper);
	}

	@Override
	public BlockingQueue<Message> registerService(int bundleId) {
		return input.registerService(bundleId);
	}

	@Override
	public void send(Message message) {
		output.send(message);
	}
	
	public void destroy() {
		input.destroy();
		output.destroy();
	}
}
