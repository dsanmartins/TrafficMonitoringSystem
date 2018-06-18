package se.lnu.trafficmonitoring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class MergeRightResponse implements Serializable {
	private static final long serialVersionUID = 786571673746335240L;
	
	private boolean accepted;
	private Collection<Integer> cameras;
	
	public MergeRightResponse(boolean accepted) {
		setAccepted(accepted);
	}
	
	public MergeRightResponse(boolean accepted, Collection<Integer> cameras) {
		setAccepted(accepted);
		setCameras(cameras);
	}
	
	public boolean isAccepted() {
		return accepted;
	}
	
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
	public Collection<Integer> getCameras() {
		return cameras;
	}
	
	public void setCameras(Collection<Integer> cameras) {
		if (cameras instanceof Serializable) {
			this.cameras = cameras;
		} else {
			this.cameras = new ArrayList<Integer>(cameras);
		}
	}
}
