package de.htwk_leipzig.bis.dos.queue;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;

import org.apache.commons.lang3.RandomStringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.htwk_leipzig.bis.util.ToolBox;

public class QueueSwapper implements Runnable {
	private static final String EXCHANGE_NAME = "queueswapping";
	private final URI mUri;
	private final int mSwappingInterval;
	private final boolean mUsePersistentQueue;
	private final int mPendingQueueCount;
	private final QueueAction mAction;

	public QueueSwapper(final URI uri, final int swappingIntervalInMiliSec, final boolean usePersistentQueue, final int pendingQueueCount,
			final QueueAction action) {
		checkArgument(0 <= swappingIntervalInMiliSec, "Swapping interval must be greater or equal 0");
		checkArgument(0 <= pendingQueueCount, "pendingQueueCount must be greater or equal 0");
		mUri = uri;
		mSwappingInterval = swappingIntervalInMiliSec;
		mUsePersistentQueue = usePersistentQueue;
		mPendingQueueCount = pendingQueueCount;
		mAction = action;
	}

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
							queueNames[currentQueueCount] = setupQueue(consumerChannel, EXCHANGE_NAME, "queueswap" + currentQueueCount);
							mAction.doAction(producerChannel, consumerChannel, EXCHANGE_NAME, currentQueueCount, queueNames[currentQueueCount]);
							currentQueueCount++;
							Thread.sleep(mSwappingInterval);
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

	private String setupQueue(Channel channel, String exchangeName, String routingKey) throws Exception {
		String queueName = RandomStringUtils.randomAlphabetic(15);
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, routingKey);
		return queueName;
	}
}
