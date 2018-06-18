package se.lnu.trafficmonitoring.communicatorimpl;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.communicatorutil.MessageListener;
import se.lnu.trafficmonitoring.data.PidToAddress;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.data.TMMessage;

/**
 * Default implementation of AddressMapper. Maps camera (process) IDs to
 * SocketAddresses. Also used to provide information about a process' own PID
 * and SocketAddress.
 */
public class AddressMapperImpl implements AddressMapper {
	private volatile int ownPid = TMConstants.UNKNOWN_PID;
	private volatile SocketAddress ownAddress;
	
	private Map<Integer, SocketAddress> addressMap = new HashMap<Integer, SocketAddress>();
	
	public AddressMapperImpl(MessageConsumer consumer) {
		consumer.addListener(TMMessage.SET_OWN_PID, new SetOwnPidListener());
		consumer.addListener(TMMessage.SET_ADDRESS_MAP, new SetAddressMapListener());
		consumer.addListener(TMMessage.SET_PID_TO_ADDRESS, new SetPidToAddressListener());
	}

	@Override
	public void setOwnPid(int pid) {
		this.ownPid = pid;
	}

	@Override
	public int getOwnPid() {
		return ownPid;
	}

	@Override
	public synchronized void setAddress(int pid, SocketAddress address) {
		addressMap.put(pid, address);
	}

	@Override
	public synchronized SocketAddress getAddress(int pid) {
		return addressMap.get(pid);
	}

	@Override
	public synchronized void removeAddress(int pid) {
		addressMap.remove(pid);
	}

	@Override
	public synchronized boolean containsAddress(int pid) {
		return addressMap.containsKey(pid);
	}
	
	@Override
	public void setOwnAddress(SocketAddress address) {
		this.ownAddress = address;
	}

	@Override
	public SocketAddress getOwnAddress() {
		return ownAddress;
	}
	
	private class SetOwnPidListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			ownPid = (int) message.getPayload();
		}
	}
	
	private class SetAddressMapListener implements MessageListener {
		@SuppressWarnings("unchecked")
		@Override
		public void receiveMessage(Message message) {
			synchronized (AddressMapperImpl.this) {
				addressMap = (Map<Integer, SocketAddress>) message.getPayload();
			}
		}
	}
	
	private class SetPidToAddressListener implements MessageListener {
		@Override
		public void receiveMessage(Message message) {
			synchronized (AddressMapperImpl.this) {
				PidToAddress pidToAddress = (PidToAddress) message.getPayload();
				addressMap.put(pidToAddress.getPid(), pidToAddress.getAddress());
			}
		}
	}
}
