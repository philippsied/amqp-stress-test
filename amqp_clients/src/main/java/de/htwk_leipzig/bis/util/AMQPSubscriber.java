package de.htwk_leipzig.bis.util;

import java.net.URI;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * This class represents an abstract AMQP subscriber. It handles the connection
 * and channel for concrete subscribers. It also provide an interface via
 * {@code doSubscriberActions} to provide customizable actions on the channel.
 * 
 * <p>
 * {@code AMQPSubscriber} implements {@code Runnable} to allow running in a
 * {@code Thread}.
 *
 */
public abstract class AMQPSubscriber implements Runnable {

	/**
	 * Member variable to hold the used channel.
	 */
	protected Channel mChannel;

	/**
	 * Member variable to hold the RabbitMQ-Server URI.
	 */
	protected final URI mUri;

	/**
	 * Default constructor. Only used by specializations of
	 * {@code AMQPSubscriber}.
	 * 
	 * @param uri
	 *            the uri of the RabbitMQ-Server.
	 */
	protected AMQPSubscriber(final URI uri) {
		mUri = uri;
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
			final Connection connection = factory.newConnection();
			try {
				mChannel = connection.createChannel();
				try {
					doSubscriberActions();
				} finally {
					if (mChannel != null) {
						mChannel.close();
					}
				}
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Concrete subscriber must overide this method to do their specific
	 * actions.
	 * 
	 * @throws Exception
	 *             which may occur during the action.
	 */
	protected abstract void doSubscriberActions() throws Exception;

}
