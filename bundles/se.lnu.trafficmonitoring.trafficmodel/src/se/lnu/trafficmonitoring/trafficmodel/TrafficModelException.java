package se.lnu.trafficmonitoring.trafficmodel;

public class TrafficModelException extends RuntimeException {

	private static final long serialVersionUID = 9173547876535933988L;

	public TrafficModelException() {

	}

	public TrafficModelException(String message) {
		super(message);

	}

	public TrafficModelException(Throwable cause) {
		super(cause);

	}

	public TrafficModelException(String message, Throwable cause) {
		super(message, cause);

	}

}
