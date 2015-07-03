package de.htwk_leipzig.bis.connection.handshake;

/**
 * Represents an action that is performed between the steps of the AMQ Connection handeshake for
 * establishing or close the connection.
 * <p>
 * This interface specify the {@code doAction()} method for performing the
 * concrete action.
 */
public interface HandshakeAction {

	/**
	 * Perform the concrete action of the implementer of {@code HandshakeAction}.
	 */
	public void doAction();
}
