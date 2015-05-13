package de.htwk_leipzig.bis.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import com.rabbitmq.client.ConnectionFactory;

/**
 * 
 *
 *
 */
public class ToolBox {
	private final static String NTP_SERVER_ADDRESS = "ntp1.informatik.uni-leipzig.de";
	private final static int DEFAULT_TIMEOUT = 10000;

	/**
	 * 
	 * @return
	 * @throws URISyntaxException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public static ConnectionFactory createConnectionFactory(URI uri) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException {
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(uri);
		return factory;
	}

	/**
	 * 
	 * @return
	 * @throws IllegalStateException
	 */
	public static long calculateNTPOffset() throws IllegalStateException {
		final NTPUDPClient client = new NTPUDPClient();
		client.setDefaultTimeout(DEFAULT_TIMEOUT);
		try {
			client.open();
			TimeInfo info = client.getTime(InetAddress.getByName(NTP_SERVER_ADDRESS));
			info.computeDetails();

			return info.getOffset();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("NTP-server temporary unavailable");
		} finally {
			client.close();
		}

	}
}
