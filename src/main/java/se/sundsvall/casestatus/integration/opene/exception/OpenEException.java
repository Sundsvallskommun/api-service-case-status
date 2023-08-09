package se.sundsvall.casestatus.integration.opene.exception;

public class OpenEException extends RuntimeException {

	private static final long serialVersionUID = 5232504687231301194L;

	public OpenEException(String message) {
		super(message);
	}

	public OpenEException(String message, Throwable cause) {
		super(message, cause);
	}
}
