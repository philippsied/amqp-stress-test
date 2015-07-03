package de.htwk_leipzig.bis.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Represents an action that is performed after a message is consumed.
 * <p>
 * This interface specify the {@code response()} method for performing the
 * concrete action.
 */
public interface ResponseAction {
    /**
     * 
     * Perform the concrete action of the implementer of {@code ResponseAction}
     * 
     * 
     * @param channel
     *            The channel that is currently used.
     * @param delivery
     *            The consumed delivery.
     * @throws IOException
     *             Which may occur during the action.
     */
    public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException;
}
