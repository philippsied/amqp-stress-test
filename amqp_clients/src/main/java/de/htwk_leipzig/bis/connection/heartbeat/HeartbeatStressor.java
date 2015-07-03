package de.htwk_leipzig.bis.connections.heartbeat;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.htwk_leipzig.bis.util.ToolBox;

public class HeartbeatStressor implements Runnable {

	private static final int DEFAULT_HEARTBEAT_TIMOUT = 1;
	private static final int DEFAULT_CREATION_DELAY = 100;

	private final URI mUri;
	private final int mHeartbeatTimout;
	private final int mConnectionCount;
	private final int mCreationDelay;

	public HeartbeatStressor(final URI uri, final int connectionCount) {
		this(uri, DEFAULT_HEARTBEAT_TIMOUT, connectionCount, DEFAULT_CREATION_DELAY);
	}

	public HeartbeatStressor(final URI uri, final int heartbeatTimout, final int connectionCount, final int creationDelay) {
		checkArgument(connectionCount > 0);
		mUri = uri;
		mHeartbeatTimout = heartbeatTimout;
		mConnectionCount = connectionCount;
		mCreationDelay = creationDelay;
	}

	@Override
	public void run() {
		try {
			final ConnectionFactory factory = ToolBox.createConnectionFactory(mUri);
			factory.setUri(mUri);
			factory.setRequestedHeartbeat(mHeartbeatTimout);
			Connection connection = factory.newConnection();
			System.out.println("Heartbeat: " + connection.getHeartbeat());
			Thread.sleep(mCreationDelay);
			for(int i = 1; i < mConnectionCount; i++){
				connection = factory.newConnection();
				Thread.sleep(mCreationDelay);
			}
			System.out.println("Created " + mConnectionCount + " Connections.");
			System.out.println("Heartbeat will send every "+ ((double) mHeartbeatTimout/2.0) + "s");
			while (true){
				Thread.yield();
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
