package de.htwk_leipzig.bis.channel;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Create a Consumer who uses an existing connection to create its own channel.
 */
public class ChannelConsumer extends ChannelBasic implements Runnable {

	/**
	 * Creates a new instance of {@code ChannelConsumer} with the existing
	 * connection.
	 * 
	 * @param connection
	 *            the existing connection.
	 * @throws IOException
	 */
	public ChannelConsumer(Connection connection) throws IOException {
		super(connection);
	}

	/**
	 * Create a new Channel over the the existing connection. Furthermore, the
	 * channel will be connected to the queue and creates a consumer.
	 */
	@Override
	public void run() {

		QueueingConsumer consumer = null;

		try {
			Channel channel = mConnection.createChannel();
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, true, consumer);
			try {
				consume(consumer);
			} finally {
				channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Receives messages from a existing consumer - this function will never
	 * ends.
	 * 
	 * @param consumer
	 *            the existing consumer.
	 */
	private void consume(QueueingConsumer consumer) {
		while (true) {
			try {
				consumer.nextDelivery();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
