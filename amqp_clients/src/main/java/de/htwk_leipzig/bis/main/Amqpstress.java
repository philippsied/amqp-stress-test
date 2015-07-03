package de.htwk_leipzig.bis.main;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.htwk_leipzig.bis.channel.ChannelConsumer;
import de.htwk_leipzig.bis.channel.ChannelProducer;
import de.htwk_leipzig.bis.connection.dropconnection.ConnectionDropping;
import de.htwk_leipzig.bis.connection.handshake.CustomConnection;
import de.htwk_leipzig.bis.connection.handshake.HandshakeActionSleep;
import de.htwk_leipzig.bis.connection.heartbeat.HeartbeatStressor;
import de.htwk_leipzig.bis.header.LargeHeaderProducer;
import de.htwk_leipzig.bis.msg_response.MessageActionACK;
import de.htwk_leipzig.bis.msg_response.MessageActionNACK;
import de.htwk_leipzig.bis.msg_response.MessageActionNoResponse;
import de.htwk_leipzig.bis.msg_response.MessageActionReject;
import de.htwk_leipzig.bis.msg_response.MessageConsumer;
import de.htwk_leipzig.bis.msg_response.MessageProducer;
import de.htwk_leipzig.bis.msg_response.ResponseAction;
import de.htwk_leipzig.bis.queue.QueueAction;
import de.htwk_leipzig.bis.queue.QueueActionAddMsg;
import de.htwk_leipzig.bis.queue.QueueActionNo;
import de.htwk_leipzig.bis.queue.QueueAgitator;
import de.htwk_leipzig.bis.timing.TimingClient;
import de.htwk_leipzig.bis.timing.TimingServer;
import de.htwk_leipzig.bis.transaction.TxProducer;
import de.htwk_leipzig.bis.util.ToolBox;

/**
 * 
 * 
 *
 */
public class Amqpstress {

	private static final int DEFAULT_PRODUCER_COUNT = 1;
	private static final int DEFAULT_CONSUMER_COUNT = 5;
	private static final int DEFAULT_CLIENT_COUNT = 1;
	private static final int DEFAULT_MESSAGE_SIZE = 1024;
	private static final int DEFAULT_MESSAGE_COUNT = 1000;
	private static final int DEFAULT_INTERVAL = 100;
	private static final int DEFAULT_PENDING_COUNT = 1000;
	private static final int DEFAULT_HEADER_SIZE = 1000;
	private static final boolean DEFAULT_USE_PERSISTENT_MESSAGE = false;

	/**
	 * Count of prefetched messages. 0 means infinity.
	 */
	public static final int DEFAULT_PREFETCH_COUNT = 0;

	/**
	 * Amount of prefetched messages in octets. Current value means 512MB.
	 */
	public static final int DEFAULT_PREFETCH_AMOUNT = 536_870_912;

	public static void main(String... args) throws Exception {
		final Options options = new Options();
		final OptionGroup optionGrp = new OptionGroup();

		optionGrp.setRequired(true);
		options.addOption(ProgramOptions.HELP_OPT);
		optionGrp.addOption(ProgramOptions.AS_SERVER_OPT);
		optionGrp.addOption(ProgramOptions.AS_CLIENT_OPT);
		optionGrp.addOption(ProgramOptions.AS_DOS_MSG);
		optionGrp.addOption(ProgramOptions.AS_DOS_QUEUE);
		optionGrp.addOption(ProgramOptions.AS_LARGE_HEADER);
		optionGrp.addOption(ProgramOptions.AS_MANY_CHANNEL);
		optionGrp.addOption(ProgramOptions.AS_TRANSACTION);
		optionGrp.addOption(ProgramOptions.AS_HANDSHAKE_TRICKLE);
		optionGrp.addOption(ProgramOptions.AS_STRESS_HEARTBEAT);
		optionGrp.addOption(ProgramOptions.AS_DROP_CONNECTIONS);
		options.addOptionGroup(optionGrp);
		options.addOption(ProgramOptions.CLIENT_COUNT_OPT);
		options.addOption(ProgramOptions.PRODUCER_COUNT_OPT);
		options.addOption(ProgramOptions.CONSUMER_COUNT_OPT);
		options.addOption(ProgramOptions.MESSAGE_SIZE_OPT);
		options.addOption(ProgramOptions.INTERVAL_OPT);
		options.addOption(ProgramOptions.PENDING_COUNT_OPT);
		options.addOption(ProgramOptions.PERSISTENT_OPT);
		options.addOption(ProgramOptions.URI_OPT);
		options.addOption(ProgramOptions.HEADER_SIZE_OPT);
		options.addOption(ProgramOptions.COMMIT_OPT);
		options.addOption(ProgramOptions.MESSAGE_COUNT_OPT);

		final CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			printHelp(options);
			System.exit(-1);
		}

		if (cmd.hasOption(ProgramOptions.HELP_OPT.getOpt())) {
			printHelp(options);
			System.exit(0);
		}

		/*
		 * Check for valid URI, server address, credentials and connectivity
		 */
		URI uri = null;
		try {
			uri = new URI(cmd.getOptionValue(ProgramOptions.URI_OPT.getOpt()));
			final ConnectionFactory factory = ToolBox.createConnectionFactory(uri);
			try {
				Connection connection = null;
				try {
					connection = factory.newConnection();
				} finally {
					if (connection != null) {
						connection.close();
					}
				}
			} catch (IOException ie) {
				System.err.println(ie.getLocalizedMessage());
				System.exit(-1);
			}
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}

		final boolean userPersistent = getUsePersistent(cmd);
		final int messageSize = getMessageSize(cmd);
		final int headerSize = getHeaderSize(cmd);
		final int interval = getInterval(cmd);
		final int producerCount = getProducerCount(cmd);
		final int consumerCount = getConsumerCount(cmd);
		final int clientCount = getClientCount(cmd);

		/*
		 * Do specified action
		 */

		if (cmd.hasOption(ProgramOptions.AS_SERVER_OPT.getOpt())) {
			(new TimingServer(uri)).run();
			System.exit(0);
		}
		if (cmd.hasOption(ProgramOptions.AS_CLIENT_OPT.getOpt())) {
			(new TimingClient(uri, interval)).run();
			System.exit(0);
		}
		if (cmd.hasOption(ProgramOptions.AS_DOS_MSG.getOpt())) {
			final ResponseAction responseAction = getResponseType(cmd);
			System.out.printf(
					"Producer: %d\nConsumer: %d\ninterval: %d\nMessagesize: %d\nPersistent: %s\nAction: %s\n\n",
					producerCount, consumerCount, interval, messageSize, Boolean.toString(userPersistent),
					responseAction.toString());

			/*
			 * + 5% delay for producer, used to adjust send and receive rate
			 */
			int prodInterval = (int) Math.ceil(interval * 1.05);

			/*
			 * Necessary since the method to set a custom prefetch amount of the
			 * rabbitmq server is currently not implemented.
			 */
			int prefetchCount = calcPrefetchCount(DEFAULT_PREFETCH_COUNT, DEFAULT_PREFETCH_AMOUNT / consumerCount,
					messageSize);
			startClients(consumerCount, producerCount,
					new MessageConsumer(uri, interval, prefetchCount, 0, userPersistent, responseAction),
					new MessageProducer(uri, prodInterval, userPersistent, messageSize));

			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_LARGE_HEADER.getOpt())) {
			System.out.println("Producer Online - HeaderSize: " + headerSize);
			startClients(0, producerCount, null, new LargeHeaderProducer(uri, messageSize, headerSize,interval));
			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_MANY_CHANNEL.getOpt())) {
			System.out.println("Starting Producers and Consumers");
			final Connection connection = ToolBox.createConnectionFactory(uri).newConnection();
			startClients(consumerCount, producerCount, new ChannelConsumer(connection),
					new ChannelProducer(connection, messageSize));
			connection.close();
			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_TRANSACTION.getOpt())) {
			startClients(0, producerCount, null,
					new TxProducer(uri, messageSize, getMessageCount(cmd), getCommit(cmd)));
			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_DOS_QUEUE.getOpt())) {
			final QueueAction action = getQueueAction(cmd);
			System.out.printf("QueueSwapper\ninterval: %d\nMessagesize: %d\nPersistent: %s\n\n", interval, messageSize,
					Boolean.toString(userPersistent));
			(new QueueAgitator(uri, interval, userPersistent, getPendingCount(cmd), action)).run();
			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_HANDSHAKE_TRICKLE.getOpt())) {
			System.out.printf("Slow Trickle\ninterval: %d\n\n", interval, messageSize,
					Boolean.toString(userPersistent));
			(new CustomConnection(uri, interval, new HandshakeActionSleep())).run();
			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_STRESS_HEARTBEAT.getOpt())) {
			System.out.printf("Stress with Heartbeats\nConnections: %d\n\n", clientCount);
			(new HeartbeatStressor(uri, clientCount)).run();
			System.exit(0);
		}

		if (cmd.hasOption(ProgramOptions.AS_DROP_CONNECTIONS.getOpt())) {
			System.out.printf("Drop Connections\ninterval: %d\n\n", interval);
			(new ConnectionDropping(uri, interval)).run();
			System.exit(0);
		}

		/*
		 * if none of the options were used
		 */
		printHelp(options);
	}

	private static void startClients(int consumerCount, int producerCount, Runnable runConsumer, Runnable runProducer) {
		final ExecutorService es = Executors.newCachedThreadPool();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Terminate main");
				es.shutdown();
			}
		});
		for (int i = 0; i < consumerCount; i++) {
			es.execute(runConsumer);
		}
		for (int i = 0; i < producerCount; i++) {
			es.execute(runProducer);
		}
		try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			System.out.println("Timeout reached");
		}
	}

	private static QueueAction getQueueAction(CommandLine cmd) {
		String option = ProgramOptions.AS_DOS_QUEUE.getOpt();
		switch (cmd.getOptionValue(option).toLowerCase()) {
		case "no":
			return new QueueActionNo();
		case "msg":
			return new QueueActionAddMsg(getMessageSize(cmd), getUsePersistent(cmd));
		default:
			System.err.println("Invalid argument: \"" + cmd.getOptionValue(option) + "\" for option -" + option);
			System.err.println("Only \"NO\", \"msg\" allowed");
			System.exit(-1);
		}
		return null;
	}

	private static ResponseAction getResponseType(CommandLine cmd) {
		String option = ProgramOptions.AS_DOS_MSG.getOpt();

		switch (cmd.getOptionValue(option).toLowerCase()) {
		case "no":
			return new MessageActionNoResponse();
		case "nack":
			return new MessageActionNACK(getPendingCount(cmd));
		case "reject":
			return new MessageActionReject();
		case "ack":
			return new MessageActionACK();
		default:
			System.err.println("Invalid argument: \"" + cmd.getOptionValue(option) + "\" for option -" + option);
			System.err.println("Only \"ACK\", \"NO\", \"NACK\", \"REJECT\" allowed");
			System.exit(-1);
		}
		return null;
	}

	private static boolean getUsePersistent(CommandLine cmd) {
		String option = ProgramOptions.PERSISTENT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return true;
		} else {
			return DEFAULT_USE_PERSISTENT_MESSAGE;
		}
	}

	private static int getClientCount(CommandLine cmd) {
		String option = ProgramOptions.CLIENT_COUNT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_CLIENT_COUNT;
		}
	}

	private static int getConsumerCount(CommandLine cmd) {
		String option = ProgramOptions.CONSUMER_COUNT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_CONSUMER_COUNT;
		}
	}

	private static int getProducerCount(CommandLine cmd) {
		String option = ProgramOptions.PRODUCER_COUNT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_PRODUCER_COUNT;
		}
	}

	private static int getPendingCount(CommandLine cmd) {
		String option = ProgramOptions.PENDING_COUNT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_PENDING_COUNT;
		}
	}

	private static int getInterval(CommandLine cmd) {
		String option = ProgramOptions.INTERVAL_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_INTERVAL;
		}
	}

	private static int getMessageSize(CommandLine cmd) {
		String option = ProgramOptions.MESSAGE_SIZE_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_MESSAGE_SIZE;
		}
	}

	private static int getMessageCount(CommandLine cmd) {
		String option = ProgramOptions.MESSAGE_COUNT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_MESSAGE_COUNT;
		}
	}

	private static boolean getCommit(CommandLine cmd) {
		String option = ProgramOptions.COMMIT_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return Boolean.valueOf(cmd.getOptionValue(option));
		} else {
			return false;
		}
	}

	private static int getHeaderSize(CommandLine cmd) {
		String option = ProgramOptions.HEADER_SIZE_OPT.getOpt();
		if (cmd.hasOption(option)) {
			return checkForNaturalNumber(cmd, option);
		} else {
			return DEFAULT_HEADER_SIZE;
		}
	}

	private static int checkForNaturalNumber(CommandLine cmd, String option) {
		int naturalNumber = 0;
		try {
			naturalNumber = ((Number) cmd.getParsedOptionValue(option)).intValue();
			checkArgument(naturalNumber >= 0);
		} catch (ParseException | IllegalArgumentException e) {
			System.err.println("Invalid argument: \"" + cmd.getOptionValue(option) + "\" for option -" + option);
			System.err.println("Only natural numbers allowed: 0,1,2, ... ");
			System.exit(-1);
		}
		return naturalNumber;
	}

	/**
	 * 
	 * @param prefetchCount
	 * @param prefetchAmount
	 * @param messageSize
	 * @return
	 */
	private static int calcPrefetchCount(final int prefetchCount, final int prefetchAmount, final int messageSize) {
		return Math.min(prefetchCount, prefetchAmount / messageSize);
	}

	private static void printHelp(Options options) {
		final HelpFormatter help = new HelpFormatter();
		help.printHelp("amqptest", "Options", options, "", true);
	}

	/**
	 * private nested class for Program options
	 *
	 */
	private static final class ProgramOptions {
		public static final Option HELP_OPT = new Option("h", "help", false, "Show help");

		@SuppressWarnings("static-access")
		public static final Option URI_OPT = OptionBuilder.isRequired(true).hasArg().withArgName("uri")
				.withDescription("Set uri, required").withLongOpt("uri").create('u');
		public static final Option AS_SERVER_OPT = new Option("ts", "server", false, "Start as timing server");
		public static final Option AS_CLIENT_OPT = new Option("tc", "client", false, "Start as timing client");

		@SuppressWarnings("static-access")
		public static final Option AS_DOS_MSG = OptionBuilder.isRequired(false).hasArg().withArgName("responsetyp")
				.withDescription("DoS with messages, type is one of \"ACK\",\"NO\",\"NACK\",\"REJECT\"")
				.withLongOpt("dosmsg").create("dm");

		@SuppressWarnings("static-access")
		public static final Option AS_LARGE_HEADER = OptionBuilder.isRequired(false)
				.withDescription("Send messages with large header").withLongOpt("largeheader").create("lh");

		@SuppressWarnings("static-access")
		public static final Option AS_MANY_CHANNEL = OptionBuilder.isRequired(false)
				.withDescription("Send messages over one Connection and many Channels - set over -p and -c")
				.withLongOpt("manych").create("mc");

		@SuppressWarnings("static-access")
		public static final Option AS_TRANSACTION = OptionBuilder.isRequired(false)
				.withDescription("Used the transaction-mode").withLongOpt("txmode").create("tx");

		@SuppressWarnings("static-access")
		public static final Option AS_HANDSHAKE_TRICKLE = OptionBuilder.isRequired(false)
				.withDescription("Slow down the connection handshake").withLongOpt("slowhand").create("sh");

		@SuppressWarnings("static-access")
		public static final Option AS_STRESS_HEARTBEAT = OptionBuilder.isRequired(false)
				.withDescription("use small heartbeats to stress server").withLongOpt("heartbeat").create("hb");

		@SuppressWarnings("static-access")
		public static final Option AS_DROP_CONNECTIONS = OptionBuilder.isRequired(false)
				.withDescription(
						"open connection and immediately close the Socket (Without keep-alive, max. heartbeat) and send TCP RST")
				.withLongOpt("dropcon").create("dc");

		@SuppressWarnings("static-access")
		public static final Option AS_DOS_QUEUE = OptionBuilder.isRequired(false).hasArgs(1).withArgName("queueAction")
				.withDescription("DoS with queues, type is one of \"NO\",\"MSG\"").withLongOpt("dosqueue").create("dq");

		public static final Option PERSISTENT_OPT = new Option("mp", "persistent", false,
				"Set messages/queues persistent");

		@SuppressWarnings("static-access")
		public static final Option MESSAGE_SIZE_OPT = OptionBuilder.isRequired(false).hasArg()
				.withArgName("size in bytes").withType(Number.class).withDescription("Set the size of each message")
				.withLongOpt("msize").create("ms");

		@SuppressWarnings("static-access")
		public static final Option INTERVAL_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("milliseconds")
				.withType(Number.class).withDescription("Set interval for a simple action, interpreted as milliseconds")
				.withLongOpt("minterval").create('i');

		@SuppressWarnings("static-access")
		public static final Option CLIENT_COUNT_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("count")
				.withType(Number.class).withDescription("Set count of parallel running clients").withLongOpt("clients")
				.create("cl");

		@SuppressWarnings("static-access")
		public static final Option PRODUCER_COUNT_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("count")
				.withType(Number.class).withDescription("Set count of parallel running producers")
				.withLongOpt("producer").create('p');
		@SuppressWarnings("static-access")
		public static final Option CONSUMER_COUNT_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("count")
				.withType(Number.class).withDescription("Set count of parallel running consumers")
				.withLongOpt("consumer").create('c');
		@SuppressWarnings("static-access")
		public static final Option PENDING_COUNT_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("count")
				.withType(Number.class)
				.withDescription("Set count of cached elements, i. e. the count of message to NACK all at once")
				.withLongOpt("pendingcount").create("pc");

		@SuppressWarnings("static-access")
		public static final Option HEADER_SIZE_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("size")
				.withType(Number.class).withDescription("Set the size of the Headerfield - Number of entrys")
				.withLongOpt("headersize").create("hs");
		@SuppressWarnings("static-access")
		public static final Option COMMIT_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("commit")
				.withType(Boolean.class).withDescription("Set for committing messages - true/false")
				.withLongOpt("commit_messages").create("co");
		@SuppressWarnings("static-access")
		public static final Option MESSAGE_COUNT_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("count")
				.withType(Number.class).withDescription("Set the number of messages").withLongOpt("massagescount")
				.create("mct");
	}
}
