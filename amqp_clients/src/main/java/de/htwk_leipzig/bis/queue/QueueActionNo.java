/**
 * 
 */
package de.htwk_leipzig.bis.queue;

import java.io.IOException;

import com.rabbitmq.client.Channel;

/**
 * This class represents a action on a queue. It does simply do nothing.
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
    public void doAction(Channel producerChannel, Channel consumerChannel, String exchangeName, int queueNumber,
	    String queueName) throws IOException {
	// Do absolutely nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "QueueActionNo";
    }

}
