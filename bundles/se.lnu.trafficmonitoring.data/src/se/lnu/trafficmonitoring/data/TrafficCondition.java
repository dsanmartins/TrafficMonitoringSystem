package se.lnu.trafficmonitoring.data;

import java.io.Serializable;

public class TrafficCondition implements Serializable {
	private static final long serialVersionUID = 1633172154118811L;
	
	private int traffic;
	private boolean congestion;
	
	public TrafficCondition(int traffic, boolean congestion) {
		setTraffic(traffic);
		setCongestion(congestion);
	}
	
	public int getTraffic() {
		return traffic;
	}
	
	public void setTraffic(int traffic) {
		this.traffic = traffic;
	}
	
	public boolean getCongestion() {
		return congestion;
	}
	
	public void setCongestion(boolean congestion) {
		this.congestion = congestion;
	}
}
