package de.htwk_leipzig.bis.dos.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class MessageActionACK implements ResponseAction {

	public MessageActionACK() {
	}

	@Override
	public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException {
		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	}

	@Override
	public String toString() {
		return "ACK";
	}
}
