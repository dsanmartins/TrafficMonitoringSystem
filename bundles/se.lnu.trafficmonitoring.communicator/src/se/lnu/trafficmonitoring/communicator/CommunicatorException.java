package se.lnu.trafficmonitoring.communicator;

public class CommunicatorException extends RuntimeException {

	private static final long serialVersionUID = 2871588856955854887L;

	public CommunicatorException() {

	}

	public CommunicatorException(String message) {
		super(message);
	}

	public CommunicatorException(Throwable cause) {
		super(cause);
	}

	public CommunicatorException(String message, Throwable cause) {
		super(message, cause);
	}
}
