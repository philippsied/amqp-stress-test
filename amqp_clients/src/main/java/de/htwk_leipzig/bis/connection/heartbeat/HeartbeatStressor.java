package de.htwk_leipzig.bis.connection.heartbeat;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.htwk_leipzig.bis.util.ToolBox;

/**
 * This class represents a AMQ Client with multiple connections to the server
 * and allows a customizing of the used heartbeat.
 * <p>
 * It implements {@code Runnable} to allow running in a {@code Thread}.
 */
public class HeartbeatStressor implements Runnable {

    /**
     * The default heartbeat timeout value.
     */
    public static final int DEFAULT_HEARTBEAT_TIMOUT = 1;

    /**
     * The default time to wait after a connection is established.
     */
    public static final int DEFAULT_CREATION_DELAY = 100;

    /**
     * Member variable to hold the RabbitMQ-Server URI.
     */
    private final URI mUri;

    /**
     * Member variable to hold the timeout after a connection is treated as
     * closed. The server sends the first heartbeat after mHeartbeatTimout/2
     * seconds.
     */
    private final int mHeartbeatTimout;

    /**
     * Member variable to hold the count of parallel opened connections.
     */
    private final int mConnectionCount;

    /**
     * Member variable to hold the time to wait after a connection is
     * established.
     */
    private final int mCreationDelay;

    /**
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param connectionCount
     *            The count of parallel opened connections.
     */
    public HeartbeatStressor(final URI uri, final int connectionCount) {
	this(uri, DEFAULT_HEARTBEAT_TIMOUT, connectionCount, DEFAULT_CREATION_DELAY);
    }

    /**
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param heartbeatTimout
     *            The timeout after a connection is treated as closed. The
     *            server sends the first heartbeat after heartbeatTimout/2
     *            seconds.
     * @param connectionCount
     *            The count of parallel opened connections.
     */
    public HeartbeatStressor(final URI uri, final int heartbeatTimout, final int connectionCount) {
	this(uri, heartbeatTimout, connectionCount, DEFAULT_CREATION_DELAY);
    }

    /**
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param heartbeatTimout
     *            The timeout after a connection is treated as closed. The
     *            server sends the first heartbeat after heartbeatTimout/2
     *            seconds.
     * @param connectionCount
     *            The count of parallel opened connections.
     * @param creationDelay
     *            The time to wait after a connection is established.
     */
    public HeartbeatStressor(final URI uri, final int heartbeatTimout, final int connectionCount,
	    final int creationDelay) {
	checkArgument(connectionCount > 0);
	mUri = uri;
	mHeartbeatTimout = heartbeatTimout;
	mConnectionCount = connectionCount;
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
	    factory.setRequestedHeartbeat(mHeartbeatTimout);
	    Connection connection = factory.newConnection();
	    System.out.println("Heartbeat: " + connection.getHeartbeat());
	    Thread.sleep(mCreationDelay);
	    for (int i = 1; i < mConnectionCount; i++) {
		connection = factory.newConnection();
		Thread.sleep(mCreationDelay);
	    }
	    System.out.println("Created " + mConnectionCount + " Connections.");
	    System.out.println("Heartbeat will send every " + ((double) mHeartbeatTimout / 2.0) + "s");
	    while (true) {
		Thread.yield();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
