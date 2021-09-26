package br.com.topaz.loadBalancer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Rafael Costa
 *
 */
public class LoadBalancer {

	// Return Codes
	public static final int OK = 0;
	public static final int INPUT_FILE_DOES_NOT_EXIST = -1;
	public static final int INVALID_TTASK_ON_INPUT_FILE = -2;
	public static final int INVALID_UMAX_ON_INPUT_FILE = -3;
	public static final int ERROR_CREATING_OUTPUT_FILE = -4;
	public static final int INVALID_CONTENT_ON_INPUT_FILE = -5;

	private static final int MIX_TTASK = 1;
	private static final int MAX_TTASK = 10;
	private static final int MIX_UMAX = 1;
	private static final int MAX_UMAX = 10;

	private int ttask = 0;
	private int umax = 0;
	private Long totalCost = Long.valueOf(0);

	private LinkedList<ServerInstance> linkedListServers = new LinkedList<ServerInstance>();

	/**
	 * @param args
	 * 
	 *             args[0]: Full path to Input File
	 * 
	 *             args[1]: Full path to Output File
	 * 
	 */
	public static void main(final String[] args) {
		if (args.length != 2) {
			System.out.println("USAGE: java -jar loadBalancer.jar X:\\path\\input.txt X:\\path\\output.txt");
			System.exit(0);
		}
		LoadBalancer loadBalancer = new LoadBalancer();
		loadBalancer.loadBalance(args[0], args[1]);
	}

	/**
	 * 
	 * Produces the output file, corresponding to the total number of users on each
	 * server, each tick.
	 * 
	 * Last line indicates the total Cost of the processed input file (R$ 1.00 for
	 * tick)
	 * 
	 * @param inputFilePath  - The path for the input file
	 * @param outputFilePath - The path for the output file
	 * @return Return code (See the begging of LoadBalancer class)
	 */
	public int loadBalance(final String inputFilePath, final String outputFilePath) {
		int rc = OK;

		File file = new File(inputFilePath);
		if ((rc = validateFile(file)) != OK) {
			logError(rc);
			return rc;
		}

		Long usersToLoad = Long.valueOf(0);
		String[] lineString = new String[1];

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			if (!readLine(br, lineString)) {
				logError(INVALID_TTASK_ON_INPUT_FILE);
				return rc;
			}

			ttask = getIntNumber(lineString[0], MIX_TTASK, MAX_TTASK);
			if (ttask == 0) {
				logError(INVALID_TTASK_ON_INPUT_FILE);
				return rc;
			}

			if (!readLine(br, lineString)) {
				logError(INVALID_UMAX_ON_INPUT_FILE);
				return rc;
			}

			umax = getIntNumber(lineString[0], MIX_UMAX, MAX_UMAX);
			if (umax == 0) {
				logError(INVALID_UMAX_ON_INPUT_FILE);
				return rc;
			}

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(outputFilePath, "UTF-8");
			} catch (Exception e) {
				logError(ERROR_CREATING_OUTPUT_FILE);
				return rc;
			}

			Long tick = Long.valueOf(0);
			while (readLine(br, lineString)) {
				tick++;

				usersToLoad = getLongNumber(lineString[0]);

				if (lineString[0].length() > 0 && usersToLoad < 0) {
					rc = INVALID_CONTENT_ON_INPUT_FILE;
					logErrorTick(rc, tick);
					break;
				}

				updateServersAfterTick(tick);

				if (usersToLoad > 0) {
					updateServersWithNewUsers(tick, usersToLoad);
				}

				writeOutput(writer);
			}

			writeLastLine(writer);
			writer.close();
		} catch (Exception e) {
			System.out.println("Exception on loadBalance=" + e.getMessage());
		}

		return rc;
	}

	/**
	 * Validates a file
	 * 
	 * @param file
	 * @return returnCode
	 */
	private int validateFile(final File file) {
		int rc = OK;
		if (!(file.exists() && !file.isDirectory())) {
			rc = INPUT_FILE_DOES_NOT_EXIST;
		}
		return rc;
	}

	/**
	 * Returns the content of a line on input file.
	 * 
	 * 
	 * @param br
	 * @param lineString
	 * @return true for valid content. false for invalid content.
	 */
	private boolean readLine(final BufferedReader br, final String[] lineString) {
		boolean success = true;
		try {
			lineString[0] = br.readLine();
		} catch (IOException e) {
			success = false;
		}
		if (success && lineString[0] != null) {
			lineString[0] = lineString[0].trim();
		} else {
			success = false;
		}
		return success;
	}

	/**
	 * log on screen an occurred error
	 * 
	 * @param returnCode
	 */
	private void logError(final int returnCode) {
		System.out.println("Error rc=" + returnCode);
	}

	/**
	 * log on screen an occurred error, indicating the corresponding tick.
	 * 
	 * @param returnCode
	 * @param tick
	 */
	private void logErrorTick(final int returnCode, final Long tick) {
		System.out.println("Error rc=" + returnCode + ", tick=" + tick);
	}

	/**
	 * Returns the parsed Long number value of a content
	 * 
	 * @param content
	 * @return
	 */
	private Long getLongNumber(final String content) {
		Long number = Long.valueOf(0);
		if (content != null) {
			try {
				number = Long.parseLong(content);
			} catch (NumberFormatException e) {
				return Long.valueOf(-1);
			}
		}
		return number;
	}

	/**
	 * Returns the parsed Long number value of a content, validating minimun and
	 * maximum value
	 * 
	 * @param content
	 * @param min
	 * @param max
	 * @return
	 */
	private int getIntNumber(final String content, final int min, final int max) {
		Integer number = Integer.valueOf(0);
		if (content != null) {
			try {
				number = Integer.parseInt(content);
				if (!(number >= min && number <= max)) {
					return 0;
				}
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return number;
	}

	/**
	 * Updates the server instances with new users to load.
	 * 
	 * @param tick
	 * @param _usersToLoad
	 */
	private void updateServersWithNewUsers(final Long tick, final Long _usersToLoad) {
		Iterator<ServerInstance> iterator = linkedListServers.iterator();
		ServerInstance serverInstance = null;
		Long usersToLoad = _usersToLoad;
		Long usersLoaded = Long.valueOf(0);
		boolean hasNext = true;
		while (usersToLoad > 0) {
			while (hasNext && iterator.hasNext() && (usersToLoad > 0)) {
				serverInstance = iterator.next();
				usersToLoad = serverInstance.addTickUser(tick, usersToLoad);
				if (!iterator.hasNext()) {
					hasNext = false;
				}
			}
			if (usersToLoad > 0) {
				usersLoaded = usersToLoad > umax ? umax : usersToLoad;
				serverInstance = new ServerInstance(tick, usersLoaded, ttask, umax);
				linkedListServers.add(serverInstance);
				usersToLoad -= usersLoaded;
			}
		}
	}

	/**
	 * Updates the server instances after a tick occurred, freeing the users if
	 * ttask ticks have occurred
	 * 
	 * @param tick
	 */
	private void updateServersAfterTick(final Long tick) {
		Iterator<ServerInstance> iterator = linkedListServers.iterator();
		ServerInstance serverInstance = null;
		while (iterator.hasNext()) {
			serverInstance = iterator.next();
			if (serverInstance.updateListAfterTick(tick) == 0) {
				iterator.remove();
			}
			totalCost++;
		}
	}

	/**
	 * Writes content on the output file
	 * 
	 * @param writer
	 */
	private void writeOutput(final PrintWriter writer) {
		Iterator<ServerInstance> iterator = linkedListServers.iterator();
		StringBuilder sb = new StringBuilder();
		ServerInstance serverInstance = null;
		while (iterator.hasNext()) {
			serverInstance = iterator.next();
			sb.append(sb.length() > 0 ? "," + serverInstance.getTotalUsers() : serverInstance.getTotalUsers());
		}
		String output = sb.length() > 0 ? sb.toString() : "0";
		writer.println(output);
	}

	/**
	 * Writes the last line of the output file
	 * 
	 * @param writer
	 */
	private void writeLastLine(final PrintWriter writer) {
		writer.println(String.valueOf(totalCost));
	}

}
