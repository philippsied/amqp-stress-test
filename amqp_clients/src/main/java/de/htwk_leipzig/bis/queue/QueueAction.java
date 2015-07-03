package de.htwk_leipzig.bis.dos.queue;

import java.io.IOException;

import com.rabbitmq.client.Channel;

public interface QueueAction {
	
	/**
	 * 
	 * @param producerChannel
	 * @param consumerChannel
	 * @param queueNumber
	 * @param queueName
	 * @throws IOException
	 */
	public void doAction(Channel producerChannel, Channel consumerChannel, String exchangeName, int queueNumber, String queueName) throws IOException;
}
