package se.lnu.trafficmonitoring.cameraimpl;

import se.lnu.trafficmonitoring.camera.Camera;
import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.CommunicatorUser;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicator.MessageFactory;
import se.lnu.trafficmonitoring.communicator.MessageFactoryUser;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.communicatorutil.MessageListener;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.data.TMMessage;

/**
 * Default implementation of Camera.
 */
public class CameraImpl implements Camera, CommunicatorUser, MessageFactoryUser {
	private GetLocalTrafficThread thread;
	
	private volatile int localTraffic;
	private volatile boolean running;
	
	private Communicator communicator;
	private MessageFactory messageFactory;
	
	public CameraImpl(MessageConsumer consumer) {
		consumer.addListener(TMMessage.SET_LOCAL_TRAFFIC, new SetLocalTrafficListener());
	}

	@Override
	public void addSubscriber(String subscriberUrl) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public int getLocalTraffic() {
		return localTraffic;
	}
	
	@Override
	public synchronized void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
		tryStartThread();
	}
	
	@Override
	public void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
		tryStartThread();
	}
	
	public synchronized void tryStartThread() {
		destroy();
		
		if (!isInitialized()) {
			return;
		}
		
		running = true;
		thread = new GetLocalTrafficThread();
		thread.start();
	}
	
	public synchronized void destroy() {
		running = false;
		
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}
	
	private synchronized void requestLocalTraffic() {
		if (!isInitialized()) {
			throw new IllegalStateException("Camera not initialized.");
		}
		
		Message message = messageFactory.createMessage(
				TMConstants.ENVIRONMENT_PID,
				TMConstants.ENVIRONMENT_BID,
				TMMessage.GET_LOCAL_TRAFFIC
		);
		
		communicator.send(message);
	}
	
	private synchronized boolean isInitialized() {
		return communicator != null && messageFactory != null;
	}

	private class SetLocalTrafficListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			localTraffic = (int) message.getPayload();
		}
	}
	
	private class GetLocalTrafficThread extends Thread {
		@Override
		public void run() {
			while (running) {
				try {
					sleep(1000);
					requestLocalTraffic();
				} catch (InterruptedException e) {
					running = false;
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
