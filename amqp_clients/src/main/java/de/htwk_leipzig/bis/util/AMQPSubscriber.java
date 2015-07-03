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
 * It implements {@code Runnable} to allow running in a {@code Thread}.
 *
 */
public abstract class AMQPSubscriber implements Runnable {

    /**
     * Member variable to hold the RabbitMQ-Server URI.
     */
    protected final URI mUri;

    /**
     * Default constructor. Only used by specializations of
     * {@code AMQPSubscriber}.
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
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
		Channel channel = connection.createChannel();
		try {
		    doSubscriberActions(connection, channel);
		} finally {
		    if (channel != null) {
			channel.close();
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
     * @param channel
     *            the channel to use.
     * @throws Exception
     *             which may occur during the action.
     */
    protected abstract void doSubscriberActions(Connection connection, Channel channel) throws Exception;

}
