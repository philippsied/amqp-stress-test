package de.htwk_leipzig.bis.queue;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;

import org.apache.commons.lang3.RandomStringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.htwk_leipzig.bis.util.ToolBox;

/**
 * This class represents a AMQP Client that frequently creates and deletes
 * queues.
 * 
 * <p>
 * It implements {@code Runnable} to allow running in a {@code Thread}.
 *
 */
public class QueueAgitator implements Runnable {

    /**
     * The name of the used exchange.
     */
    public static final String EXCHANGE_NAME = "queueswapping";

    /**
     * Member variable to hold the RabbitMQ-Server URI.
     */
    private final URI mUri;

    /**
     * Member variable to hold the delay after a queue was created.
     */
    private final int mCreationDelay;

    /**
     * Member variable to indicate whether the communication is persistent or
     * not.
     */
    private final boolean mUsePersistentQueue;

    /**
     * Member variable to hold the threshold of undeleted queues.
     */
    private final int mPendingQueueCount;

    /**
     * Member variable to hold the used queue action.
     */
    private final QueueAction mAction;

    /**
     * Creates a new instance of {@code QueueAgitator} with the given uri, the
     * given delay after queue creation, the given threshold of undeleted queues
     * 
     * @param uri
     *            The uri of the RabbitMQ-Server.
     * @param creationDelayInMiliSec
     *            The delay after a queue was created.
     * @param usePersistentQueue
     *            True, if the communication should be persistent.
     * @param pendingQueueCount
     *            The threshold of undeleted queues.
     * @param action
     *            The action after a queue was created.
     */
    public QueueAgitator(final URI uri, final int creationDelayInMiliSec, final boolean usePersistentQueue,
	    final int pendingQueueCount, final QueueAction action) {
	checkArgument(0 <= creationDelayInMiliSec, "Swapping interval must be greater or equal 0");
	checkArgument(0 <= pendingQueueCount, "pendingQueueCount must be greater or equal 0");
	mUri = uri;
	mCreationDelay = creationDelayInMiliSec;
	mUsePersistentQueue = usePersistentQueue;
	mPendingQueueCount = pendingQueueCount;
	mAction = action;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
	try {
	    final ConnectionFactory factory = ToolBox.createConnectionFactory(mUri);
	    final Connection connectionProducer = factory.newConnection();
	    final Connection connectionConsumer = factory.newConnection();
	    try {
		Channel producerChannel = connectionProducer.createChannel();
		Channel consumerChannel = connectionConsumer.createChannel();
		try {
		    producerChannel.exchangeDelete(EXCHANGE_NAME);
		    producerChannel.exchangeDeclare(EXCHANGE_NAME, "direct", mUsePersistentQueue, false, null);
		    System.out.println("QueueSwapper Online");
		    while (true) {
			int currentQueueCount = 0;
			String[] queueNames = new String[mPendingQueueCount + 1];
			do {
			    queueNames[currentQueueCount] = setupQueue(consumerChannel, EXCHANGE_NAME,
				    "queueswap" + currentQueueCount);
			    mAction.doAction(producerChannel, consumerChannel, EXCHANGE_NAME, currentQueueCount,
				    queueNames[currentQueueCount]);
			    currentQueueCount++;
			    Thread.sleep(mCreationDelay);
			} while (currentQueueCount <= mPendingQueueCount);

			for (int i = 0; i < queueNames.length; i++) {
			    consumerChannel.queueDeleteNoWait(queueNames[i], false, false);
			}
		    }
		} finally {
		    producerChannel.exchangeDelete(EXCHANGE_NAME);
		    consumerChannel.close();
		    producerChannel.close();
		}
	    } finally {
		connectionConsumer.close();
		connectionProducer.close();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Private API - Setup a queue
     * 
     * @param channel
     * @param exchangeName
     * @param routingKey
     * @return
     * @throws Exception
     */
    private String setupQueue(Channel channel, String exchangeName, String routingKey) throws Exception {
	String queueName = RandomStringUtils.randomAlphabetic(15);
	channel.queueDeclare(queueName, mUsePersistentQueue, false, false, null);
	channel.queueBind(queueName, exchangeName, routingKey);
	return queueName;
    }
}
