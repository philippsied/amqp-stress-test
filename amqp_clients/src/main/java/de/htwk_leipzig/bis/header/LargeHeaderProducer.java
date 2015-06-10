package de.htwk_leipzig.bis.header;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import de.htwk_leipzig.bis.util.AMQPSubscriber;

public class LargeHeaderProducer extends AMQPSubscriber {

	private static final String QUEUE_NAME = "testq";
	
	private final int mMessageSizeInBytes;
	private final int mHeaderSize;
	private final BasicProperties mProb;
	
	public LargeHeaderProducer(URI uri, int messageSizeInBytes, int headerSize) {
		super(uri);
		this.mHeaderSize = headerSize;
		this.mProb = generateHeader();
		this.mMessageSizeInBytes = messageSizeInBytes;
	}
	
	private BasicProperties generateHeader() {
		Map<String, Object> headers= new HashMap<String, Object>();
		
		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder(); 
			 
		 headers.put("x-match", "any"); //any or all
		 
		 for (int i = 0; i < mHeaderSize; i++) {
			 headers.put("header" + i, UUID.randomUUID().toString() );
		 }

	     builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
	     builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());
	     builder.headers(headers);
	     
	     return builder.build();
	}

	@Override
	protected void doSubscriberActions() throws Exception {
		
		mChannel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		byte[] message;

		if (mMessageSizeInBytes > 0) {
			message = new byte[mMessageSizeInBytes];
			new Random().nextBytes(message);
		} else {
			message = null;
		}
		
		System.out.println("Producer Online - HeaderSize: " + mHeaderSize);
		
		while(true){
			mChannel.basicPublish("", QUEUE_NAME , mProb , message);
		}	
	}
	
}
