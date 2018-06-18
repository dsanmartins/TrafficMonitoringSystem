package se.lnu.trafficmonitoring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class MergeLeftRequest implements Serializable {
	private static final long serialVersionUID = 7577656235274106233L;
	
	private int sourcePid;
	private Collection<Integer> cameras;
	
	public MergeLeftRequest(int sourcePid, Collection<Integer> cameras) {
		setSourcePid(sourcePid);
		setCameras(cameras);
	}
	
	public int getSourcePid() {
		return sourcePid;
	}
	
	public void setSourcePid(int sourcePid) {
		this.sourcePid = sourcePid;
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
