package de.htwk_leipzig.bis.msg_response;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * This class represents a response on a given message. It sends a NACK to the
 * server to indicate, that all unacknowledged messages are have to be
 * redelivered.
 */
public class MessageActionNACK implements ResponseAction {

    /**
     * Member variable to hold the threshold of unacked messages.
     */
    private final int mPendingMessagesCount;

    /**
     * Member variable to hold the count of messages.
     */
    private int mMessageCounter;

    /**
     * Creates an instance of {@code MessageActionNACK}.
     * 
     * @param pendingMessagesCount
     *            The threshold of unacknowledged messages.
     */
    public MessageActionNACK(final int pendingMessagesCount) {
	checkArgument(0 <= pendingMessagesCount, "Message count must be greater 0");
	mPendingMessagesCount = pendingMessagesCount;
	mMessageCounter = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.htwk_leipzig.bis.msg_response.ResponseAction#response(com.rabbitmq.
     * client.Channel, com.rabbitmq.client.QueueingConsumer.Delivery)
     */
    @Override
    public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException {
	if (mMessageCounter <= mPendingMessagesCount) {
	    mMessageCounter++;
	} else {
	    mMessageCounter = 0;
	    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), true, true);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "NACK (PendingCount: " + mPendingMessagesCount + ")";
    }
}
