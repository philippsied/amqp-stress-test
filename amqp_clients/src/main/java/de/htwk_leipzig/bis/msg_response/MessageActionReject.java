package de.htwk_leipzig.bis.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * This class represents a response on a given message. It sends a REJECT to the
 * server to indicate, that the message must be redelivered.
 *
 */
public class MessageActionReject implements ResponseAction {

    /**
     * Creates an instance of {@code MessageActionReject}.
     */
    public MessageActionReject() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.htwk_leipzig.bis.msg_response.ResponseAction#response(com.rabbitmq.
     * client.Channel, com.rabbitmq.client.QueueingConsumer.Delivery)
     */
    @Override
    public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException {
	channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "Reject";
    }
}
