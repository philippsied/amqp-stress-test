package de.htwk_leipzig.bis.connections.slowConnection;


public class HandshakeActionSleep implements HandshakeAction {
	private final long mDelay;

	public HandshakeActionSleep(final long delay) {
		mDelay = delay;
	}

	@Override
	public void doAction() {
		/*
		 * Add delay
		 */
		try {
			Thread.sleep(mDelay);
			System.out.println("Sleep for "+ mDelay + "ms");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
