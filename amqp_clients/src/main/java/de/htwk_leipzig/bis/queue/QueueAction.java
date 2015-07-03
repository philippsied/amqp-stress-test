package de.htwk_leipzig.bis.queue;

import java.io.IOException;

import com.rabbitmq.client.Channel;

/**
 * Represents an action that is performed after a queue is created.
 * <p>
 * This interface specify the {@code doAction()} method for performing the
 * concrete action.
 *
 */
public interface QueueAction {

    /**
     * Perform the concrete action of the implementer of {@code QueueAction}
     * 
     * @param producerChannel
     *            The channel used to produce things.
     * @param consumerChannel
     *            The channel used to consume things.
     * @param queueNumber
     *            The number of queue
     * @param queueName
     *            The name of the queue
     * @throws IOException
     *             Which may occur during the action.
     */
    public void doAction(Channel producerChannel, Channel consumerChannel, String exchangeName, int queueNumber,
	    String queueName) throws IOException;
}
