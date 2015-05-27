package de.htwk_leipzig.bis.dos.msg_response;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;

import org.apache.commons.lang3.RandomStringUtils;

import com.rabbitmq.client.QueueingConsumer;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

public class MessageConsumer extends AMQPSubscriber {
	public static final String EXCHANGE_NAME = "messaging";

	private final int mConsumeInterval;
	private final boolean mUsePersistentQueue;
	private final ResponseAction mResponse;

	/**
	 * 
	 * @param uri
	 * @param consumeIntervalInMiliSec
	 * @param usePersistentQueue
	 * @param action
	 */
	public MessageConsumer(final URI uri, final int consumeIntervalInMiliSec, final boolean usePersistentQueue, final ResponseAction action) {
		super(uri);
		checkArgument(0 <= consumeIntervalInMiliSec, "Interval must be greater or equal 0");
		mConsumeInterval = consumeIntervalInMiliSec;
		mUsePersistentQueue = usePersistentQueue;
		mResponse = action;

	}

	@Override
	protected void doSubscriberActions() throws Exception {
		String queueName = RandomStringUtils.randomAlphabetic(15);
		mChannel.queueDeclare(queueName, mUsePersistentQueue, false, true, null);
		mChannel.queueBind(queueName, EXCHANGE_NAME, "");
		final QueueingConsumer consumer = new QueueingConsumer(mChannel);
		mChannel.basicConsume(queueName, false, consumer);

		System.out.println("Consumer Online");

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			mResponse.response(mChannel, delivery);
			Thread.sleep(mConsumeInterval);
		}
	}
}
