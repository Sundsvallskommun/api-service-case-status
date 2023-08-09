package se.sundsvall.casestatus.integration.db.exception;

public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = -3947342790448525485L;

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
