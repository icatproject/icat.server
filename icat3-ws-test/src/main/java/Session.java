import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import uk.icat3.client.DatafileFormat;
import uk.icat3.client.DatafileFormatPK;
import uk.icat3.client.DatasetType;
import uk.icat3.client.Facility;
import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;
import uk.icat3.client.Investigation;
import uk.icat3.client.InvestigationType;
import uk.icat3.client.SessionException_Exception;

class Session {
	private ICAT icatEP;
	private String sessionId;

	public void createDatasetType(String name) throws Exception {
		DatasetType dst = new DatasetType();
		dst.setName(name);
		this.icatEP.create(this.sessionId, dst);
	}

	public void createDatafileFormat(String name, String formatType) throws Exception {
		DatafileFormatPK dffpk = new DatafileFormatPK();
		dffpk.setName(name);
		dffpk.setVersion("1");
		DatafileFormat dff = new DatafileFormat();
		dff.setDatafileFormatPK(dffpk);
		dff.setFormatType(formatType);
		this.icatEP.create(this.sessionId, dff);
	}

	public void createFacility(String shortName, long daysUntilRelease) throws Exception {
		Facility f = new Facility();
		f.setFacilityShortName(shortName);
		f.setDaysUntilRelease(daysUntilRelease);
		this.icatEP.create(this.sessionId, f);
	}

	public void createInvestigation(String facility, String invNumber, String title, String invType) throws Exception {
		Investigation i = new Investigation();
		i.setFacility(facility);
		i.setInvNumber(invNumber);
		i.setTitle(title);
		i.setInvType(invType);
		this.icatEP.create(this.sessionId, i);

	}

	public void createInvestigationType(String name) throws Exception {
		InvestigationType type = new InvestigationType();
		type.setName(name);
		this.icatEP.create(this.sessionId, type);
	}

	public Session() throws MalformedURLException, SessionException_Exception {
		String urlString = ("http://localhost:8080");
		URL icatUrl = new URL(urlString + "/ICATService/ICAT?wsdl");
		ICATService icatService = new ICATService(icatUrl, new QName("client.icat3.uk", "ICATService"));
		this.icatEP = icatService.getICATPort();
		this.sessionId = this.icatEP.login("root", "password");
	}

}