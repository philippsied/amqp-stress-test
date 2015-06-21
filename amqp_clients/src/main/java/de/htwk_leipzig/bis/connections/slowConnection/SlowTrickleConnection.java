package de.htwk_leipzig.bis.connections.slowConnection;

import java.net.URI;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class SlowTrickleConnection implements Runnable {
	private final URI mUri;
	private final long mDelay;
	private final HandshakeAction mAction;

	public SlowTrickleConnection(final URI uri, final long delay, final HandshakeAction handshakeAction) {
		mUri = uri;
		mDelay = delay;
		mAction = handshakeAction;
	}

	@Override
	public void run() {
		try {
			do {
				final ConnectionFactory factory = new SlowConnectionFactory(mAction);
				factory.setUri(mUri);
				factory.setShutdownTimeout(Integer.MAX_VALUE);
				factory.setConnectionTimeout(Integer.MAX_VALUE);
				final Connection connection = factory.newConnection();
				connection.close();
				Thread.sleep(mDelay);
			} while (true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
