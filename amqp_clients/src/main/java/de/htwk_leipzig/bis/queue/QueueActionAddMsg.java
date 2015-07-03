package de.htwk_leipzig.bis.dos.queue;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Random;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class QueueActionAddMsg implements QueueAction {
	private final int mMessageSizeInBytes;
	private final boolean mUsePersistentQueue;

	public QueueActionAddMsg(final int messageSizeInBytes, final boolean usePersistentQueue) {
		checkArgument(0 <= messageSizeInBytes, "Message size must be greater or equal 0");
		mMessageSizeInBytes = messageSizeInBytes;
		mUsePersistentQueue = usePersistentQueue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.htwk_leipzig.bis.dos.queue.QueueAction#doAction(com.rabbitmq.client
	 * .Channel, com.rabbitmq.client.Channel, java.lang.String, int,
	 * java.lang.String)
	 */
	@Override
	public void doAction(Channel producerChannel, Channel consumerChannel, String exchangeName, int queueNumber, String queueName) throws IOException {
		byte[] message = generateContent(mMessageSizeInBytes);

		if (mUsePersistentQueue) {
			producerChannel.basicPublish(exchangeName, "queueswap" + queueNumber, MessageProperties.PERSISTENT_BASIC, message);
		} else {
			producerChannel.basicPublish(exchangeName, "queueswap" + queueNumber, null, message);
		}
		consumerChannel.basicGet(queueName, false);
	}

	private byte[] generateContent(final int size) {
		byte[] message = null;
		if (size > 0) {
			message = new byte[size];
			new Random().nextBytes(message);
		}
		return message;
	}

}
