package de.htwk_leipzig.bis.transaction;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

public class TxProducer {

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
	 * Holds the uri of the RabbitMQ-Server.
	 */
	private final URI mURI;

	/**
	 * Creates a new instance of {@code TxProducer} with the given uri, message
	 * size, commit-state and producer count.
	 * 
	 * @param uri
	 * @param messageSizeInBytes
	 * @param messageCount
	 * @param commit
	 * @param producerCount
	 */
	public TxProducer(final URI uri, final int messageSizeInBytes,
			final int messageCount, final boolean commit,
			final int producerCount) {

		this.mMessageSizeInBytes = messageSizeInBytes;
		this.mCommit = commit;
		this.mMessageCount = messageCount;
		this.mURI = uri;

		System.out.print("Producer: " + producerCount);
		System.out.print(" | Messagesize: " + mMessageSizeInBytes);
		System.out.print(" | Messages per producer: " + mMessageCount);
		System.out.print(" | Commit: " + mCommit + "\n\n");

		setup(producerCount);
	}

	/**
	 * Creates the given number of producer. Its used a thread-pool to send
	 * messages over each producer.
	 * 
	 * @param producerCount
	 */
	private void setup(final int producerCount) {

		final ExecutorService es = Executors.newCachedThreadPool();

		for (int i = 0; i < producerCount; i++) {
			es.execute(new TxPublisher(i));
		}

		try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			System.out.println("Timeout reached");
		}

	}

	/**
	 * The thread for sending messages over the transaction-mode.
	 *
	 */
	public class TxPublisher extends AMQPSubscriber implements Runnable {

		/**
		 * Holds the thread-id.
		 */
		private final int threadID;

		/**
		 * Creates a new instance of {@code TxPublisher} with a special
		 * thread-id.
		 * 
		 * @param threadid
		 *            the given thread-id.
		 */
		public TxPublisher(int threadid) {
			super(mURI);
			this.threadID = threadid;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
		 */
		@Override
		protected void doSubscriberActions() throws Exception {

			// mChannel.queueDeclare(QUEUE_NAME, false, false, false, null);

			byte[] message;

			if (mMessageSizeInBytes > 0) {
				message = new byte[mMessageSizeInBytes];
				new Random().nextBytes(message);
			} else {
				message = null;
			}

			mChannel.txSelect();

			for (int i = 0; i < mMessageCount; ++i) {
				mChannel.basicPublish("", QUEUE_NAME, null, message);

				System.out.println("Producer [" + threadID + "] | Message ["
						+ i + "]");
			}

			if (mCommit) {
				mChannel.txCommit();
			}

		}
	}
}
