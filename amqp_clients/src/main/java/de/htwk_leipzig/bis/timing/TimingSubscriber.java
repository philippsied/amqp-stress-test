package de.htwk_leipzig.bis.timing;

import java.io.IOException;
import java.net.URI;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.htwk_leipzig.bis.util.ToolBox;

public abstract class TimingSubscriber {
	protected final static String QUEUE_NAME_REQUEST = "request";
	protected final static String QUEUE_NAME_RESPONSE = "response";
	protected final static String QUEUE_NAME_SYNC = "timesync";

	protected Channel mChannel;

	public void start(URI uri) throws Exception {
		final ConnectionFactory factory = ToolBox.createConnectionFactory(uri);

		try {
			final Connection connection = factory.newConnection();
			try {
				mChannel = connection.createChannel();
				try {
					mChannel.queueDeclare(QUEUE_NAME_REQUEST, false, false, true, null);
					mChannel.queueDeclare(QUEUE_NAME_RESPONSE, false, false, true, null);
					mChannel.queueDeclare(QUEUE_NAME_SYNC, false, false, true, null);
					doSubscriberActions();
				} finally {
					mChannel.close();
				}
			} finally {
				connection.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void doSubscriberActions() throws Exception;

}
