package de.htwk_leipzig.bis.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * This class represents a response on a given message. It sends an ACK to the
 * server to indicate, that the message is properly arrived.
 */
public class MessageActionACK implements ResponseAction {

    /**
     * Creates an instance of {@code MessageActionACK}.
     */
    public MessageActionACK() {
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
	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "ACK";
    }
}
