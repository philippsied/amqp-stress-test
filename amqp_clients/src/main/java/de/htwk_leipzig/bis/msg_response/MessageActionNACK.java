package de.htwk_leipzig.bis.msg_response;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class MessageActionNACK implements ResponseAction {

	private final int mPendingMessagesCount;
	private int mMessageCounter;

	public MessageActionNACK(final int pendingMessagesCount) {
		checkArgument(0 <= pendingMessagesCount, "Message count must be greater 0");
		mPendingMessagesCount = pendingMessagesCount;
		mMessageCounter = 0;
	}

	@Override
	public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException {
		if (mMessageCounter <= mPendingMessagesCount) {
			mMessageCounter++;
		} else {
			mMessageCounter = 0;
			channel.basicNack(delivery.getEnvelope().getDeliveryTag(), true, true);
		}
	}
	@Override
	public String toString() {
		return "NACK (PendingCount: "+ mPendingMessagesCount+ ")";
	}
}
