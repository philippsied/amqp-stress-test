package de.htwk_leipzig.bis.channel;

import java.io.IOException;

import com.rabbitmq.client.Connection;

public class ChannelBasic {

	protected static final String QUEUE_NAME = "testq";
	Connection mConnection;
	
	public ChannelBasic(Connection connection) throws IOException {
		this.mConnection = connection;
	}
}
