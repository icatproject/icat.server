package uk.icat3.xmlingest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;

/**
 * 
 * @author df01
 */
public class ICATXMLIngestClient {

	public static void printHelp() {
		System.out.println();
		System.out.println("Usage: java -jar ICATIngestClient.jar [username] [password] [filename]");
		System.out.println("e.g.   java -jar ICATIngestClient.jar fred secret /tmp/ASTRA00001.XML");
	}

	public static String readXMLFile(String _filename) {
		StringBuffer _buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_filename)));
			String line = "";
			while ((line = br.readLine()) != null) {
				_buffer.append(line);
			}// end while
		} catch (Exception e) {
			System.err.println("[ERROR] - Supplied filename does not exist or cannot be read");
			printHelp();
			System.exit(-1);
		}// end try/catch

		return _buffer.toString();
	}


	public static void main(String[] args) throws IOException {

		if ((args == null) || (args.length < 3)) {
			System.err.println("[ERROR] - Incorrect number of arguments supplied");
			printHelp();
			System.exit(-1);
		}

		String username = args[0];
		String password = args[1];

		String buffer = readXMLFile(args[2]);

		InputStream fis = ICATXMLIngestClient.class.getResourceAsStream("/icatclient.properties");
		Properties properties = new Properties();
		properties.load(fis);

		List<Long> ids = ingestInvestigation(username, password, buffer, properties);

		for (Long id : ids)
			System.out.println("Returned : " + id);
	}

	public static List<Long> ingestInvestigation(String user, String password, String xml, Properties properties) {

		ICAT icatPort = null;
		List<Long> ids = null;

		try {
			String urlString = properties.getProperty("icat.url");
			if (urlString == null) {
				System.err.println("icat.url property not set in icatclient.properties");
				System.exit(1);
			}
			URL icatURL = new URL(properties.getProperty("icat.url") + "/ICATService/ICAT?wsdl");
			icatPort = new ICATService(icatURL, new QName("client.icat3.uk", "ICATService")).getICATPort();

			System.out.println("Logging in...");
			String sessionId = icatPort.login(user, password);

			System.out.println("SessionId = " + sessionId);

			// ingest here
			System.out.println("Ingesting metadata...");
			ids = icatPort.ingestMetadata(sessionId, xml);

			System.out.println("Logging out...");
			icatPort.logout(sessionId);

			return ids;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
