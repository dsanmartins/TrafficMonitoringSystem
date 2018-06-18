package se.lnu.trafficmonitoring.communicator;

import java.io.Serializable;

public interface Message extends Serializable {
	public int getBundleId();
	public void setBundleId(int bundleId);
	
	public int getReceiverPid();
	public void setReceiverPid(int receiverPid);
	
	public int getSenderPid();
	public void setSenderPid(int senderPid);
	
	public String getType();
	public void setType(String type);
	
	public Object getPayload();
	public void setPayload(Object payload);
}
