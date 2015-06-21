package de.htwk_leipzig.bis.dos.msg_response;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import org.apache.commons.lang3.RandomStringUtils;

import com.rabbitmq.client.QueueingConsumer;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

/**
 * The MessageConsumer class represents a AMQP Consumer that allows a
 * customizable response for a consumed message.
 * 
 * 
 * It is a specialisation of {@code AMQPSubscriber}
 *
 */
public class MessageConsumer extends AMQPSubscriber {

	/**
	 * The name of the used exchange.
	 */
	public static final String EXCHANGE_NAME = "messaging";

	/**
	 * Count of prefetched messages. 0 means infinity.
	 */
	public static final int DEFAULT_PREFETCH_COUNT = 0;

	/**
	 * Amount of prefetched messages in octets. 0 means infinity octets.
	 */
	public static final int DEFAULT_PREFETCH_AMOUNT = 0;

	/**
	 * Member variable to hold the interval between two Consume&Response events.
	 */
	private final int mConsumeInterval;

	/**
	 * Member variable to indicate whether the communication is persistent or not.
	 */
	private final boolean mUsePersistentQueue;

	/**
	 * Member variable to hold the used response action.
	 */
	private final ResponseAction mResponse;

	/**
	 * Main Constructor. Used to create an instance of {@code MessageConsumer}
	 * 
	 * @param uri
	 *            of the RabbitMQ-Server
	 * @param consumeIntervalInMiliSec
	 * 			 between two Consume&Response events
	 * @param usePersistentQueue
	 *            Flag to indicate that the communication is persistent
	 * @param action
	 *            the response action to use for every consumed message
	 */
	public MessageConsumer(final URI uri, final int consumeIntervalInMiliSec, final boolean usePersistentQueue, final ResponseAction action) {
		super(uri);
		checkArgument(0 <= consumeIntervalInMiliSec,"Interval must be greater or equal 0");
		mConsumeInterval = consumeIntervalInMiliSec;
		mUsePersistentQueue = usePersistentQueue;
		mResponse = checkNotNull(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
	 */
	@Override
	protected void doSubscriberActions() throws Exception {
		String queueName = RandomStringUtils.randomAlphabetic(15);
		mChannel.exchangeDeclare(EXCHANGE_NAME, "fanout", mUsePersistentQueue,false, null);
		mChannel.queueDeclare(queueName, mUsePersistentQueue, false, true, null);
		mChannel.queueBind(queueName, EXCHANGE_NAME, "");
		mChannel.basicQos(DEFAULT_PREFETCH_AMOUNT, DEFAULT_PREFETCH_COUNT,false);
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
