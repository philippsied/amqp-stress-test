/**
 * 
 */
package de.htwk_leipzig.bis.queue;

import java.io.IOException;

import com.rabbitmq.client.Channel;

/**
 *
 */
public class QueueActionNo implements QueueAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.htwk_leipzig.bis.dos.queue.QueueAction#doAction(com.rabbitmq.client
	 * .Channel, com.rabbitmq.client.Channel, int, java.lang.String)
	 */
	@Override
	public void doAction(Channel producerChannel, Channel consumerChannel, String exchangeName, int queueNumber, String queueName) throws IOException {
		// Do absolutely nothing

	}

	@Override
	public String toString() {
		return "QueueActionNo";
	}

}
