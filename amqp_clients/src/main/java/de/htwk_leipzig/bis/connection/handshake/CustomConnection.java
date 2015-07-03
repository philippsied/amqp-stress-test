package de.htwk_leipzig.bis.connection.handshake;

import java.net.URI;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * This class represents a connection that allow a customizable action during
 * the handshake for establishing or close an AMQ Connection slow down the
 * connection handshake.
 * 
 * <p>
 * {@code SlowTrickleConnection} implements {@code Runnable} to allow running in
 * a {@code Thread}.
 *
 */
public class CustomConnection implements Runnable {

	/**
	 * Member variable to hold the RabbitMQ-Server URI.
	 */
	private final URI mUri;

	/**
	 * Member variable to hold the delay between two created connections with
	 * full Establish&Close run
	 */
	private final long mDelay;

	/**
	 * Member variable to hold the used handshake action.
	 */
	private final HandshakeAction mAction;

	/**
	 * Creates an instance of {@code CustomConnection} with the given uri, the
	 * given delay between two Establish&Close runs and the given handshake
	 * action.
	 * 
	 * @param uri
	 *            the uri of the RabbitMQ-Server.
	 * @param delay
	 *            the delay between two created connections with full
	 *            Establish&Close run
	 * @param handshakeAction
	 *            action to use between the handshake steps for every
	 *            Establish&Close run.
	 */
	public CustomConnection(final URI uri, final long delay, final HandshakeAction handshakeAction) {
		mUri = uri;
		mDelay = delay;
		mAction = handshakeAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			do {
				final ConnectionFactory factory = new CustomConnectionFactory(mAction);
				factory.setUri(mUri);
//				factory.setShutdownTimeout(Integer.MAX_VALUE);
//				factory.setConnectionTimeout(Integer.MAX_VALUE);
				final Connection connection = factory.newConnection();
				connection.close();
				Thread.sleep(mDelay);
			} while (true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
