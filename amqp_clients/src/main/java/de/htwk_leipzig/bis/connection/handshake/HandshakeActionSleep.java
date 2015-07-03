package de.htwk_leipzig.bis.connection.handshake;

/**
 * This class represents an sleep action during two steps of a AMQ Connection
 * handshake.
 * <p>
 * {@code HandshakeActionSleep} implements {@code HandshakeAction}
 */
public class HandshakeActionSleep implements HandshakeAction {

    /**
     * Default handshake delay, higher values are resulting in close exception
     */
    private final long DELAY = 1000;

    /**
     * Creates an instance of {@code HandshakeActionSleep}.
     */
    public HandshakeActionSleep() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.htwk_leipzig.bis.connection.handshake.HandshakeAction#doAction()
     */
    @Override
    public void doAction() {
	/*
	 * Add delay
	 */
	try {
	    Thread.sleep(DELAY);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }
}
