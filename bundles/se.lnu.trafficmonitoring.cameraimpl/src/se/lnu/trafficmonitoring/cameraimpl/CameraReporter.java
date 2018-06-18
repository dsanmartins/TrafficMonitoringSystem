package se.lnu.trafficmonitoring.cameraimpl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.lnu.trafficmonitoring.camera.Camera;
import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.AddressMapperUser;
import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.CommunicatorUser;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicator.MessageFactory;
import se.lnu.trafficmonitoring.communicator.MessageFactoryUser;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.communicatorutil.MessageListener;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.data.TMMessage;
import se.lnu.trafficmonitoring.data.TrafficCondition;
import se.lnu.trafficmonitoring.orgcontroller.OrgController;
import se.lnu.trafficmonitoring.orgcontroller.OrgControllerUser;
import se.lnu.trafficmonitoring.orgcontroller.OrgRole;
import se.lnu.trafficmonitoring.position.Position;
import se.lnu.trafficmonitoring.position.PositionUser;
import se.lnu.trafficmonitoring.trafficmonitor.TrafficMonitor;
import se.lnu.trafficmonitoring.trafficmonitor.TrafficMonitorUser;

/**
 * Reports the degree of traffic to the Client GUI on regular intervals.
 */
public class CameraReporter implements TrafficMonitorUser, AddressMapperUser,
		OrgControllerUser, CommunicatorUser, MessageFactoryUser, PositionUser {
	
	private volatile boolean running;
	private ReporterThread thread;

	private TrafficMonitor trafficMonitor;
	private AddressMapper addressMapper;
	private OrgController orgController;
	private Communicator communicator;
	private MessageFactory messageFactory;
	private Position position;
	
	private Camera camera;
	
	/**
	 * Information from other cameras. This may be updated slowly as slaves only
	 * provide this information during congestions.
	 */
	private Map<Integer, TrafficCondition> trafficConditions = new HashMap<Integer, TrafficCondition>();
	
	public CameraReporter(MessageConsumer messageConsumer, Camera camera) {
		setCamera(camera);
		messageConsumer.addListener(TMMessage.SET_TRAFFIC_CONDITION, new SetTrafficConditionListener());
	}
	
	@Override
	public synchronized void setAddressMapper(AddressMapper addressMapper) {
		this.addressMapper = addressMapper;
		tryStartReporterThread();
	}
	
	@Override
	public synchronized void setTrafficMonitor(TrafficMonitor trafficMonitor) {
		this.trafficMonitor = trafficMonitor;
		tryStartReporterThread();
	}
	
	
	@Override
	public synchronized void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
		tryStartReporterThread();
	};
	
	@Override
	public synchronized void setOrgController(OrgController orgController) {
		this.orgController = orgController;
		tryStartReporterThread();
	}
	
	@Override
	public synchronized void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
		tryStartReporterThread();
	};
	
	public synchronized void setPosition(Position position) {
		this.position = position;
		tryStartReporterThread();
	}
	
	private synchronized void tryStartReporterThread() {
		destroy();
		
		if (!isInitialized()) {
			return;
		}
		
		running = true;
		thread = new ReporterThread();
		thread.start();
	}
	
	public synchronized boolean isInitialized() {
		return addressMapper != null && trafficMonitor != null
				&& orgController != null && communicator != null
				&& messageFactory != null && position != null;
	}
	
	public synchronized void destroy() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
			thread = null;
		}
		
		running = false;
	}
	
	private synchronized void doReportToMaster(boolean congestion, int localTraffic) {
		int masterPid = orgController.getMaster();
		
		/* Don't report to ourself. */
		if (addressMapper.getOwnPid() == masterPid) {
			return;
		}
		
		TrafficCondition localTrafficCondition = new TrafficCondition(localTraffic, congestion);
		
		Message message = messageFactory.createMessage(
				masterPid,
				TMConstants.CAMERA_BID,
				TMMessage.SET_TRAFFIC_CONDITION
		);
		
		message.setPayload(localTrafficCondition);
		communicator.send(message);
	}
	
	private synchronized void doCameraReport(boolean congestion, int localTraffic, int ownPid) {
		if (ownPid < 0) {
			throw new IllegalStateException("PID " + ownPid + " is illegal for Camera.");
		}
		
		int pos = position.getPosition(ownPid);
		
		if (pos < 0) {
			throw new IllegalStateException("Position for camera " + ownPid + " is unknown.");
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<model>\n\t<camera id=\"");
		builder.append(ownPid);
		builder.append("\" position=\"");
		builder.append(pos);
		builder.append("\" traffic=\"");
		builder.append(localTraffic);
		builder.append("\" congestion=\"");
		builder.append(congestion);
		builder.append("\" />\n");
		builder.append("</model>");
		
		sendPostRequest(TMConstants.CAMERA_SERVLET, builder.toString());
	}
	
	private synchronized void doOrganizationReport(boolean congestion, int localTraffic) {
		int pid = addressMapper.getOwnPid();
		
		if (pid < 0) {
			throw new IllegalStateException("Illegal PID for Camera.");
		}
		
		Set<Integer> cameraSet = new HashSet<Integer>();
		OrgRole role = orgController.getOrganizationalRole();
		TrafficCondition localTrafficCondition = new TrafficCondition(localTraffic, congestion);
		
		if (role == OrgRole.SINGLE) {
			cameraSet.add(pid);
			trafficConditions.put(pid, localTrafficCondition);
		} else if (role == OrgRole.MASTER) {
			cameraSet.add(pid);
			trafficConditions.put(pid, localTrafficCondition);
			cameraSet.addAll(orgController.getSlaves());
		} else {
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<model organization=\"");
		builder.append(pid);
		builder.append("\">\n");
		
		for (Integer cameraId : cameraSet) {
			TrafficCondition trafficCondition = trafficConditions.get(cameraId);
			int cameraPosition = position.getPosition(cameraId);
			
			if (cameraPosition < 0) {
				throw new IllegalStateException("Position for camera " + cameraId + " is unknown.");
			}
			
			builder.append("\t<camera id=\"");
			builder.append(cameraId);
			builder.append("\" position=\"");
			builder.append(cameraPosition);
			
			if (trafficCondition != null) {
				builder.append("\" traffic=\"");
				builder.append(trafficCondition.getTraffic());
				builder.append("\" congestion=\"");
				builder.append(trafficCondition.getCongestion());
			} else {
				builder.append("\" congestion=\"");
				builder.append(congestion);
			}
			
			builder.append("\" />\n");
		}
		
		builder.append("</model>");
		
		sendPostRequest(TMConstants.ORGANIZATION_SERVLET, builder.toString());
	}
	
	private synchronized void sendPostRequest(String servlet, String data) {
		HttpURLConnection connection = null;
		OutputStreamWriter writer = null;
		
		try {
			URL url = new URL(servlet);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(data);
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

	private synchronized void setCamera(Camera camera) {
		if (camera == null) {
			throw new NullPointerException("Parameter \"camera\" must not be null");
		}
		
		this.camera = camera;
	}

	private class SetTrafficConditionListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			int cameraId = message.getSenderPid();
			TrafficCondition trafficCondition = (TrafficCondition) message.getPayload();
			
			synchronized (CameraReporter.this) {
				trafficConditions.put(cameraId, trafficCondition);
			}
		}
	}
	
	private class ReporterThread extends Thread {
		private boolean congestion = false;
		private OrgRole role;
		
		public ReporterThread() {
			setDaemon(true);
		}
		
		@Override
		public void run() {
			int ownPid = TMConstants.UNKNOWN_PID;
			
			while (running) {
				try {
					OrgRole currentRole;
					boolean currentCongestion;
					int localTraffic;
					
					synchronized (CameraReporter.this) {
						localTraffic = camera.getLocalTraffic();
						trafficMonitor.updateLocalTraffic();
						currentCongestion = trafficMonitor.isCongestion();
					}
					
					try {
						ownPid = addressMapper.getOwnPid();
						doCameraReport(currentCongestion, localTraffic, ownPid);
					
					/* Avoids flooding the console when not yet initialized. */
					} catch (IllegalStateException e) {
						if (TMConstants.INITIALIZATION_DEBUG) {
							e.printStackTrace();
						} else if (ownPid != TMConstants.UNKNOWN_PID) {
							e.printStackTrace();
						}
						
						sleep(1000);
						continue;
					}
					
					/* Allow the organization middleware to catch up. */
					sleep(1000);
					
					doReportToMaster(currentCongestion, localTraffic);
					
					currentRole = orgController.getOrganizationalRole();
					if (!TMConstants.FORCE_NOTIFY_CLIENT) {
						if (currentCongestion == congestion && currentRole == role && currentRole != OrgRole.MASTER) {
							/* Keep the pace. */
							sleep(1000);
							continue;
						} else {
							role = currentRole;
							congestion = currentCongestion;
						}
					}
					
					/* Allow the master to catch up with the clients' LocalTraffic. */
					sleep(1000);
					
					if (orgController.getOrganizationalRole() != OrgRole.SLAVE) {
						doOrganizationReport(currentCongestion, localTraffic);
					}
					
				} catch (InterruptedException e) {
					running = false;
					return;
					
				} catch (Exception e) {
					e.printStackTrace();
					
					try {
						/* Avoid flooding. */
						sleep(1000);
					} catch (InterruptedException ex) {
						running = false;
						return;
					}
				}
			}
		}
	}
}
