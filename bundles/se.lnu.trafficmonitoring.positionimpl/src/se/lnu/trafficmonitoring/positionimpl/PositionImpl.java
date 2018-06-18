package se.lnu.trafficmonitoring.positionimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.AddressMapperUser;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.communicatorutil.MessageListener;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.data.TMMessage;
import se.lnu.trafficmonitoring.position.Position;

/**
 * The default Position implementation.
 */
public class PositionImpl implements Position, AddressMapperUser {
	private List<Integer> positions = new ArrayList<Integer>();
	private Map<Integer, Integer> positionMap = new HashMap<Integer, Integer>();
	
	private AddressMapper addressMapper;
	
	public PositionImpl(MessageConsumer messageConsumer) {
		messageConsumer.addListener(TMMessage.SET_POSITIONS, new SetPositionsListener());
	}
	
	@Override
	public synchronized int getPosition(int cameraId) {
		if (!isInitialized()) {
			throw new IllegalStateException("Position not yet initialized.");
		}
		
		Integer position = positionMap.get(cameraId);
		
		if (position == null) {
			return -1;
		} else {
			return position;
		}
	}
	
	@Override
	public synchronized int getOwnPosition() {
		return getPosition(addressMapper.getOwnPid());
	}
	
	@Override
	public synchronized int getLeftCameraId() {
		int position = getPosition(addressMapper.getOwnPid());
		
		if (position < 1) {
			return TMConstants.UNKNOWN_PID;
		}
		
		return positions.get(position - 1);
	}
	
	@Override
	public synchronized int getRightCameraId() {
		int position = getPosition(addressMapper.getOwnPid());
		
		if (position < 0) {
			return TMConstants.UNKNOWN_PID;
		}
		
		position++;
		
		if (positions.size() <= position) {
			return TMConstants.UNKNOWN_PID;
		}
		
		return positions.get(position);
	}
	
	@Override
	public synchronized void setPositions(List<Integer> positions) {
		if (positions == null) {
			throw new NullPointerException("Parameter \"positions\" must not be null.");
		}
		
		this.positions = new ArrayList<Integer>(positions);
		updatePositionMap();
	}
	
	private synchronized void updatePositionMap() {
		int positionCount = positions.size();
		positionMap.clear();
		
		for (int i = 0; i < positionCount; i++) {
			positionMap.put(positions.get(i), i);
		}
	}
	
	@Override
	public synchronized void setAddressMapper(AddressMapper addressMapper) {
		this.addressMapper = addressMapper;
	}
	
	public synchronized boolean isInitialized() {
		return addressMapper != null;
	}
	
	private class SetPositionsListener implements MessageListener {
		@Override
		@SuppressWarnings("unchecked")
		public void receiveMessage(Message message) {
			setPositions((List<Integer>) message.getPayload());
		}
	}
}
