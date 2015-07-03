package de.htwk_leipzig.bis.msg_response;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import org.apache.commons.lang3.RandomStringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

/**
 * The {@code  MessageConsumer} class represents a AMQP Consumer that allows a
 * customizable response for a consumed message.
 * 
 * <p>
 * It is a specialization of {@code AMQPSubscriber}.
 *
 */
public class MessageConsumer extends AMQPSubscriber {

    /**
     * The name of the used exchange.
     */
    public static final String EXCHANGE_NAME = "messaging";

    /**
     * Member variable to hold the interval between two Consume&Response events.
     */
    private final int mConsumeInterval;

    /**
     * Member variable to hold the max. count of prefetched messages or 0 for
     * infinity.
     */
    private final int mPrefetchCount;

    /**
     * Member variable to hold the max. amount of prefetched messages in octets
     * (bytes) or 0 for infinity.
     */
    private final int mPrefetchAmount;

    /**
     * Member variable to indicate whether the communication is persistent or
     * not.
     */
    private final boolean mUsePersistentQueue;

    /**
     * Member variable to hold the used response action.
     */
    private final ResponseAction mResponse;

    /**
     * Creates a new instance of {@code MessageConsumer} with the given uri,
     * given consume interval, the given response action and allow to specify
     * whether the communication is persistent or not.
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param consumeIntervalInMiliSec
     *            Interval between two Consume&Response events.
     * @param prefetchCount
     *            Max. count of prefetched messages or 0 for infinity.
     * @param prefetchAmount
     *            Max. amount of prefetched messages in octets (bytes) or 0 for
     *            infinity.
     * @param usePersistentQueue
     *            True, if the communication should be persistent.
     * @param responseAction
     *            The response action to use for every consumed message.
     */
    public MessageConsumer(final URI uri, final int consumeIntervalInMiliSec, final int prefetchCount,
	    final int prefetchAmount, final boolean usePersistentQueue, final ResponseAction responseAction) {
	super(uri);
	checkArgument(0 <= consumeIntervalInMiliSec, "Interval must be greater or equal 0");
	mConsumeInterval = consumeIntervalInMiliSec;
	mPrefetchCount = prefetchCount;
	mPrefetchAmount = prefetchAmount;
	mUsePersistentQueue = usePersistentQueue;
	mResponse = checkNotNull(responseAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
     */
    @Override
    protected void doSubscriberActions(Connection connection, Channel channel) throws Exception {
	String queueName = RandomStringUtils.randomAlphabetic(15);
	channel.exchangeDeclare(EXCHANGE_NAME, "fanout", mUsePersistentQueue, false, null);
	channel.queueDeclare(queueName, mUsePersistentQueue, false, true, null);
	channel.queueBind(queueName, EXCHANGE_NAME, "");
	channel.basicQos(mPrefetchAmount, mPrefetchCount, false);
	final QueueingConsumer consumer = new QueueingConsumer(channel);
	channel.basicConsume(queueName, false, consumer);

	System.out.println("Consumer Online");
	while (true) {
	    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	    mResponse.response(channel, delivery);
	    delivery = null;
	    Thread.sleep(mConsumeInterval);
	}
    }
}
