package de.htwk_leipzig.bis.connection.handshake;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.ConnectionParams;
import com.rabbitmq.client.impl.FrameHandler;
import com.rabbitmq.client.impl.FrameHandlerFactory;
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection;

import de.htwk_leipzig.bis.connection.handshake.clientRewrite.AMQConnection;

/**
 * This class is a {@code ConnectionFactory} which only overides the
 * {@code newConnection()} methode to invoke the custom connection with a given
 * handshake action.
 *
 */
public class CustomConnectionFactory extends ConnectionFactory {

    /**
     * Member variable to hold the used action between the steps of the
     * handshake.
     */
    private HandshakeAction mHandshakeAction;

    /**
     * Creates a new instance of {@code CustomConnectionFactory} with the given
     * handshake action.
     * 
     * @param handshakeAction
     *            The action to perform during handshake steps.
     */
    public CustomConnectionFactory(final HandshakeAction handshakeAction) {
	super();
	mHandshakeAction = handshakeAction;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rabbitmq.client.ConnectionFactory#newConnection(java.util.concurrent
     * .ExecutorService, com.rabbitmq.client.Address[])
     */
    @Override
    public Connection newConnection(ExecutorService executor, Address[] addrs) throws IOException {
	FrameHandlerFactory fhFactory = createFrameHandlerFactory();
	ConnectionParams params = params(executor);

	if (isAutomaticRecoveryEnabled()) {
	    // see
	    // com.rabbitmq.client.impl.recovery.RecoveryAwareAMQConnectionFactory#newConnection
	    AutorecoveringConnection conn = new AutorecoveringConnection(params, fhFactory, addrs);
	    conn.init();
	    return conn;
	} else {
	    IOException lastException = null;
	    for (Address addr : addrs) {
		try {
		    FrameHandler handler = fhFactory.create(addr);

		    /*
		     * Fix for bogus use
		     */
		    AMQConnection conn = new AMQConnection(params, handler);
		    conn.setHandshakeAction(mHandshakeAction);
		    conn.start();

		    return conn;
		} catch (IOException e) {
		    lastException = e;
		}
	    }
	    throw (lastException != null) ? lastException : new IOException("failed to connect");
	}
    }
}
