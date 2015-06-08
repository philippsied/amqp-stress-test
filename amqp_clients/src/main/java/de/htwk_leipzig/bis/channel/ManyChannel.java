package de.htwk_leipzig.bis.channel;

import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ManyChannel {
	
	private static final String QUEUE_NAME = "testq";
	
	private final Connection connection;
	private final int mMessageSizeInBytes;
	
	public ManyChannel(URI uri, int producerCount, int consumerCount, int messageSize) throws IOException {
		this.mMessageSizeInBytes = messageSize;
		this.connection = connect(uri);
		createChannels(producerCount, consumerCount);
	}

	private void createChannels(int producerCount, int consumerCount) throws IOException {
		
		final ExecutorService es = Executors.newCachedThreadPool();
		
		System.out.println("Starting Producers and Consumers");
		
		for (int i = 0; i < consumerCount; i++) {
			es.execute(new Consume(connection.createChannel()));
		}
		
		for (int i = 0; i < producerCount; i++) {
			es.execute(new Publisher(connection.createChannel()));
		}
		
		try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			System.out.println("Timeout reached");
		}
		
		connection.close();
		
	}

	public Connection connect(URI uri) {
		
		ConnectionFactory factory = new ConnectionFactory();
		
		try {
			factory.setUri(uri);
			return factory.newConnection();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		return null;
	
	}
	
	//------------------------------------------------------------------
	
	public class Publisher implements Runnable{
		
		final Channel channel;
		
		public Publisher(Channel channel) {
			this.channel = channel;
		}

		public void run() {

			try {
				channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			byte[] message = generateMessage();
			
			while(true){	
				try {
					channel.basicPublish("", QUEUE_NAME , null, message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private byte[] generateMessage() {
			byte[] message;
			if (mMessageSizeInBytes > 0) {
				message = new byte[mMessageSizeInBytes];
				new Random().nextBytes(message);
			} else {
				message = null;
			}
			return message;
		}

	}
	
	//------------------------------------------------------------------
	
		public class Consume implements Runnable{
			
			final Channel channel;
			
			public Consume(Channel channel) {
				this.channel = channel;
			}

			public void run() {

				QueueingConsumer consumer = new QueueingConsumer(channel);
			    try {
					channel.basicConsume(QUEUE_NAME, true, consumer);
				} catch (IOException e) {
					e.printStackTrace();
				}

			    while (true) {
					try {
						consumer.nextDelivery();
						//Delivery delivery  = consumer.nextDelivery();
						//String message = new String(delivery.getBody());
					    //System.out.println(" [x] Received '" + message + "'");
					} catch (Exception e) {
						e.printStackTrace();
					}
			      
			    }
			}


		}
	
}
