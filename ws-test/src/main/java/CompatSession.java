import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import uk.icat3.client.ICATCompat;
import uk.icat3.client.ICATCompatService;
import uk.icat3.client.Investigation;
import uk.icat3.client.SessionException_Exception;

public class CompatSession {

	private final ICATCompat icatEP;
	private final String sessionId;

	public CompatSession(String sessionId) throws MalformedURLException {
		String host = System.getProperty("serverHost");
		String port = System.getProperty("serverPort");
		final String urlString = "https://" + host + ":" + port;
		final URL icatCompatUrl = new URL(urlString + "/ICATCompatService/ICATCompat?wsdl");
		final ICATCompatService icatCompatService = new ICATCompatService(icatCompatUrl, new QName(
				"client.icat3.uk", "ICATCompatService"));
		this.icatEP = icatCompatService.getICATCompatPort();
		this.sessionId = sessionId;
	}

	public List<Investigation> getMyInvestigations() throws SessionException_Exception  {
		return  this.icatEP.getMyInvestigations(this.sessionId);
	}



}