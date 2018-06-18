package se.lnu.trafficmonitoring.communicatorimpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import se.lnu.trafficmonitoring.communicator.CommunicatorException;
import se.lnu.trafficmonitoring.communicator.Message;

/**
 * Part of the Communicator. Responsible for receiving messages from other
 * processes.
 */
public class CommunicatorInput {
	private ServerSocket socket;
	private Map<Integer, BlockingQueue<Message>> queues = new HashMap<Integer, BlockingQueue<Message>>();
	private CommunicatorInputThread inputThread;
	
	private volatile boolean running;

	public CommunicatorInput(int port) throws IOException {
		socket = new ServerSocket(port, 128);
		socket.setReuseAddress(true);

		inputThread = new CommunicatorInputThread();
		running = true;
		inputThread.start();
	}

	public synchronized BlockingQueue<Message> registerService(int bundleId) {
		if (queues.containsKey(bundleId)) {
			throw new CommunicatorException("Queue already registered!");
		}

		BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
		queues.put(bundleId, queue);

		return queue;
	}

	public synchronized void destroy() {
		running = false;

		if (inputThread != null && inputThread.isAlive()) {
			inputThread.interrupt();
		}

		inputThread = null;

		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {

			}
		}

		socket = null;
	}

	private class CommunicatorInputThread extends Thread {
		
		public CommunicatorInputThread() {
			setDaemon(true);
		}

		@Override
		public void run() {
			while (running) {
				try {
					Socket connection = socket.accept();
					ObjectInputStream input = null;

					try {
						input = new ObjectInputStream(
								connection.getInputStream());
						Message message = (Message) input.readObject();

						BlockingQueue<Message> queue;

						synchronized (CommunicatorInput.this) {
							queue = queues.get(message.getBundleId());
						}

						if (queue != null) {
							queue.put(message);
						}
					} finally {
						connection.close();
						input.close();
					}
				} catch (InterruptedException e) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
