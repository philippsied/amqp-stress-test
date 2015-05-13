package de.htwk_leipzig.bis.main;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.URI;

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

import de.htwk_leipzig.bis.timing.TimingClient;
import de.htwk_leipzig.bis.timing.TimingServer;
import de.htwk_leipzig.bis.util.ToolBox;

/**
 * 
 * 
 *
 */
public class Main {

	@SuppressWarnings("static-access")
	private static final Option HELP_OPT = OptionBuilder.hasArg(false).withDescription("Show help").withLongOpt("help").create('h');
	@SuppressWarnings("static-access")
	private static final Option AS_SERVER_OPT = OptionBuilder.hasArg(false).withDescription("Start as server").withLongOpt("server").create('s');
	@SuppressWarnings("static-access")
	private static final Option AS_CLIENT_OPT = OptionBuilder.hasArg(false).withDescription("Start as client").withLongOpt("client").create('c');
	@SuppressWarnings("static-access")
	private static final Option PING_DELAY_OPT = OptionBuilder.isRequired(false).hasArg().withArgName("milliseconds").withType(Number.class)
			.withDescription("Set ping delay, only used as client").withLongOpt("delay").create('d');
	@SuppressWarnings("static-access")
	private static final Option URI_OPT = OptionBuilder.isRequired(true).hasArg().withArgName("uri").withDescription("Set uri, required").withLongOpt("uri")
			.create('u');

	private static final int DEFAULT_PING_DELAY = 1;

	public static void main(String... args) throws Exception {
		final Options options = new Options();
		final OptionGroup optionGrp = new OptionGroup();

		optionGrp.setRequired(true);
		optionGrp.addOption(AS_SERVER_OPT);
		optionGrp.addOption(AS_CLIENT_OPT);
		options.addOption(HELP_OPT);
		options.addOptionGroup(optionGrp);
		options.addOption(PING_DELAY_OPT);
		options.addOption(URI_OPT);

		final CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			printHelp(options);
			System.exit(-1);
		}

		if (cmd.hasOption(HELP_OPT.getOpt())) {
			printHelp(options);
			System.exit(0);
		}

		/*
		 * Check for valid URI, server address, credentials and connectivity
		 */
		URI uri = null;
		try {
			uri = new URI(cmd.getOptionValue(URI_OPT.getOpt()));
			final ConnectionFactory factory = ToolBox.createConnectionFactory(uri);
			try {
				final Connection connection = factory.newConnection();
				try {
				} finally {
					connection.close();
				}
			} catch (IOException ie) {
				System.err.println(ie.getLocalizedMessage());
				System.exit(-1);
			}
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}

		if (cmd.hasOption(AS_SERVER_OPT.getOpt())) {
			(new TimingServer()).start(uri);
			System.exit(0);
		}
		if (cmd.hasOption(AS_CLIENT_OPT.getOpt())) {
			int pingDelay = DEFAULT_PING_DELAY;

			if (cmd.hasOption(PING_DELAY_OPT.getOpt())) {
				try {
					pingDelay = ((Number) cmd.getParsedOptionValue(PING_DELAY_OPT.getOpt())).intValue();
					checkArgument(pingDelay >= 0);
				} catch (ParseException | IllegalArgumentException e) {
					System.err.println("Invalid argument: '" + pingDelay + "' for -" + PING_DELAY_OPT.getOpt());
					System.err.println("Only natural numbers allowed: 0,1,2, ... ");
					System.exit(-1);
				}
			}

			(new TimingClient(pingDelay)).start(uri);
			System.exit(0);
		}
		/*
		 * if none of the options were used
		 */
		printHelp(options);
	}

	private static void printHelp(Options options) {
		final HelpFormatter help = new HelpFormatter();
		help.printHelp("amqptest", "Options", options, "", true);
	}
}
