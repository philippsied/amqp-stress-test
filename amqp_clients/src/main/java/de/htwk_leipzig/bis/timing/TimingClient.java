package de.htwk_leipzig.bis.timing;

import java.net.URI;
import java.nio.ByteBuffer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import de.htwk_leipzig.bis.util.AMQPSubscriber;
import de.htwk_leipzig.bis.util.ToolBox;

/**
 * 
 *
 *
 */
public class TimingClient extends AMQPSubscriber {
	private final static String QUEUE_NAME_REQUEST = "request";
	private final static String QUEUE_NAME_RESPONSE = "response";
	private final static String QUEUE_NAME_SYNC = "timesync";

	private final int mPingDelay;

	public TimingClient(final URI uri, final int pingDelay) {
		super(uri);
		mPingDelay = pingDelay;
	}

	@Override
	protected void doSubscriberActions(Connection connection, Channel channel) throws Exception {
		// adjustOffset(channel);

		channel.queueDeclare(QUEUE_NAME_REQUEST, false, false, true, null);
		channel.queueDeclare(QUEUE_NAME_RESPONSE, false, false, true, null);
		channel.queueDeclare(QUEUE_NAME_SYNC, false, false, true, null);

		final QueueingConsumer consumer = new QueueingConsumer(channel);

		channel.basicConsume(QUEUE_NAME_RESPONSE, true, consumer);

		System.out.println(" [*] Send echo. Press CTRL+C to exit");
		while (true) {
			final long now = System.currentTimeMillis();
			channel.basicPublish("", QUEUE_NAME_REQUEST, null, ByteBuffer.allocate(Long.BYTES).putLong(now).array());
			consumer.nextDelivery();
			final long after = System.currentTimeMillis();

			System.out.println(" [x] Receive reply: " + (after - now) + "ms (RTT)");
			Thread.sleep(mPingDelay);
		}

	}

	@SuppressWarnings("unused")
	private void adjustOffset(Channel channel) throws Exception {
		System.out.println("Use ping delay: " + mPingDelay + "ms");
		final long timeOffset = ToolBox.calculateNTPOffset();
		System.out.println("NTP offset: " + timeOffset);
		channel.basicPublish("", QUEUE_NAME_SYNC, null, ByteBuffer.allocate(Long.BYTES).putLong(timeOffset).array());
	}
}
