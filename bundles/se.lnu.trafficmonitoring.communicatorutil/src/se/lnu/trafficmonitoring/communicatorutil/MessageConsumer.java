package se.lnu.trafficmonitoring.communicatorutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.CommunicatorUser;
import se.lnu.trafficmonitoring.communicator.Message;

/**
 * Convenience class for reading messages and routing them to the correct
 * receivers.
 */
public class MessageConsumer implements CommunicatorUser {
	private int bundleId;
	private MessageConsumerThread thread;
	private Map<String, Set<MessageListener>> listeners = new HashMap<String, Set<MessageListener>>();
	private boolean running;
	
	public MessageConsumer(int bundleId) {
		this.bundleId = bundleId;
	}

	@Override
	public synchronized void setCommunicator(Communicator communicator) {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
		
		if (communicator == null) {
			return;
		}
		
		BlockingQueue<Message> queue = communicator.registerService(bundleId);
		thread = new MessageConsumerThread(queue);
		running = true;
		thread.start();
	}
	
	public synchronized void addListener(String type, MessageListener listener) {
		Set<MessageListener> listenerSet = listeners.get(type);
		
		if (listenerSet == null) {
			listenerSet = new HashSet<MessageListener>();
			listeners.put(type, listenerSet);
		}
		
		listenerSet.add(listener);
	}
	
	public synchronized void removeListener(String type, MessageListener listener) {
		Set<MessageListener> listenerSet = listeners.get(type);
		
		if (listenerSet == null) {
			return;
		}
		
		listenerSet.remove(listener);
	}
	
	public synchronized boolean containsListener(String type, MessageListener listener) {
		Set<MessageListener> listenerSet = listeners.get(type);
		
		if (listenerSet == null) {
			return false;
		}
		
		return listenerSet.contains(listener);
	}
	
	public synchronized void destroy() {
		running = false;
		
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
		
		thread = null;
	}
	
	private class MessageConsumerThread extends Thread {
		private BlockingQueue<Message> queue;
		
		public MessageConsumerThread(BlockingQueue<Message> queue) {
			this.queue = queue;
			setDaemon(true);
		}
		
		@Override
		public void run() {
			try {
				while (running) {
					Message message = queue.take();
					MessageListener[] listenerArray;
					
					synchronized (MessageConsumer.this) {
						Set<MessageListener> listenerSet = listeners.get(message.getType());
						
						if (listenerSet == null) {
							continue;
						}
						
						listenerArray = listenerSet.toArray(new MessageListener[] {});
					}
					
					for (MessageListener listener : listenerArray) {
						try {
							listener.receiveMessage(message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
			} catch (InterruptedException e) {
				running = false;
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
