package de.htwk_leipzig.bis.transaction;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

public class TxProducer {

	private static final String QUEUE_NAME = "testq";

	private final int mMessageSizeInBytes;
	private final int mMessageCount;
	private final boolean mCommit;
	private final URI mURI;

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

	public class TxPublisher extends AMQPSubscriber implements Runnable {

		private final int threadID;

		public TxPublisher(int i) {
			super(mURI);
			this.threadID = i;
		}

		@Override
		protected void doSubscriberActions() throws Exception {

			//mChannel.queueDeclare(QUEUE_NAME, false, false, false, null);

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
				
				System.out.println("Producer [" + threadID + "] | Message [" + i + "]");
			}

			if (mCommit) {
				mChannel.txCommit();
			}

		}
	}
}
