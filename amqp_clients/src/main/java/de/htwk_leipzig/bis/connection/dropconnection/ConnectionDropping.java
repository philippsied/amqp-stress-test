package de.htwk_leipzig.bis.connections.dropconnection;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.SocketConfigurator;

import de.htwk_leipzig.bis.util.ToolBox;

public class ConnectionHanging implements Runnable {

	private final URI mUri;
	private final int mCreationInterval;
	private Socket mCurrentSocket;
		
	public ConnectionHanging(final URI uri, final int creationInterval) {
		mUri = uri;
		mCreationInterval = creationInterval;
	}

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
				mCurrentSocket.close();	
				Thread.sleep(mCreationInterval);
			} while (true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
