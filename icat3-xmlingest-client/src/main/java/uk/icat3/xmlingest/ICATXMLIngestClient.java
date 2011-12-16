package uk.icat3.xmlingest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;

public class ICATXMLIngestClient {

	public static List<Long> ingestInvestigation(String user, String password, String xml, Properties properties) {

		ICAT icatPort = null;
		List<Long> ids = null;

		try {
			final String urlString = properties.getProperty("icat.url");
			if (urlString == null) {
				System.err.println("icat.url property not set in icatclient.properties");
				System.exit(1);
			}
			final URL icatURL = new URL(properties.getProperty("icat.url") + "/ICATService/ICAT?wsdl");
			icatPort = new ICATService(icatURL, new QName("client.icat3.uk", "ICATService")).getICATPort();

			System.out.println("Logging in...");
			final String sessionId = icatPort.login(user, password);

			System.out.println("SessionId = " + sessionId);

			System.out.println("Ingesting metadata...");
			ids = icatPort.ingestMetadata(sessionId, xml);

			System.out.println("Logging out...");
			icatPort.logout(sessionId);

			return ids;
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws IOException {

		if (args == null || args.length < 3) {
			System.err.println("[ERROR] - Incorrect number of arguments supplied");
			printHelp();
			System.exit(-1);
		}

		final String username = args[0];
		final String password = args[1];

		final String buffer = readXMLFile(args[2]);

		final InputStream fis = ICATXMLIngestClient.class.getResourceAsStream("/icatclient.properties");
		final Properties properties = new Properties();
		properties.load(fis);

		final List<Long> ids = ingestInvestigation(username, password, buffer, properties);

		for (final Long id : ids) {
			System.out.println("Returned : " + id);
		}
	}

	public static void printHelp() {
		System.out.println();
		System.out.println("Usage: java -jar ICATIngestClient.jar [username] [password] [filename]");
		System.out.println("e.g.   java -jar ICATIngestClient.jar fred secret /tmp/ASTRA00001.XML");
	}

	public static String readXMLFile(String _filename) {
		final StringBuffer _buffer = new StringBuffer();
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_filename)));
			String line = "";
			while ((line = br.readLine()) != null) {
				_buffer.append(line);
			}// end while
		} catch (final Exception e) {
			System.err.println("[ERROR] - Supplied filename does not exist or cannot be read");
			printHelp();
			System.exit(-1);
		}// end try/catch

		return _buffer.toString();
	}
}
