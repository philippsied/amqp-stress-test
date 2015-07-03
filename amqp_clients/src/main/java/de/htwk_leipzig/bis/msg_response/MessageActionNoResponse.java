package de.htwk_leipzig.bis.dos.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class MessageActionNoResponse implements ResponseAction {

	public MessageActionNoResponse() {

	}

	@Override
	public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException {
		// Do absolutely nothing
	}

	@Override
	public String toString() {
		return "No Response";
	}
	
	
}
