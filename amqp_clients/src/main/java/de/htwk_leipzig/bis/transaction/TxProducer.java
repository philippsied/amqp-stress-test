package de.htwk_leipzig.bis.transaction;

import java.net.URI;
import java.util.Random;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

public class TxProducer extends AMQPSubscriber {

	/**
	 * The queue-name for this test.
	 */
	private static final String QUEUE_NAME = "testq";

	/**
	 * Hold the used message size in bytes.
	 */
	private final int mMessageSizeInBytes;

	/**
	 * Hold the number of messages for each producer.
	 */
	private final int mMessageCount;

	/**
	 * Holds the state to be perform a commit after sending all the messages.
	 */
	private final boolean mCommit;

	/**
	 * Holds the special thread-id
	 */
	private int mID;

	/**
	 * Creates a new instance of {@code TxProducer} with the given uri, message
	 * size and commit-state.
	 * 
	 * @param uri
	 *            the uri-string for the server-connection
	 * @param messageSizeInBytes
	 *            the size for each messages
	 * @param messageCount
	 *            the number of messages for each thread
	 * @param commit
	 *            the commit-state for commit all messages or not
	 */
	public TxProducer(URI uri, final int messageSizeInBytes,
			final int messageCount, final boolean commit) {
		super(uri);
		this.mID = 1;
		this.mMessageSizeInBytes = messageSizeInBytes;
		this.mCommit = commit;
		this.mMessageCount = messageCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
	 */
	@Override
	protected void doSubscriberActions(Connection connection, Channel channel)
			throws Exception {

		// mChannel.queueDeclare(QUEUE_NAME, false, false, false, null);

		byte[] message;
		final int threadID = mID++;

		if (mMessageSizeInBytes > 0) {
			message = new byte[mMessageSizeInBytes];
			new Random().nextBytes(message);
		} else {
			message = null;
		}

		channel.txSelect();

		for (int i = 0; i < mMessageCount; ++i) {
			channel.basicPublish("", QUEUE_NAME, null, message);

			System.out.println("Producer [" + threadID + "] | Message [" + i
					+ "]");
		}

		if (mCommit) {
			channel.txCommit();
		}
	}

}
