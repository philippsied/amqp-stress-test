package de.htwk_leipzig.bis.connection.handshake;


public class HandshakeActionSleep implements HandshakeAction {
	private final long DELAY = 1000;

	public HandshakeActionSleep() {
	}

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
