package se.lnu.trafficmonitoring.communicator;

import java.net.SocketAddress;

public interface AddressMapper {
	public void setOwnPid(int pid);
	public int getOwnPid();
	
	public void setOwnAddress(SocketAddress address);
	public SocketAddress getOwnAddress();
	
	public void setAddress(int pid, SocketAddress address);
	public SocketAddress getAddress(int pid);
	public void removeAddress(int pid);
	public boolean containsAddress(int pid);
}
