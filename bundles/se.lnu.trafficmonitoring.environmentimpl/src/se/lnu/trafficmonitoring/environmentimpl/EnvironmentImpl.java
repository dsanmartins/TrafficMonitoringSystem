package se.lnu.trafficmonitoring.environmentimpl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.AddressMapperUser;
import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.CommunicatorUser;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicator.MessageFactory;
import se.lnu.trafficmonitoring.communicator.MessageFactoryUser;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.communicatorutil.MessageListener;
import se.lnu.trafficmonitoring.data.PidToAddress;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.data.TMMessage;
import se.lnu.trafficmonitoring.environment.Environment;
import se.lnu.trafficmonitoring.trafficmodel.TrafficModel;
import se.lnu.trafficmonitoring.trafficmodel.TrafficModelUser;

/**
 * Default implementation of the Environment interface. Reports degrees of
 * traffic both to the Agents and the Client GUI.
 */
public class EnvironmentImpl implements Environment, AddressMapperUser, TrafficModelUser, CommunicatorUser, MessageFactoryUser {
	
	private AddressMapper addressMapper;
	private TrafficModel trafficModel;
	private Communicator communicator;
	private MessageFactory messageFactory;

	private Map<Integer, SocketAddress> localAddressMap = new HashMap<Integer, SocketAddress>();
	private List<Integer> cameras = new ArrayList<Integer>();

	private int totalTimesteps = 0;
	private int timestep = 0;
	
	private volatile boolean running = false;

	private EnvironmentTimeThread thread;
	
	public EnvironmentImpl(MessageConsumer messageConsumer) {
		messageConsumer.addListener(TMMessage.GET_LOCAL_TRAFFIC, new GetLocalTrafficListener());
	}

	@Override
	public void addSubscriber(String subscriberUrl) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public synchronized void addCamera(int cameraID) {
		SocketAddress cameraAddress;
		
		if (cameraID < TMConstants.AGENT_ADDRESSES.length && TMConstants.AGENT_ADDRESSES[cameraID] != null) {
			cameraAddress = TMConstants.AGENT_ADDRESSES[cameraID];
		} else {
			cameraAddress = new InetSocketAddress("localhost", TMConstants.BASE_PORT + cameraID);
		}
		
		addCamera(cameraID, cameraAddress);
	}

	public void addCamera(final int cameraID, SocketAddress cameraAddress) {
		if (!isInitialized()) {
			throw new IllegalStateException("Environment not yet initialized.");
		}
		
		if (cameraID < 0) {
			throw new IllegalArgumentException("Camera ID must be less than 0.");
		}

		if (cameras.contains(cameraID)) {
			throw new IllegalArgumentException("Camera ID " + cameraID + " alread exists!");
		}
		
		if (cameraAddress == null) {
			throw new NullPointerException("Parameter \"cameraAddress\" must not be null.");
		}
		
		trafficModel.setTotalCameras(cameras.size() + 1);
		
		/* Register the address of the new camera. */
		addressMapper.setAddress(cameraID, cameraAddress);
		
		/* New positions. */
		ArrayList<Integer> positions = new ArrayList<Integer>(cameras.size() + 1);
		positions.addAll(cameras);
		positions.add(cameraID);
		
		/* Make sure the camera knows it's own ID. */
		Message message = messageFactory.createMessage(
				cameraID,
				TMConstants.COMMUNICATOR_BID,
				TMMessage.SET_OWN_PID
		);
		
		message.setPayload(cameraID);
		communicator.send(message);
		
		/* Give all camera addresses to the new camera. */
		message = messageFactory.createMessage(
				cameraID,
				TMConstants.COMMUNICATOR_BID,
				TMMessage.SET_ADDRESS_MAP
		);
		
		message.setPayload(localAddressMap);
		communicator.send(message);
		
		/* Give the positions to the new camera. */
		message = messageFactory.createMessage(
				cameraID,
				TMConstants.POSITION_BID,
				TMMessage.SET_POSITIONS
		);
		
		message.setPayload(positions);
		communicator.send(message);
		
		/* Informs all cameras about the new camera. */
		PidToAddress newCameraPidToAddress = new PidToAddress(cameraID, cameraAddress);
		for (Integer otherCameraId : cameras) {
			try {
				message = messageFactory.createMessage(
						otherCameraId,
						TMConstants.COMMUNICATOR_BID,
						TMMessage.SET_PID_TO_ADDRESS
				);
				
				message.setPayload(newCameraPidToAddress);
				communicator.send(message);
				
				message = messageFactory.createMessage(
						otherCameraId,
						TMConstants.POSITION_BID,
						TMMessage.SET_POSITIONS
				);
				
				message.setPayload(positions);
				communicator.send(message);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/* Store the information about the new camera. */
		localAddressMap.put(cameraID, cameraAddress);
		cameras.add(cameraID);
		
		tryStartThread();
	}

	@Override
	public synchronized int monitorTraffic(int cameraID) {
		if (trafficModel == null) {
			throw new IllegalStateException("TrafficModel is unavailable.");
		}

		return trafficModel.getLocalTraffic(cameraID, timestep);
	}

	@Override
	public synchronized List<Integer> getAllCameraIDs() {
		return new ArrayList<Integer>(cameras);
	}

	@Override
	public synchronized int getTotalNumberOfCameras() {
		return cameras.size();
	}

	@Override
	public synchronized void setAddressMapper(AddressMapper addressMapper) {
		this.addressMapper = addressMapper;

		if (addressMapper == null) {
			return;
		}

		// We are the environment.
		addressMapper.setOwnPid(TMConstants.ENVIRONMENT_PID);
		localAddressMap.put(TMConstants.ENVIRONMENT_PID, addressMapper.getOwnAddress());
		
		tryStartThread();
	}

	@Override
	public synchronized void setTrafficModel(TrafficModel trafficModel) {
		this.trafficModel = trafficModel;
		tryStartThread();
	}

	@Override
	public synchronized void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
		tryStartThread();
	}

	@Override
	public synchronized void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
		tryStartThread();
	}

	public synchronized boolean isInitialized() {
		return addressMapper != null && trafficModel != null
				&& messageFactory != null && communicator != null;
	}

	public synchronized void destroy() {
		running = false;

		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}

		thread = null;
	}

	private synchronized void tryStartThread() {
		if (!isInitialized() || running || cameras.size() < 1) {
			return;
		}

		timestep = 0;
		running = true;
		thread = new EnvironmentTimeThread();
		thread.start();
	}
	
	private synchronized int getLocalTraffic(int camera, int timestep) {
		/* Add time steps as we encounter them. */
		if (totalTimesteps <= timestep) {
			totalTimesteps += 100;
			trafficModel.setTotalTimeSteps(getTotalNumberOfCameras() + totalTimesteps);
		}
		
		return trafficModel.getLocalTraffic(camera, timestep);
	}

	private synchronized void nextTimestep() {
		if (!isInitialized()) {
			throw new IllegalStateException("Environment not yet initialized!");
		}
		
		timestep++;

		/* Update the GUI. */
		StringBuilder builder = new StringBuilder();
		builder.append("<model>\n");
		
		int cameraCount = cameras.size();
		
		for (int i = 0; i < cameraCount; i++) {
			int camera = cameras.get(i);
			int localTraffic = getLocalTraffic(camera, timestep);

			builder.append("\t<camera id=\"");
			builder.append(camera);
			builder.append("\" traffic=\"");
			builder.append(localTraffic);
			builder.append("\" position=\"");
			builder.append(i);
			builder.append("\" congestion=\"");
			builder.append(TMConstants.TRAFFIC_CONGESTION <= localTraffic);
			builder.append("\" />\n");
		}
		builder.append("</model>\n");

		String xml = builder.toString();
		HttpURLConnection connection = null;
		OutputStreamWriter writer = null;

		try {
			URL url = new URL(TMConstants.ENVIRONMENT_SERVLET);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(xml);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {

			}

			try {
				connection.getInputStream().close();
			} catch (Exception e) {

			}
		}
	}
	
	private class GetLocalTrafficListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			int cameraId = message.getSenderPid();
			
			Message response = messageFactory.createMessage(
					cameraId,
					TMConstants.CAMERA_BID,
					TMMessage.SET_LOCAL_TRAFFIC
			);
			
			response.setPayload(getLocalTraffic(cameraId, timestep));
			communicator.send(response);
		}
	}

	private class EnvironmentTimeThread extends Thread {
		public EnvironmentTimeThread() {
			setDaemon(true);
		}

		@Override
		public void run() {
			if (TMConstants.MANUAL_TIME_STEPS) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						while (true) {
							JOptionPane.showMessageDialog(
									null,
									"Close me to progress to the next time step!"
							);
							
							nextTimestep();
						}
					}
				});
				
			} else {
				while (running) {
					try {
						sleep(10000);
						nextTimestep();
						
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
}
