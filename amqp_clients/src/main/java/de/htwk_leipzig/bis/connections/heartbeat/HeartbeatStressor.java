package de.htwk_leipzig.bis.connections.heartbeat;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.SocketConfigurator;

import de.htwk_leipzig.bis.util.ToolBox;

public class HeartbeatStressor implements Runnable {

	private final URI mUri;
	private Socket newSock;

	public HeartbeatStressor(final URI uri) {
		mUri = uri;

	}

	@Override
	public void run() {
		try {
			do {
				final ConnectionFactory factory = ToolBox.createConnectionFactory(mUri);
				factory.setUri(mUri);
				factory.setRequestedHeartbeat(0);
				factory.setSocketConfigurator(new SocketConfigurator() {

					@Override
					public void configure(Socket socket) throws IOException {
						socket.setKeepAlive(false);
						socket.setSoLinger(true, Integer.MAX_VALUE);
						newSock = socket;

					}
				});
				final Connection connection = factory.newConnection();
				Channel channel = connection.createChannel();
				
				System.out.println("Hearthbeat: " + connection.getHeartbeat() + "s");
				System.out.println("Port: " + connection.getPort());
				System.out.println("SO_Timout: " + newSock.getSoTimeout());
				System.out.println("SO_Linger: " + newSock.getSoLinger());
				Thread.sleep(5000);
				System.out.println("Kill connection");
				newSock.close();
				Thread.sleep(5000);
			} while (true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
