package de.htwk_leipzig.bis.header;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

public class LargeHeaderProducer extends AMQPSubscriber {

	/**
	 * The queue-name for this test.
	 */
	private static final String QUEUE_NAME = "testq";

	/**
	 * Hold the used message size in bytes.
	 */
	private final int mMessageSizeInBytes;

	/**
	 * Holds the number of header entries. Reflects the items in the hash map.
	 */
	private final int mHeaderSize;

	/**
	 * The properties for each messages. Holds the hash map.
	 */
	private final BasicProperties mProb;

	/**
	 * Creates a new instance of {@code LargeHeaderProducer} with the given uri,
	 * message size and header size.
	 * 
	 * @param uri
	 *            the uri of the RabbitMQ-Server.
	 * @param messageSizeInBytes
	 *            the given message sizes
	 * @param headerSize
	 *            the given header sizes
	 */
	public LargeHeaderProducer(URI uri, int messageSizeInBytes, int headerSize) {
		super(uri);
		this.mHeaderSize = headerSize;
		this.mProb = generateHeader();
		this.mMessageSizeInBytes = messageSizeInBytes;
	}

	/**
	 * Generate the Header for the messages. It used the given header size to
	 * create the desired header. Each Entry a size of 44 Byte.
	 * 
	 * @return the header includes in a property-object.
	 */
	private BasicProperties generateHeader() {
		Map<String, Object> headers = new HashMap<String, Object>();

		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

		headers.put("x-match", "any"); // any or all

		for (int i = 0; i < mHeaderSize; i++) {
			headers.put("header" + i, UUID.randomUUID().toString());
		}

		builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN
				.getDeliveryMode());
		builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
		builder.headers(headers);

		return builder.build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
	 */
	@Override
	protected void doSubscriberActions() throws Exception {

		mChannel.queueDeclare(QUEUE_NAME, false, false, false, null);

		byte[] message;

		if (mMessageSizeInBytes > 0) {
			message = new byte[mMessageSizeInBytes];
			new Random().nextBytes(message);
		} else {
			message = null;
		}

		while (true) {
			mChannel.basicPublish("", QUEUE_NAME, mProb, message);
		}
	}

}
