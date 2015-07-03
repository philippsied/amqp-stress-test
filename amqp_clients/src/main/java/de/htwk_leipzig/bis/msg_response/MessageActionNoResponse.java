package de.htwk_leipzig.bis.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * This class represents a response on a given message. It does simply do nothing.
 *
 */
public class MessageActionNoResponse implements ResponseAction {

    /**
     * Creates an instance of {@code MessageActionNoResponse}.
     */
    public MessageActionNoResponse() {
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
	// Do absolutely nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "No Response";
    }

}
