package de.htwk_leipzig.bis.dos.queue;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import de.htwk_leipzig.bis.util.ToolBox;

public class QueueSwapper implements Runnable {
	private static final String EXCHANGE_NAME = "queueswapping";
	private final URI mUri;
	private final int mSwappingInterval;
	private final int mMessageSizeInBytes;
	private final boolean mUsePersistentQueue;
	private final int mPendingQueueCount;
	private Channel mProducerChannel;
	private Channel mConsumerChannel;

	public QueueSwapper(final URI uri, final int messageSizeInBytes, final int swappingIntervalInMiliSec, final boolean usePersistentQueue, final int pendingQueueCount) {
		checkArgument(0 <= swappingIntervalInMiliSec, "Swapping interval must be greater or equal 0");
		checkArgument(0 <= pendingQueueCount, "pendingQueueCount must be greater or equal 0");
		checkArgument(0 <= messageSizeInBytes, "Message size must be greater or equal 0");
		mUri = uri;
		mSwappingInterval = swappingIntervalInMiliSec;
		mMessageSizeInBytes = messageSizeInBytes;
		mUsePersistentQueue = usePersistentQueue;
		mPendingQueueCount = pendingQueueCount;
	}

	@Override
	public void run() {
		try {
			final ConnectionFactory factory = ToolBox.createConnectionFactory(mUri);
			final Connection connectionProducer = factory.newConnection();
			final Connection connectionConsumer = factory.newConnection();
			try {
				mProducerChannel = connectionProducer.createChannel();
				mConsumerChannel = connectionConsumer.createChannel();
				try {
					mProducerChannel.exchangeDelete(EXCHANGE_NAME);
					mProducerChannel.exchangeDeclare(EXCHANGE_NAME, "direct", mUsePersistentQueue, false, null);
					System.out.println("QueueSwapper Online");
					while (true) {
						int currentQueueCount = 0;
						String[] queueNames = new String[mPendingQueueCount + 1];
						do {
							queueNames[currentQueueCount] = setupQueue(mConsumerChannel, EXCHANGE_NAME, "queueswap" + currentQueueCount);
							byte[] message = generateContent(mMessageSizeInBytes);
							mProducerChannel.basicPublish(EXCHANGE_NAME, "queueswap" + currentQueueCount, MessageProperties.PERSISTENT_BASIC, message);
							mConsumerChannel.basicGet(queueNames[currentQueueCount], false);
							currentQueueCount++;
						} while (currentQueueCount <= mPendingQueueCount);
						Thread.sleep(mSwappingInterval);

						for (int i = 0; i < queueNames.length; i++) {
							mConsumerChannel.queueDeleteNoWait(queueNames[i], false, false);
						}
					}
				} finally {
					mProducerChannel.exchangeDelete(EXCHANGE_NAME);
					mConsumerChannel.close();
					mProducerChannel.close();
				}
			} finally {
				connectionConsumer.close();
				connectionProducer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] generateContent(final int size) {
		byte[] message = null;
		if (size > 0) {
			message = new byte[size];
			new Random().nextBytes(message);
		}
		return message;
	}

	private String setupQueue(Channel channel, String exchangeName, String routingKey) throws Exception {
		String queueName = RandomStringUtils.randomAlphabetic(15);
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, routingKey);
		return queueName;
	}
}
