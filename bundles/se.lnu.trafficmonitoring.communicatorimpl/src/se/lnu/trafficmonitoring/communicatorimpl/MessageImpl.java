package se.lnu.trafficmonitoring.communicatorimpl;

import se.lnu.trafficmonitoring.communicator.Message;

/**
 * The default implementation of Message.
 */
public class MessageImpl implements Message {
	private static final long serialVersionUID = -8682556343816446710L;
	
	private int bundleId;
	private int receiverPid;
	private int senderPid;
	private String type;
	private Object payload;
	
	public MessageImpl(int receiverPid, int bundleId, String type) {
		setReceiverPid(receiverPid);
		setBundleId(bundleId);
		setType(type);
	}

	@Override
	public int getBundleId() {
		return bundleId;
	}
	
	@Override
	public void setBundleId(int bundleId) {
		this.bundleId = bundleId;
	}
	
	@Override
	public int getReceiverPid() {
		return receiverPid;
	}
	
	@Override
	public void setReceiverPid(int receiverPid) {
		this.receiverPid = receiverPid;
	}
	
	@Override
	public int getSenderPid() {
		return senderPid;
	}
	
	@Override
	public void setSenderPid(int senderPid) {
		this.senderPid = senderPid;
	}
	
	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public void setPayload(Object payload) {
		this.payload = payload;
	}
}
