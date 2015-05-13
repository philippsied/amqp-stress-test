package de.htwk_leipzig.bis.timing;

import java.nio.ByteBuffer;

import com.rabbitmq.client.QueueingConsumer;

import de.htwk_leipzig.bis.util.ToolBox;

/**
 * 
 * 
 *
 */
public class TimingServer extends TimingSubscriber {
	private long mTimeOffset;

	public TimingServer() {
		mTimeOffset = 0;
	}

	@Override
	protected void doSubscriberActions() throws Exception {
		// adjustOffset();

		final QueueingConsumer consumer = new QueueingConsumer(mChannel);
		mChannel.basicConsume(QUEUE_NAME_REQUEST, true, consumer);

		System.out.println(" [*] Waiting for messages. Press CTRL+C to exit");
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			long now = System.currentTimeMillis();
			mChannel.basicPublish("", QUEUE_NAME_RESPONSE, null, null);
			long clientSend = ByteBuffer.wrap(delivery.getBody()).getLong();
			System.out.println(" [x] Receive request: " + ((now - clientSend) + mTimeOffset) + "ms (latency)");
		}

	}

	@SuppressWarnings("unused")
	private void adjustOffset() throws Exception {
		mTimeOffset = ToolBox.calculateNTPOffset();
		System.out.println("NTP offset: " + mTimeOffset);
		final QueueingConsumer syncConsumer = new QueueingConsumer(mChannel);
		mChannel.basicConsume(QUEUE_NAME_SYNC, true, syncConsumer);
		long clientOffset = ByteBuffer.wrap(syncConsumer.nextDelivery().getBody()).getLong();
		mTimeOffset -= clientOffset;
		System.out.println("adjusted NTP offset: " + mTimeOffset);
	}
}
