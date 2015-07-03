package de.htwk_leipzig.bis.queue;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Random;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

/**
 * This class represents a action on a queue. It add a message of a given size
 * to it.
 *
 */
public class QueueActionAddMsg implements QueueAction {

    /**
     * Member variable to hold the used message size. Measured in bytes.
     */
    private final int mMessageSizeInBytes;

    /**
     * Member variable to indicate whether the queue is persistent or not.
     */
    private final boolean mUsePersistentQueue;

    /**
     * Creates an instance of {@code QueueActionAddMsg} with the given message
     * size and whether the queue is persistent or not.
     * 
     * @param messageSizeInBytes
     *            The message size, measured in bytes.
     * @param usePersistentQueue
     *            True, if the queue should be persistent.
     */
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
    public void doAction(Channel producerChannel, Channel consumerChannel, String exchangeName, int queueNumber,
	    String queueName) throws IOException {
	byte[] message = generateContent(mMessageSizeInBytes);

	if (mUsePersistentQueue) {
	    producerChannel.basicPublish(exchangeName, "queueswap" + queueNumber, MessageProperties.PERSISTENT_BASIC,
		    message);
	} else {
	    producerChannel.basicPublish(exchangeName, "queueswap" + queueNumber, null, message);
	}
	consumerChannel.basicGet(queueName, false);
    }

    /**
     * Private API - Generate a random byte array of given size.
     * 
     * @param size
     *            The size of the random array.
     * @return The random byte array
     */
    private byte[] generateContent(final int size) {
	byte[] message = null;
	if (size > 0) {
	    message = new byte[size];
	    new Random().nextBytes(message);
	}
	return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "QueueActionAddMsg [mMessageSizeInBytes=" + mMessageSizeInBytes + ", mUsePersistentQueue="
		+ mUsePersistentQueue + "]";
    }

}
