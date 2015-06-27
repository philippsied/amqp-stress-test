package de.htwk_leipzig.bis.channel;

import java.io.IOException;
import java.util.Random;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Create a Producer who uses an existing connection to create its own channel.
 */
public class ChannelProducer extends ChannelBasic implements Runnable {

	/**
	 * Holds the given message size.
	 */
	final int mMessageSizeInBytes;

	/**
	 * Creates a new instance of {@code ChannelProducer} with the existing
	 * connection.
	 * 
	 * @param connection
	 *            the existing connection.
	 * @param messageSize
	 *            the given message size.
	 * @throws IOException
	 */
	public ChannelProducer(Connection connection, int messageSize)
			throws IOException {
		super(connection);
		this.mConnection = connection;
		this.mMessageSizeInBytes = messageSize;
	}

	/**
	 * Create a new Channel over the the existing connection. Furthermore, the
	 * channel will be connected to the queue and creates a producer. After
	 * this, the messages are sent permanently.
	 */
	@Override
	public void run() {

		Channel channel = null;
		byte[] message = generateMessage();

		try {
			channel = mConnection.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			try {
				send(channel, message);
			} finally {
				channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends messages over the given channel and message.
	 * 
	 * @param channel
	 *            the given channel to send from.
	 * @param message
	 *            the messages to send.
	 */
	private void send(Channel channel, byte[] message) {
		while (true) {
			try {
				channel.basicPublish("", QUEUE_NAME, null, message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a random messages. The length depends on the variable
	 * {@code mMessageSizeInBytes}
	 * 
	 * @return the generated messages
	 */
	private byte[] generateMessage() {
		byte[] message;
		if (mMessageSizeInBytes > 0) {
			message = new byte[mMessageSizeInBytes];
			new Random().nextBytes(message);
		} else {
			message = null;
		}
		return message;
	}

}
