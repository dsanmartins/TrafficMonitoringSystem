package se.lnu.trafficmonitoring.data;

import java.io.Serializable;
import java.net.SocketAddress;

/**
 * Used as a message payload for notifying Agents about other processes addresses.
 */
public class PidToAddress implements Serializable {
	private static final long serialVersionUID = -1275340672348807338L;
	
	private int pid;
	private SocketAddress address;
	
	public PidToAddress(int pid, SocketAddress address) {
		setPid(pid);
		setAddress(address);
	}
	
	public int getPid() {
		return pid;
	}
	
	public void setPid(int pid) {
		this.pid = pid;
	}
	
	public SocketAddress getAddress() {
		return address;
	}
	
	public void setAddress(SocketAddress address) {
		if (address == null) {
			throw new NullPointerException("Parameter \"address\" must not be null.");
		}
		
		this.address = address;
	}
}
