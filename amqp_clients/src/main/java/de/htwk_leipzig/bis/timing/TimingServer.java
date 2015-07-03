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
public class TimingServer extends AMQPSubscriber {
    private final static String QUEUE_NAME_REQUEST = "request";
    private final static String QUEUE_NAME_RESPONSE = "response";
    private final static String QUEUE_NAME_SYNC = "timesync";

    private long mTimeOffset;

    public TimingServer(final URI uri) {
	super(uri);
	mTimeOffset = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions(com.rabbitmq.
     * client.Connection, com.rabbitmq.client.Channel)
     */
    @Override
    protected void doSubscriberActions(Connection connection, Channel channel) throws Exception {
	// adjustOffset(channel);

	channel.queueDeclare(QUEUE_NAME_REQUEST, false, false, true, null);
	channel.queueDeclare(QUEUE_NAME_RESPONSE, false, false, true, null);
	channel.queueDeclare(QUEUE_NAME_SYNC, false, false, true, null);

	final QueueingConsumer consumer = new QueueingConsumer(channel);
	channel.basicConsume(QUEUE_NAME_REQUEST, true, consumer);

	System.out.println(" [*] Waiting for messages. Press CTRL+C to exit");
	while (true) {
	    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	    long now = System.currentTimeMillis();
	    channel.basicPublish("", QUEUE_NAME_RESPONSE, null, null);
	    long clientSend = ByteBuffer.wrap(delivery.getBody()).getLong();
	    System.out.println(" [x] Receive request: " + ((now - clientSend) + mTimeOffset) + "ms (latency)");
	}

    }

    @SuppressWarnings("unused")
    private void adjustOffset(Channel channel) throws Exception {
	mTimeOffset = ToolBox.calculateNTPOffset();
	System.out.println("NTP offset: " + mTimeOffset);
	final QueueingConsumer syncConsumer = new QueueingConsumer(channel);
	channel.basicConsume(QUEUE_NAME_SYNC, true, syncConsumer);
	long clientOffset = ByteBuffer.wrap(syncConsumer.nextDelivery().getBody()).getLong();
	mTimeOffset -= clientOffset;
	System.out.println("adjusted NTP offset: " + mTimeOffset);
    }
}
