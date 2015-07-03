package de.htwk_leipzig.bis.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class MessageActionReject implements ResponseAction {

	public MessageActionReject() {

	}

	@Override
	public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException {
		channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
	}
	
	@Override
	public String toString() {
		return "Reject";
	}
}
