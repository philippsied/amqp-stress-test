package de.htwk_leipzig.bis.dos.msg_response;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public interface ResponseAction {
	public void response(Channel channel, QueueingConsumer.Delivery delivery) throws IOException;
}

