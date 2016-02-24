package org.icatproject.integration;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonString;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.icatproject.AccessType;
import org.icatproject.Application;
import org.icatproject.DataCollection;
import org.icatproject.DataCollectionDatafile;
import org.icatproject.DataCollectionDataset;
import org.icatproject.Datafile;
import org.icatproject.DatafileFormat;
import org.icatproject.Dataset;
import org.icatproject.DatasetParameter;
import org.icatproject.DatasetType;
import org.icatproject.EntityBaseBean;
import org.icatproject.EntityInfo;
import org.icatproject.Facility;
import org.icatproject.Grouping;
import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.icatproject.IcatExceptionType;
import org.icatproject.IcatException_Exception;
import org.icatproject.Instrument;
import org.icatproject.InstrumentScientist;
import org.icatproject.Investigation;
import org.icatproject.InvestigationGroup;
import org.icatproject.InvestigationInstrument;
import org.icatproject.InvestigationType;
import org.icatproject.InvestigationUser;
import org.icatproject.Job;
import org.icatproject.Login.Credentials;
import org.icatproject.Login.Credentials.Entry;
import org.icatproject.ParameterType;
import org.icatproject.ParameterValueType;
import org.icatproject.Rule;
import org.icatproject.Study;
import org.icatproject.StudyInvestigation;
import org.icatproject.User;
import org.icatproject.UserGroup;
import org.icatproject.utils.ContainerGetter;
import org.icatproject.utils.ContainerGetter.ContainerType;

public class WSession {
	public enum ParameterApplicability {
		DATASET, DATAFILE, SAMPLE, INVESTIGATION
	};

	private final ICAT icat;

	private final String sessionId;

	private String rootsessionId;

	private ContainerType containerType;

	private static String[] suffices = new String[] { "ICATService/ICAT?wsdl", "icat/ICAT?wsdl" };

	public WSession() throws Exception {
		String url = System.getProperty("serverUrl");
		System.out.println("Using ICAT service at " + url);
		ICATService icatService = null;
		for (String suffix : suffices) {
			URL icatUrl = new URL(url + "/" + suffix);

			try {
				icatService = new ICATService(icatUrl, new QName("http://icatproject.org", "ICATService"));
			} catch (WebServiceException e) {
				Throwable cause = e.getCause();
				if (cause != null && cause.getMessage().contains("security")) {
					throw e;
				}
			}
		}
		if (icatService == null) {
			throw new Exception("Unable to connect to: " + url);
		}
		this.icat = icatService.getICATPort();
		this.rootsessionId = login("db", "username", "root", "password", "password");
		this.sessionId = login("db", "username", "notroot", "password", "password");

		String json = (new org.icatproject.icat.client.ICAT(System.getProperty("serverUrl"))).getProperties();
		containerType = ContainerGetter.ContainerType
				.valueOf(((JsonString) Json.createReader(new ByteArrayInputStream(json.getBytes())).readObject()
						.get("containerType")).getString());

		System.out.println("Logged in to " + containerType + " container");
	}

	private WSession(ICAT icat, String plugin, String[] credbits) throws IcatException_Exception {
		this.icat = icat;
		this.sessionId = login(plugin, credbits);
		System.out.println("Logged in");
	}

	public WSession getSession(String plugin, String... credbits) throws IcatException_Exception {
		return new WSession(icat, plugin, credbits);
	}

	private String login(String plugin, String... credbits) throws IcatException_Exception {
		Credentials credentials = new Credentials();
		List<Entry> entries = credentials.getEntry();
		int i = 0;
		while (i < credbits.length) {
			Entry e = new Entry();
			e.setKey(credbits[i]);
			e.setValue(credbits[i + 1]);
			entries.add(e);
			i += 2;
		}
		return this.icat.login(plugin, credentials);
	}

	public void addRule(String groupName, String what, String crudFlags) throws Exception {
		Rule rule = new Rule();
		if (groupName != null) {
			Grouping g = (Grouping) icat.search(rootsessionId, "Grouping [name= '" + groupName + "']").get(0);
			rule.setGrouping(g);
		}
		rule.setWhat(what);
		rule.setCrudFlags(crudFlags);
		this.icat.create(this.rootsessionId, rule);
	}

	public void delRule(String groupName, String what, String crudFlags) throws Exception {
		List<Object> rules = null;

		what = what.replace("'", "''");
		if (groupName == null) {
			rules = search("select r FROM Rule r WHERE r.what = '" + what + "' AND r.crudFlags = '" + crudFlags
					+ "' AND r.grouping IS NULL");
		} else {
			rules = search("Rule [what = '" + what + "' and crudFlags = '" + crudFlags + "'] <-> Grouping [name= '"
					+ groupName + "']");
		}

		if (rules.size() == 1) {
			delete((EntityBaseBean) rules.get(0));
		} else {
			throw new Exception(rules.size() + " rules match " + groupName + ", " + what + ", " + crudFlags);
		}
	}

	public void addUserGroupMember(String groupName, String userName) throws Exception {
		Grouping grouping = null;
		if (groupName != null) {
			List<Object> groupings = icat.search(rootsessionId, "Grouping [name= '" + groupName + "']");
			if (groupings.isEmpty()) {
				grouping = new Grouping();
				grouping.setName(groupName);
				grouping.setId(icat.create(rootsessionId, grouping));
			} else {
				grouping = (Grouping) groupings.get(0);
			}
		}
		User user = null;
		if (userName != null) {
			List<Object> users = icat.search(rootsessionId, "User [name= '" + userName + "']");
			if (users.isEmpty()) {
				user = new User();
				user.setName(userName);
				user.setId(icat.create(rootsessionId, user));
			} else {
				user = (User) users.get(0);
			}
		}

		if (user != null && grouping != null) {
			UserGroup userGroup = new UserGroup();
			userGroup.setUser(user);
			userGroup.setGrouping(grouping);
			this.icat.create(rootsessionId, userGroup);
		}
	}

	public void clear() throws Exception {
		deleteAll(Arrays.asList("Facility", "DataCollection", "Study"));
	}

	private void deleteAll(List<String> names) throws IcatException_Exception {
		List<EntityBaseBean> toDelete = new ArrayList<EntityBaseBean>();
		StringBuilder sb = new StringBuilder();
		for (String type : names) {
			List<Object> lo = this.search(type);
			if (lo.size() > 0) {
				if (sb.length() == 0) {
					sb.append("Will delete");
				} else {
					sb.append(" and");
				}
				sb.append(" " + lo.size() + " object" + (lo.size() == 1 ? "" : "s") + " of type " + type);
				for (Object o : lo) {
					toDelete.add((EntityBaseBean) o);
				}
			}
		}
		if (sb.length() != 0) {
			System.out.println(sb);
		}
		icat.deleteMany(rootsessionId, toDelete);
	}

	public void clearAuthz() throws Exception {
		deleteAll(Arrays.asList("Rule", "UserGroup", "User", "Grouping", "PublicStep"));
	}

	public Application createApplication(Facility facility, String name, String version)
			throws IcatException_Exception {
		final Application application = new Application();
		application.setFacility(facility);
		application.setName(name);
		application.setVersion(version);
		application.setId(this.icat.create(this.sessionId, application));
		return application;
	}

	public Datafile createDatafile(String name, DatafileFormat format, Dataset ds, Long fileSize)
			throws IcatException_Exception {
		final Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		datafile.setDataset(ds);
		datafile.setFileSize(fileSize);
		datafile.setId(this.icat.create(this.sessionId, datafile));
		return datafile;
	}

	public DatafileFormat createDatafileFormat(Facility facility, String name, String formatType) throws Exception {
		final DatafileFormat dff = new DatafileFormat();
		dff.setName(name);
		dff.setVersion("1");
		dff.setType(formatType);
		dff.setFacility(facility);
		dff.setId(this.icat.create(this.sessionId, dff));
		return dff;
	}

	public Dataset createDataset(String name, DatasetType type, Investigation inv) throws IcatException_Exception {
		final Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(type);
		dataset.setInvestigation(inv);
		dataset.setId(this.icat.create(this.sessionId, dataset));
		return dataset;
	}

	public DatasetParameter createDatasetParameter(Object value, ParameterType p, Dataset ds)
			throws IcatException_Exception {

		final DatasetParameter dsp = new DatasetParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			dsp.setDateTimeValue((XMLGregorianCalendar) value);
		}
		dsp.setType(p);
		dsp.setDataset(ds);
		this.icat.create(this.sessionId, dsp);
		return dsp;
	}

	public DatasetType createDatasetType(Facility facility, String name) throws Exception {
		final DatasetType dst = new DatasetType();
		dst.setName(name);
		dst.setFacility(facility);
		dst.setId(this.icat.create(this.sessionId, dst));
		return dst;
	}

	public Facility createFacility(String name, int daysUntilRelease) throws Exception {
		final Facility f = new Facility();
		f.setName(name);
		f.setDaysUntilRelease(daysUntilRelease);
		f.setId(icat.create(this.sessionId, f));
		return f;
	}

	public Investigation createInvestigation(Facility facility, String name, String title, InvestigationType invType)
			throws Exception {
		final Investigation i = new Investigation();
		i.setFacility(facility);
		i.setName(name);
		i.setTitle(title);
		i.setType(invType);
		i.setVisitId("42");
		i.setId(this.icat.create(this.sessionId, i));
		return i;
	}

	public InvestigationType createInvestigationType(Facility facility, String name) throws Exception {
		final InvestigationType type = new InvestigationType();
		type.setFacility(facility);
		type.setName(name);
		type.setId(this.icat.create(this.sessionId, type));
		return type;
	}

	public Job createJob(Application application, DataCollection input, DataCollection output)
			throws IcatException_Exception {
		final Job job = new Job();
		job.setApplication(application);
		job.setInputDataCollection(input);
		job.setOutputDataCollection(output);
		job.setId((Long) icat.create(sessionId, job));
		return job;
	}

	public void delete(EntityBaseBean bean) throws IcatException_Exception {
		this.icat.delete(this.sessionId, bean);
	}

	public EntityBaseBean get(String query, long key) throws IcatException_Exception {
		return this.icat.get(this.sessionId, query, key);
	}

	public List<Object> search(String query) throws IcatException_Exception {
		return this.icat.search(this.sessionId, query);
	}

	public void setAuthz() throws Exception {
		clearAuthz();
		try {
			this.addUserGroupMember("notroot", "db/notroot");
			this.addUserGroupMember("root", "db/root");
		} catch (IcatException_Exception e) {
			if (e.getFaultInfo().getType() == IcatExceptionType.OBJECT_ALREADY_EXISTS) {
				System.out.println("root is already a member of root - carry on");
			} else {
				throw e;
			}
		}
		this.addRule("notroot", "SELECT x FROM Rule x", "CRUD");
		this.addRule("notroot", "SELECT x FROM User x", "CRUD");
		this.addRule("notroot", "SELECT x FROM Grouping x", "CRUD");
		this.addRule("notroot", "SELECT x FROM UserGroup x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DatafileFormat x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DatasetType x", "CRUD");
		this.addRule("notroot", "SELECT x FROM Facility x", "CRUD");
		this.addRule("notroot", "SELECT x FROM Investigation x", "CRUD");
		this.addRule("notroot", "SELECT x FROM InvestigationUser x", "CRUD");
		this.addRule("notroot", "SELECT x FROM InvestigationType x", "CRUD");
		this.addRule("notroot", "SELECT x FROM ParameterType x", "CRUD");
		this.addRule("notroot", "Dataset", "CRUD");
		this.addRule("notroot", "SELECT x FROM ParameterType x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DatasetParameter x", "CRUD");
		this.addRule("notroot", "SELECT x FROM Datafile x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DatafileParameter x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DatafileFormat x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DatasetType x", "CRUD");
		this.addRule("notroot", "SELECT x FROM Application x", "CRUD");
		this.addRule("notroot", "SELECT x FROM Job x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DataCollection x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DataCollectionParameter x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DataCollectionDataset x", "CRUD");
		this.addRule("notroot", "SELECT x FROM DataCollectionDatafile x", "CRUD");
		this.addRule("notroot", "SELECT x FROM InvestigationParameter x", "CRUD");
		this.addRule("notroot", "Instrument", "CRUD");
		this.addRule("notroot", "InvestigationInstrument", "CRUD");
		this.addRule("notroot", "InstrumentScientist", "CRUD");
		this.addRule("notroot", "SampleType", "CRUD");
		this.addRule("notroot", "Sample", "CRUD");
		this.addRule("notroot", "PublicStep", "CRUD");
		this.addRule("notroot", "Study", "CRUD");
		this.addRule("notroot", "StudyInvestigation", "CRUD");
	}

	public void update(EntityBaseBean df) throws IcatException_Exception {
		this.icat.update(this.sessionId, df);
	}

	public void registerInvestigation(Investigation inv) throws IcatException_Exception {
		inv.setId(this.icat.create(this.sessionId, inv));
	}

	public EntityInfo getEntityInfo(String beanName) throws IcatException_Exception {
		return icat.getEntityInfo(beanName);
	}

	public Long create(EntityBaseBean bean) throws IcatException_Exception {
		return this.icat.create(this.sessionId, bean);
	}

	public List<Long> createMany(List<EntityBaseBean> beans) throws IcatException_Exception {
		return this.icat.createMany(this.sessionId, beans);
	}

	public double getRemainingMinutes() throws IcatException_Exception {
		return this.icat.getRemainingMinutes(this.sessionId);
	}

	public String getApiVersion() throws IcatException_Exception {
		return this.icat.getApiVersion();
	}

	public String getUserName() throws IcatException_Exception {
		return this.icat.getUserName(this.sessionId);
	}

	public void registerDatafile(Datafile datafile) throws IcatException_Exception {
		datafile.setId(this.icat.create(this.sessionId, datafile));
	}

	public void deleteMany(List<EntityBaseBean> beans) throws IcatException_Exception {
		this.icat.deleteMany(sessionId, beans);
	}

	public void refresh(String sessionId) throws IcatException_Exception {
		this.icat.refresh(sessionId);
	}

	public void refresh() throws IcatException_Exception {
		this.icat.refresh(sessionId);
	}

	public void logout() throws IcatException_Exception {
		icat.logout(sessionId);
	}

	// This assumes that the lucene.commitSeconds is set to 1 for testing
	// purposes
	public void synchLucene() throws InterruptedException {
		Thread.sleep(2000);
	}

	public DataCollection createDataCollection(EntityBaseBean... beans) throws IcatException_Exception {
		DataCollection dataCollection = new DataCollection();
		for (EntityBaseBean bean : beans) {
			if (bean instanceof Datafile) {
				DataCollectionDatafile dataCollectionDatafile = new DataCollectionDatafile();
				dataCollectionDatafile.setDatafile((Datafile) bean);
				dataCollection.getDataCollectionDatafiles().add(dataCollectionDatafile);
			} else if (bean instanceof Dataset) {
				DataCollectionDataset dataCollectionDataset = new DataCollectionDataset();
				dataCollectionDataset.setDataset((Dataset) bean);
				dataCollection.getDataCollectionDatasets().add(dataCollectionDataset);
			} else {
				throw new IllegalArgumentException(bean + " must be a Dataset or a Datafile");
			}
		}
		dataCollection.setId(this.icat.create(this.sessionId, dataCollection));
		return dataCollection;
	}

	public List<String> getProperties() throws IcatException_Exception {
		return icat.getProperties(rootsessionId);
	}

	public Instrument createInstrument(Facility facility, String name) throws IcatException_Exception {
		Instrument ins = new Instrument();
		ins.setFacility(facility);
		ins.setName(name);
		ins.setId(icat.create(sessionId, ins));
		return ins;
	}

	public InvestigationInstrument createInvestigationInstrument(Investigation inv, Instrument ins)
			throws IcatException_Exception {
		InvestigationInstrument ii = new InvestigationInstrument();
		ii.setInvestigation(inv);
		ii.setInstrument(ins);
		ii.setId(icat.create(sessionId, ii));
		return ii;
	}

	public InstrumentScientist createInstrumentScientist(Instrument ins, User user) throws IcatException_Exception {
		InstrumentScientist is = new InstrumentScientist();
		is.setInstrument(ins);
		is.setUser(user);
		is.setId(icat.create(sessionId, is));
		return is;

	}

	public InvestigationUser createInvestigationUser(Investigation inv, User user, String role)
			throws IcatException_Exception {
		InvestigationUser iu = new InvestigationUser();
		iu.setInvestigation(inv);
		iu.setUser(user);
		iu.setRole(role);
		iu.setId(icat.create(sessionId, iu));
		return iu;
	}

	public boolean isAccessAllowed(EntityBaseBean bean, AccessType aType) throws IcatException_Exception {
		return icat.isAccessAllowed(sessionId, bean, aType);
	}

	public List<String> getEntityNames() throws IcatException_Exception {
		return icat.getEntityNames();
	}

	public void createInvestigationGroup(Investigation inv, String groupName, String role)
			throws IcatException_Exception {
		List<Object> groupings = icat.search(rootsessionId, "Grouping [name= '" + groupName + "']");

		Grouping grouping = (Grouping) groupings.get(0);
		InvestigationGroup ig = new InvestigationGroup();
		ig.setInvestigation(inv);
		ig.setGrouping(grouping);
		ig.setRole(role);
		icat.create(sessionId, ig);
	}

	public ContainerType getContainerType() {
		return containerType;
	}

	public Study createStudy(String name) throws IcatException_Exception {
		Study s = new Study();
		s.setName(name);
		s.setId(icat.create(sessionId, s));
		return s;
	}

	public void createStudyInvestigation(Study study, Investigation inv) throws IcatException_Exception {
		StudyInvestigation si = new StudyInvestigation();
		si.setStudy(study);
		si.setInvestigation(inv);
		icat.create(sessionId, si);

	}

}