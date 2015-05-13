package de.htwk_leipzig.bis.timing;

import java.nio.ByteBuffer;

import com.rabbitmq.client.QueueingConsumer;

import de.htwk_leipzig.bis.util.ToolBox;

/**
 * 
 *
 *
 */
public class TimingClient extends TimingSubscriber {
	private final int mPingDelay;

	public TimingClient(int pingDelay) {
		mPingDelay = pingDelay;
	}

	@Override
	protected void doSubscriberActions() throws Exception {
		// adjustOffset();

		final QueueingConsumer consumer = new QueueingConsumer(mChannel);

		mChannel.basicConsume(QUEUE_NAME_RESPONSE, true, consumer);

		System.out.println(" [*] Send echo. Press CTRL+C to exit");
		while (true) {
			final long now = System.currentTimeMillis();
			mChannel.basicPublish("", QUEUE_NAME_REQUEST, null, ByteBuffer.allocate(Long.BYTES).putLong(now).array());
			consumer.nextDelivery();
			final long after = System.currentTimeMillis();

			System.out.println(" [x] Receive reply: " + (after - now) + "ms (RTT)");
			Thread.sleep(mPingDelay * 1000);
		}

	}

	@SuppressWarnings("unused")
	private void adjustOffset() throws Exception {
		System.out.println("Use ping delay: " + mPingDelay + "s");
		final long timeOffset = ToolBox.calculateNTPOffset();
		System.out.println("NTP offset: " + timeOffset);
		mChannel.basicPublish("", QUEUE_NAME_SYNC, null, ByteBuffer.allocate(Long.BYTES).putLong(timeOffset).array());
	}
}
