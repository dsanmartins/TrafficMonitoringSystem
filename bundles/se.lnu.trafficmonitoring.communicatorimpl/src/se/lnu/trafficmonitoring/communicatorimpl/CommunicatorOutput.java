package se.lnu.trafficmonitoring.communicatorimpl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.CommunicatorException;
import se.lnu.trafficmonitoring.communicator.Message;
import se.lnu.trafficmonitoring.data.TMConstants;

/**
 * Part of the Communicator. Responsible for delivering messages to other
 * processes.
 */
public class CommunicatorOutput {
	private volatile AddressMapper addressMapper;
	
	public CommunicatorOutput(AddressMapper addressMapper) {
		setAddressMapper(addressMapper);
	}
	
	public void send(Message message) {
		if (addressMapper == null) {
			throw new CommunicatorException("AddressMapper is unavailable.");
		}
		
		int ownPid = addressMapper.getOwnPid();
		int recvPid = message.getReceiverPid();
		
		SocketAddress receiverAddress = addressMapper.getAddress(recvPid);
		
		if (receiverAddress == null) {
			if (TMConstants.INITIALIZATION_DEBUG) {
				throw new CommunicatorException("SocketAddress for receiverPid " + recvPid + " is unknown.");
			} else if (ownPid != TMConstants.UNKNOWN_PID || recvPid != TMConstants.ENVIRONMENT_PID) {
				throw new CommunicatorException("SocketAddress for receiverPid " + recvPid + " is unknown.");
			} else {
				return;
			}
		}
		
		message.setSenderPid(ownPid);
		
		Socket socket = null;
		ObjectOutputStream oos = null;
		
		try {
			socket = new Socket();
			socket.connect(receiverAddress);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(message);
		} catch (IOException e) {
			throw new CommunicatorException(e);
		} finally {
			try {
				oos.close();
				
			} catch (NullPointerException e) {
				/* Not the best placement, but it works just fine. */
				throw new CommunicatorException("Could not connect to " + receiverAddress);
				
			} catch (IOException e) {
				
			}

			try {
				socket.close();
			} catch (IOException e) {
				
			}
		}
	}

	private void setAddressMapper(AddressMapper addressMapper) {
		this.addressMapper = addressMapper;
	}
	
	public void destroy() {
		setAddressMapper(null);
	}
}
