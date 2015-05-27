package de.htwk_leipzig.bis.util;

import java.net.URI;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class AMQPSubscriber implements Runnable {
	protected Channel mChannel;
	protected final URI mUri;

	public AMQPSubscriber(final URI mUri) {
		this.mUri = mUri;
	}

	@Override
	public void run() {
		try {
			final ConnectionFactory factory = ToolBox.createConnectionFactory(mUri);
			final Connection connection = factory.newConnection();
			try {
				mChannel = connection.createChannel();
				try {
					doSubscriberActions();
				} finally {
					mChannel.close();
				}
			} finally {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void doSubscriberActions() throws Exception;

}
