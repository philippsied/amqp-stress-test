/**
 * 
 */
package de.htwk_leipzig.bis.msg_response;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;

import org.apache.commons.lang3.RandomUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

/**
 * The MessageProducer class represents a AMQP Producer that repeatedly sends
 * messages by a fanout exchange to the consumer.
 * 
 * <p>
 * It is a specialisation of {@code AMQPSubscriber}.
 *
 */
public class MessageProducer extends AMQPSubscriber {

    /**
     * The name of the used exchange.
     */
    public static final String EXCHANGE_NAME = "messaging";

    /**
     * Member variable to hold the interval between two Generate&Publish events.
     */
    private final int mProduceInterval;

    /**
     * Member variable to hold the used size of message content - Measured in
     * bytes.
     */
    private final int mMessageSizeInBytes;

    /**
     * Member variable to indicate whether the communication is persistent or
     * not.
     */
    private final boolean mUsePersistentMessage;

    /**
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param produceIntervalInSec
     *            Interval between two produced messages, specified in
     *            milliseconds. The value "0" means as fast as possible.
     * @param usePersistentMessage
     *            True, if the communication should be persistent.
     * @param messageSizeInBytes
     *            The size of the message to be sent. The value "0" means no
     *            message payload.
     */
    public MessageProducer(final URI uri, final int produceIntervalInSec, final boolean usePersistentMessage,
	    final int messageSizeInBytes) {
	super(uri);
	checkArgument(0 <= produceIntervalInSec, "Interval must be greater or equal 0");
	checkArgument(0 <= messageSizeInBytes, "Message size must be greater or equal 0");
	mProduceInterval = produceIntervalInSec;
	mMessageSizeInBytes = messageSizeInBytes;
	mUsePersistentMessage = usePersistentMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
     */
    @Override
    protected void doSubscriberActions(Connection connection, Channel channel) throws Exception {
	channel.exchangeDeclare(EXCHANGE_NAME, "fanout", mUsePersistentMessage, false, null);

	System.out.println("Producer Online");

	byte[] message = null;
	if (mMessageSizeInBytes > 0) {
	    message = new byte[mMessageSizeInBytes];
	}

	while (true) {
	    if (mMessageSizeInBytes > 0) {
		for (int i = 0; i < message.length; i++) {
		    message[i] = RandomUtils.nextBytes(1)[0];
		}
	    }
	    if (mUsePersistentMessage) {
		channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_BASIC, message);
	    } else {
		channel.basicPublish(EXCHANGE_NAME, "", null, message);
	    }
	    Thread.sleep(mProduceInterval);
	}
    }
}
