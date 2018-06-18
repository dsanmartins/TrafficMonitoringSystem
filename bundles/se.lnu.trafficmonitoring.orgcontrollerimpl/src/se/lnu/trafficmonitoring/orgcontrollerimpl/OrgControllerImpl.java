package se.lnu.trafficmonitoring.orgcontrollerimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.AddressMapperUser;
import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.CommunicatorUser;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicator.MessageFactory;
import se.lnu.trafficmonitoring.communicator.MessageFactoryUser;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.communicatorutil.MessageListener;
import se.lnu.trafficmonitoring.data.MergeLeftRequest;
import se.lnu.trafficmonitoring.data.MergeRightResponse;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.data.TMMessage;
import se.lnu.trafficmonitoring.orgcontroller.OrgController;
import se.lnu.trafficmonitoring.orgcontroller.OrgRole;
import se.lnu.trafficmonitoring.position.Position;
import se.lnu.trafficmonitoring.position.PositionUser;

/**
 * Default implementation of OrgController.
 */
public class OrgControllerImpl implements OrgController, AddressMapperUser,
		CommunicatorUser, MessageFactoryUser, PositionUser {
	
	private OrgRole role = OrgRole.SINGLE;
	private int master = TMConstants.UNKNOWN_PID;
	private Set<Integer> slaves = new HashSet<Integer>();
	
	private AddressMapper addressMapper;
	private Communicator communicator;
	private MessageFactory messageFactory;
	private Position position;
	
	private boolean congested = false;
	private OrgState state = OrgState.IDLE;
	
	private Message delayedMergeLeftRequest;
	private Message returnedMergeLeftRequest;
	
	private boolean resendSetMaster = false;
	private boolean makeDelayedSplitRequest = false;
	
	public OrgControllerImpl(MessageConsumer messageConsumer) {
		messageConsumer.addListener(TMMessage.MERGE_LEFT_DELAY, new MergeLeftDelayListener());
		messageConsumer.addListener(TMMessage.MERGE_LEFT_REQUEST, new MergeLeftRequestListener());
		messageConsumer.addListener(TMMessage.MERGE_LEFT_RESPONSE, new MergeLeftResponseListener());
		messageConsumer.addListener(TMMessage.MERGE_RIGHT_REQUEST, new MergeRightRequestListener());
		messageConsumer.addListener(TMMessage.MERGE_RIGHT_RESPONSE, new MergeRightResponseListener());
		messageConsumer.addListener(TMMessage.SET_ORGANIZATION_MASTER, new SetOrganizationMasterListener());
		messageConsumer.addListener(TMMessage.CREATE_ORGANIZATION, new CreateOrganizationListener());
		messageConsumer.addListener(TMMessage.SPLIT_REQUEST, new SplitRequestListener());
		messageConsumer.addListener(TMMessage.SPLIT_RESPONSE, new SplitResponseListener());
	}

	@Override
	public synchronized void setCongestedTraffic() {
		if (!isInitialized()) {
			throw new IllegalStateException("OrgController not yet initialized.");
		}
		
		if (congested || state != OrgState.IDLE) {
			return;
		}
		
		if (sendMergeRightRequest()) {
			state = OrgState.MERGING_RIGHT;
			
		} else if (sendMergeLeftRequest()) {
			state = OrgState.MERGING_LEFT;
		}
		
		congested = true;
	}

	@Override
	public synchronized void setFreeflowTraffic() {
		if (!isInitialized()) {
			throw new IllegalStateException("OrgController not yet initialized.");
		}
		
		if (!congested || state != OrgState.IDLE) {
			return;
		}
		
		if (role == OrgRole.SINGLE) {
			congested = false;
			return;
		}
		
		performSplitOrganization();
		congested = false;
	}

	@Override
	public synchronized OrgRole getOrganizationalRole() {
		if (!isInitialized()) {
			throw new IllegalStateException("OrgController not yet initialized.");
		}
		
		return role;
	}

	@Override
	public synchronized int getMaster() {
		if (!isInitialized()) {
			throw new IllegalStateException("OrgController not yet initialized.");
		}
		
		if (master == TMConstants.UNKNOWN_PID && role != OrgRole.SLAVE) {
			master = addressMapper.getOwnPid();
		}
		
		return master;
	}

	@Override
	public synchronized List<Integer> getSlaves() {
		return new ArrayList<Integer>(slaves);
	}
	
	/**
	 * Ask our right neighbor to merge.
	 * @return True if a request was sent.
	 */
	private synchronized boolean sendMergeRightRequest() {
		int rightCameraId = position.getRightCameraId();
		
		if (rightCameraId == TMConstants.UNKNOWN_PID) {
			debug("There are no cameras to our right.");
			return false;
		}
		
		Message message = messageFactory.createMessage(
				rightCameraId,
				TMConstants.ORG_CONTROLLER_BID,
				TMMessage.MERGE_RIGHT_REQUEST
		);
		
		debug("Sending merge right request to " + rightCameraId);
		communicator.send(message);
		
		return true;
	}
	
	/**
	 * Ask our left neighbor to merge.
	 * @return True if a request was sent.
	 */
	private synchronized boolean sendMergeLeftRequest() {
		int leftCameraId = position.getLeftCameraId();
		
		if (leftCameraId == TMConstants.UNKNOWN_PID) {
			debug("There are no cameras to our left.");
			return false;
		}
		
		Message message = messageFactory.createMessage(
				leftCameraId,
				TMConstants.ORG_CONTROLLER_BID,
				TMMessage.MERGE_LEFT_REQUEST
		);
		
		int ownPid = addressMapper.getOwnPid();
		List<Integer> potentialSlaves = new ArrayList<Integer>(slaves.size() + 1);
		potentialSlaves.add(ownPid);
		potentialSlaves.addAll(slaves);
		message.setPayload(new MergeLeftRequest(ownPid, potentialSlaves));
		communicator.send(message);
		
		debug("Sending merge left request to " + leftCameraId);
		return true;
	}
	
	/**
	 * Introduce the current master to our slaves.
	 */
	private synchronized void sendSetMaster() {
		for (Integer slave : slaves) {
			debug("Send set master to " + slave);
			try {
				Message introduction = messageFactory.createMessage(
						slave,
						TMConstants.ORG_CONTROLLER_BID,
						TMMessage.SET_ORGANIZATION_MASTER
				);
				
				introduction.setPayload(getMaster());
				communicator.send(introduction);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Splits this organization into two parts and informs the master of the new organization.
	 */
	private synchronized void performSplitOrganizationAsMaster(int newOrgMasterPosition) {
		if (role != OrgRole.MASTER) {
			throw new IllegalStateException("We are not the master.");
		}
		
		/* We may only split away Agents to our right. */
		if (newOrgMasterPosition <= position.getOwnPosition() || newOrgMasterPosition < 1) {
			throw new IllegalArgumentException("The \"newOrgMasterPosition\" must be known and on our right side.");
		}
		
		int newOrgMasterPid = TMConstants.UNKNOWN_PID;
		Set<Integer> newOrgSlaves = new HashSet<Integer>();
		
		for (Integer slave : slaves) {
			int slavePosition = position.getPosition(slave);
			
			if (newOrgMasterPosition == slavePosition) {
				newOrgMasterPid = slave;
			} else if (newOrgMasterPosition < slavePosition) {
				newOrgSlaves.add(slave);
			}
		}
		
		slaves.remove(newOrgMasterPid);
		slaves.removeAll(newOrgSlaves);
		
		if (slaves.isEmpty()) {
			role = OrgRole.SINGLE;
		}
		
		/* The new organization would have been empty. */
		if (newOrgMasterPid == TMConstants.UNKNOWN_PID) {
			debug("Not sending CreateOrganization as it was empty.");
			return;
		}
		
		/* Assign the master of the new organization. */
		Message splitMessage = messageFactory.createMessage(
				newOrgMasterPid,
				TMConstants.ORG_CONTROLLER_BID,
				TMMessage.CREATE_ORGANIZATION
		);
		
		splitMessage.setPayload(newOrgSlaves);
		
		debug("Sending CreateOrganization to " + newOrgMasterPid);
		communicator.send(splitMessage);
	}
	
	/**
	 * Requests an organization split.
	 * @return True if a split attempt was made.
	 */
	private synchronized boolean performSplitOrganization() {
		if (role == OrgRole.MASTER) {
			performSplitOrganizationAsMaster(position.getOwnPosition() + 1);
			
			return true;
			
		} else if (role == OrgRole.SLAVE) {
			state = OrgState.SPLITTING;
			
			Message message = messageFactory.createMessage(
					getMaster(),
					TMConstants.ORG_CONTROLLER_BID,
					TMMessage.SPLIT_REQUEST
			);
			
			debug("Sending split request to " + getMaster());
			
			communicator.send(message);
			
			return true;
			
		} else {
			debug("Don't split, we are single.");
			
			return false;
			
		}
	}
	
	/**
	 * Called when our left neighbor wishes to merge with us.
	 */
	private synchronized void handleMergeRightRequest(Message message) {
		Message response = messageFactory.createMessage(
				message.getSenderPid(),
				TMConstants.ORG_CONTROLLER_BID,
				TMMessage.MERGE_RIGHT_RESPONSE
		);
		
		/*
		 * Note, if our left neighbor attempts to merge while we are merging
		 * with our right neighbor, then we deny the request in order to
		 * postpone the merge. We can do this as we will send a merge request to
		 * our left neighbor once we are done merging with our right.
		 */
		
		if (!congested || state == OrgState.MERGING_RIGHT) {
			if (!congested) {
				debug("No, we are not congested.");
			} else {
				debug("No, we are already mergning.");
			}
			
			response.setPayload(new MergeRightResponse(false));
			communicator.send(response);
			return;
		}
		
		Set<Integer> mergeSlaves = slaves;
		mergeSlaves.add(addressMapper.getOwnPid());
		
		master = message.getSenderPid();
		role = OrgRole.SLAVE;
		slaves = new HashSet<Integer>();
		
		response.setPayload(new MergeRightResponse(true, mergeSlaves));
		
		debug("Merging with " + getMaster());
		communicator.send(response);
	}
	
	/**
	 * Called when our right neighbor responds to our merge request.
	 */
	private synchronized void handleMergeRightResponse(Message message) {
		if (state != OrgState.MERGING_RIGHT) {
			throw new IllegalStateException("Received MergeRightResponse while not merging.");
		}
		
		MergeRightResponse mergeRightResponse = (MergeRightResponse) message.getPayload();
		
		if (mergeRightResponse.isAccepted()) {
			debug("Merge right accepted by " + message.getSenderPid());
			
			master = addressMapper.getOwnPid();
			role = OrgRole.MASTER;
			slaves.addAll(mergeRightResponse.getCameras());
			sendSetMaster();
		} else {
			debug("Merge right rejected by " + message.getSenderPid());
		}
		
		/* Merge left request. */
		if (sendMergeLeftRequest()) {
			state = OrgState.MERGING_LEFT;
		} else {
			state = OrgState.IDLE;
		}
	}
	
	/**
	 * Called when our right neighbor wishes to merge with us.
	 */
	private synchronized void handleMergeLeftRequest(Message message) {
		MergeLeftRequest mergeLeftRequest = (MergeLeftRequest) message.getPayload();
		
		/*
		 * We received a request from our right while we are merging
		 * with our left neighbor. We delay this until our left neighbor has
		 * responded.
		 */
		
		if (state == OrgState.MERGING_LEFT) {
			debug("We are already merging, deplay merge left request from " + message.getSenderPid());
			
			if (delayedMergeLeftRequest != null) {
				throw new IllegalStateException("Has multiple merge left requests.");
			}
			
			delayedMergeLeftRequest = message;
			return;
		}
			
		/* Masters are responsible for handling merge requests. */
		if (role == OrgRole.SLAVE) {
			debug("Forwarding left merge by " + mergeLeftRequest.getSourcePid() + " request to " + getMaster());
			
			message.setReceiverPid(getMaster());
			communicator.send(message);
			return;
		}
		
		/*
		 * We are or used to be a master, but the request was forwarded by an
		 * Agent which is not our slave. Return the message for later
		 * processing.
		 */
		if (mergeLeftRequest.getSourcePid() != message.getSenderPid() && !slaves.contains(message.getSenderPid())) {
			debug("Return the request by " + mergeLeftRequest.getSourcePid() + " to forwarder " + message.getSenderPid());
			
			message.setReceiverPid(message.getSenderPid());
			message.setType(TMMessage.MERGE_LEFT_DELAY);
			communicator.send(message);
			return;
		}
		
		Message response = messageFactory.createMessage(
				mergeLeftRequest.getSourcePid(),
				TMConstants.ORG_CONTROLLER_BID,
				TMMessage.MERGE_LEFT_RESPONSE
		);
		
		/* We will not merge. */
		if (!congested) {
			debug("Not congested, refuse left merge request.");
			
			response.setPayload(false);
			communicator.send(response);
			return;
		}
		
		/* Merge. */
		master = addressMapper.getOwnPid();
		role = OrgRole.MASTER;
		slaves.addAll(mergeLeftRequest.getCameras());
		
		response.setPayload(true);
		debug("Accepted left merge request.");
		
		communicator.send(response);
		sendSetMaster();
	}
	
	/**
	 * Called when our left neighbor responds to our merge request.
	 */
	private synchronized void handleMergeLeftResponse(Message message) {
		if (state != OrgState.MERGING_LEFT) {
			throw new IllegalStateException("Received MergeLeftResponse while not merging.");
		}
		
		boolean willMerge = (boolean) message.getPayload();
		
		if (willMerge) {
			master = message.getSenderPid();
			role = OrgRole.SLAVE;
			slaves.clear();
			
			resendSetMaster = false;
			debug("Our left merge request was accepted by " + message.getSenderPid());
		} else {
			/* In case someone wanted to split while we were merging. */
			if (resendSetMaster) {
				resendSetMaster = false;
				sendSetMaster();
			}
			
			debug("Our left merge request was rejected by " + message.getSenderPid());
		}
		
		state = OrgState.IDLE;
		
		/* In case someone wanted to merge while we were merging. */
		if (delayedMergeLeftRequest != null) {
			Message delayedMessage = delayedMergeLeftRequest;
			delayedMergeLeftRequest = null;
			handleMergeLeftRequest(delayedMessage);
		}
	}
	
	/**
	 * We forwarded our request to the wrong master, wait for our new master to
	 * present itself.
	 */
	private synchronized void handleMergeLeftDelay(Message message) {
		int returnerPid = message.getSenderPid();
		MergeLeftRequest mergeLeftRequest = (MergeLeftRequest) message.getPayload();
		
		/* The returned message was not successfully forwarded. */
		message.setSenderPid(mergeLeftRequest.getSourcePid());
		message.setType(TMMessage.MERGE_LEFT_REQUEST);
		
		if (returnerPid != getMaster()) {
			message.setReceiverPid(getMaster());
			
			if (role == OrgRole.SLAVE) {
				debug("Resend left merge request by " + mergeLeftRequest.getSourcePid());
				communicator.send(message);
			} else {
				debug("We are master, handle left merge request by " + mergeLeftRequest.getSourcePid());
				handleMergeLeftRequest(message);
			}
		} else {
			debug("Delaying left merge request by " + mergeLeftRequest.getSourcePid());
			
			if (returnedMergeLeftRequest != null) {
				throw new IllegalStateException("A returned MergeLeftRequest already exists!");
			}
			
			returnedMergeLeftRequest = message;
		}
	}
	
	/**
	 * Called when we are claimed by a master as a slave.
	 */
	private synchronized void handleSetOrganizationMaster(Message message) {
		master = (int) message.getPayload();
		role = OrgRole.SLAVE;
		slaves.clear();
		
		debug("Our new master is " + message.getPayload());
		performTasksWaitingForNewOrganization();
	}
	
	/**
	 * Called when a slave wants to leave our organization.
	 */
	private synchronized void handleSplitRequest(Message message) {
		int senderPid = message.getSenderPid();
		
		Message response = messageFactory.createMessage(
				senderPid,
				TMConstants.ORG_CONTROLLER_BID,
				TMMessage.SPLIT_RESPONSE
		);
		
		/* The slave will soon get a new master, we ask it to wait for that. */
		if (!slaves.contains(senderPid)) {
			debug("Deny split request by" + senderPid);
			response.setPayload(false);
			communicator.send(response);
			return;
		}
		
		/* We are merging left, reject the request until we know who the master is. */
		if (state == OrgState.MERGING_LEFT) {
			debug("Deny split request by " + senderPid + " and maybe send set master again.");
			response.setPayload(false);
			communicator.send(response);
			resendSetMaster = true;
			return;
		}
		
		debug("Accept split request by " + senderPid);
		response.setPayload(true);
		communicator.send(response);
		slaves.remove(senderPid);
		
		performSplitOrganizationAsMaster(position.getPosition(senderPid) + 1);
	}
	
	/**
	 * Called when we receive a response to our split request.
	 */
	private synchronized void handleSplitResponse(Message message) {
		boolean willSplit = (boolean) message.getPayload();
		
		if (willSplit || role == OrgRole.SINGLE) {
			role = OrgRole.SINGLE;
			state = OrgState.IDLE;
			slaves.clear();
			
			debug("We successfully split.");
			performTasksWaitingForNewOrganization();
		
		/* We already know who our new master is, resend the request. */
		} else if (message.getSenderPid() != getMaster()) {
			performSplitOrganization();
			debug("Resent split request.");
		
		/* We need to wait for our new master to contact us. */
		} else {
			if (makeDelayedSplitRequest) {
				throw new IllegalStateException("Felayed splits already waiting.");
			}
			
			makeDelayedSplitRequest = true;
			state = OrgState.IDLE;
			
			debug("Delayed split request ontil new organization.");
		}
	}
	
	/**
	 * Called when we become a master of a new organization due to splitting.
	 */
	private synchronized void handleCreateOrganization(Message message) {
		@SuppressWarnings("unchecked")
		Collection<Integer> mySlaves = (Collection<Integer>) message.getPayload();
		
		master = addressMapper.getOwnPid();
		slaves.clear();
		slaves.addAll(mySlaves);
		
		if (slaves.isEmpty()) {
			role = OrgRole.SINGLE;
			debug("Created new single organization as requested by " + message.getSenderPid());
		} else {
			role = OrgRole.MASTER;
			debug("Create new master organization with " + slaves.size() + " as requested by + " + message.getSenderPid());
			sendSetMaster();
		}
		
		performTasksWaitingForNewOrganization();
	}
	
	/**
	 * Complete delayed tasks.
	 */
	private synchronized void performTasksWaitingForNewOrganization() {
		if (returnedMergeLeftRequest != null) {
			try {
				Message message = returnedMergeLeftRequest;
				returnedMergeLeftRequest = null;
				
				if (role == OrgRole.SLAVE) {
					debug("Resending left merge request to our new master.");
					message.setReceiverPid(getMaster());
					communicator.send(message);
				} else {
					debug("Handling returned left merge request ourself.");
					handleMergeLeftRequest(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (makeDelayedSplitRequest) {
			try {
				makeDelayedSplitRequest = false;
				
				if (role != OrgRole.SINGLE) {
					debug("Attemptint to split once again...");
					performSplitOrganization();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized boolean isInitialized() {
		return communicator != null && messageFactory != null
				&& position != null && addressMapper != null;
	}
	
	private synchronized void debug(String text) {
		if (TMConstants.ORGANIZATION_DEBUG) {
			int ownPid = addressMapper.getOwnPid();
			String method = new Throwable().getStackTrace()[1].getMethodName();
			String time = new SimpleDateFormat("kk:mm:ss").format(new Date());
			String info = ownPid + ", " + role + ", " + state + ", " + method + ", " + slaves.size();
			System.out.println(time + " [" + info  + "] " + text);
		}
	}
	
	/* Code below this comment is boilerplate code only. */

	@Override
	public synchronized void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public synchronized void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
	}

	@Override
	public synchronized void setAddressMapper(AddressMapper addressMapper) {
		this.addressMapper = addressMapper;
	}
	
	@Override
	public synchronized void setPosition(Position position) {
		this.position = position;
	}
	
	private class MergeRightRequestListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleMergeRightRequest(message);
		}
	}
	
	private class MergeRightResponseListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleMergeRightResponse(message);
		}
	}
	
	private class MergeLeftRequestListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleMergeLeftRequest(message);
		}
	}
	
	private class MergeLeftResponseListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleMergeLeftResponse(message);
		}
	}
	
	private class SetOrganizationMasterListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleSetOrganizationMaster(message);
		}
	}
	
	private class SplitRequestListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleSplitRequest(message);
		}
	}
	
	private class SplitResponseListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleSplitResponse(message);
		}
	}
	
	private class CreateOrganizationListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleCreateOrganization(message);
		}
	}
	
	private class MergeLeftDelayListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			handleMergeLeftDelay(message);
		}
	}
}
