/**
 * 
 */
package de.htwk_leipzig.bis.dos.msg_response;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.util.Random;

import com.rabbitmq.client.MessageProperties;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

/**
 *
 *
 */
public class MessageProducer extends AMQPSubscriber {
	private static final String EXCHANGE_NAME = "messaging";

	private final int mProduceInterval;
	private final int mMessageSizeInBytes;
	private final boolean mUsePersistentMessage;

	/**
	 * 
	 * @param uri
	 * @param produceIntervalInSec
	 *            Interval between two produced messages, specified in
	 *            milliseconds. The value "0" means as fast as possible.
	 * 
	 * @param usePersistentMessage
	 * @param messageSizeInBytes
	 *            The size of the message to be sent. The value "0" means no
	 *            message payload.
	 */
	public MessageProducer(final URI uri, final int produceIntervalInSec, final boolean usePersistentMessage, final int messageSizeInBytes) {
		super(uri);
		checkArgument(0 <= produceIntervalInSec, "Interval must be greater or equal 0");
		checkArgument(0 <= messageSizeInBytes, "Message size must be greater or equal 0");
		mProduceInterval = produceIntervalInSec;
		mMessageSizeInBytes = messageSizeInBytes;
		mUsePersistentMessage = usePersistentMessage;
	}

	/**
	 * @see de.htwk_leipzig.bis.util.AMQPSubscriber#doSubscriberActions()
	 */
	@Override
	protected void doSubscriberActions() throws Exception {
		mChannel.exchangeDeclare(EXCHANGE_NAME, "fanout", mUsePersistentMessage, false, null);
	
		System.out.println("Producer Online");
		while (true) {
			byte[] message;

			if (mMessageSizeInBytes > 0) {
				message = new byte[mMessageSizeInBytes];
				new Random().nextBytes(message);
			} else {
				message = null;
			}
			if (mUsePersistentMessage) {
				mChannel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_BASIC, message);
			} else {
				mChannel.basicPublish(EXCHANGE_NAME, "", null, message);
			}
			Thread.sleep(mProduceInterval);
		}
	}
}
