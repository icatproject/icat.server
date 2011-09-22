/**
 * Base class for examples
 * 
 * @author Mariusz Balawajder
 * 
 * $Date: 2011-08-08 17:23:28 +0100 (Mon, 08 Aug 2011) $
 * $Revision: 932 $
 * $LastChangedBy: abm65@FED.CCLRC.AC.UK $
 *  
 * $Id: ExampleBase.java 932 2011-08-08 16:23:28Z abm65@FED.CCLRC.AC.UK $
 */
package uk.icat.examples;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;
import uk.icat3.client.ICATServiceLocator;

public class ExampleBase {

	private static final String KEYWORD_SEPARATOR = ",";
	private static final String WSDL_LOCATION_PROPERTY_NAME = "wsdl.location";
	private static final String USERNAME_PROPERTY_NAME = "username";
	private static final String EXAMPLE_PROPERTIES_FILENAME = "example.properties";
	private static final String PASSWORD_PROPERTY_NAME = "password";
	private static final String KEYWORDS_PROPERTY_NAME = "keywords";
	private static final String INVESTIGATION_PROPERTY_NAME = "investigationId";
	private static final String DATASET_PROPERTY_NAME = "datasetId";
	private static final String DATAFILE_PROPERTY_NAME = "datafileId";

	protected static String username;
	private static String wsdlLocation;
	protected static String password;
	protected static List<String> keywordsList;
	protected static long investigationId;
	protected static long datasetId;
	protected static long datafileId;

	public static ICAT getIcat() throws Exception {
		readConfigurationFile();
		ICATService service = new ICATServiceLocator(wsdlLocation, new QName("client.icat3.uk", "ICATService"));
		return service.getICATPort();
	}

	private static void readConfigurationFile() throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream(EXAMPLE_PROPERTIES_FILENAME));

		username = (String) properties.get(USERNAME_PROPERTY_NAME);
		password = (String) properties.get(PASSWORD_PROPERTY_NAME);
		wsdlLocation = (String) properties.get(WSDL_LOCATION_PROPERTY_NAME);
		keywordsList = Arrays.asList(((String) properties.get(KEYWORDS_PROPERTY_NAME)).split(KEYWORD_SEPARATOR));
		investigationId = Long.valueOf((String) properties.get(INVESTIGATION_PROPERTY_NAME));
		datasetId = Long.valueOf((String) properties.get(DATASET_PROPERTY_NAME));
		datafileId = Long.valueOf((String) properties.get(DATAFILE_PROPERTY_NAME));
	}


}
