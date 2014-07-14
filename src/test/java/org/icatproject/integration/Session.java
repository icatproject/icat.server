package org.icatproject.integration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

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
import org.icatproject.InvestigationInstrument;
import org.icatproject.InvestigationType;
import org.icatproject.InvestigationUser;
import org.icatproject.Job;
import org.icatproject.Login.Credentials;
import org.icatproject.Login.Credentials.Entry;
import org.icatproject.ParameterType;
import org.icatproject.ParameterValueType;
import org.icatproject.Rule;
import org.icatproject.User;
import org.icatproject.UserGroup;

public class Session {
	public enum ParameterApplicability {
		DATASET, DATAFILE, SAMPLE, INVESTIGATION
	};

	private final ICAT icat;

	public ICAT getIcat() {
		return icat;
	}

	private final String sessionId;

	public Session() throws MalformedURLException, IcatException_Exception {
		String url = System.getProperty("serverUrl");
		System.out.println("Using ICAT service at " + url);
		final URL icatUrl = new URL(url + "/ICATService/ICAT?wsdl");
		final ICATService icatService = new ICATService(icatUrl, new QName(
				"http://icatproject.org", "ICATService"));
		this.icat = icatService.getICATPort();
		this.sessionId = login("db", "username", "root", "password", "password");
		System.out.println("Logged in");
	}

	public String login(String plugin, String... credbits) throws IcatException_Exception {
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

	// TODO restore code
	// public void addInputDatafile(Job job, Datafile df) throws IcatException_Exception {
	// final InputDatafile idf = new InputDatafile();
	// idf.setDatafile(df);
	// idf.setJob(job);
	// this.icat.create(this.sessionId, idf);
	// }
	//
	// public void addInputDataset(Job job, Dataset ds) throws IcatException_Exception {
	// final InputDataset ids = new InputDataset();
	// ids.setDataset(ds);
	// ids.setJob(job);
	// this.icat.create(this.sessionId, ids);
	// }
	//
	// public void addOutputDatafile(Job job, Datafile df) throws IcatException_Exception {
	// final OutputDatafile odf = new OutputDatafile();
	// odf.setDatafile(df);
	// odf.setJob(job);
	// this.icat.create(this.sessionId, odf);
	// }
	//
	// public void addOutputDataset(Job job, Dataset ds) throws IcatException_Exception {
	// final OutputDataset ods = new OutputDataset();
	// ods.setDataset(ds);
	// ods.setJob(job);
	// this.icat.create(this.sessionId, ods);
	// }

	public void addRule(String groupName, String what, String crudFlags) throws Exception {
		Rule rule = new Rule();
		if (groupName != null) {
			Grouping g = (Grouping) search("Grouping [name= '" + groupName + "']").get(0);
			rule.setGrouping(g);
		}
		rule.setWhat(what);
		rule.setCrudFlags(crudFlags);
		this.icat.create(this.sessionId, rule);
	}

	public void delRule(String groupName, String what, String crudFlags) throws Exception {
		what = what.replace("'", "''");
		List<Object> rules = null;
		if (groupName == null) {
			rules = search("select r FROM Rule r WHERE r.what = '" + what + "' AND r.crudFlags = '"
					+ crudFlags + "' AND r.grouping IS NULL");
		} else {
			rules = search("Rule [what = '" + what + "' and crudFlags = '" + crudFlags
					+ "'] <-> Grouping [name= '" + groupName + "']");
		}
		if (rules.size() == 1) {
			delete((EntityBaseBean) rules.get(0));
		} else {
			throw new Exception(rules.size() + " rules match " + groupName + ", " + what + ", "
					+ crudFlags);
		}
	}

	public void addUserGroupMember(String groupName, String userName) throws Exception {
		Grouping group = null;
		if (groupName != null) {
			List<Object> groups = search("Grouping [name= '" + groupName + "']");
			if (groups.isEmpty()) {
				group = new Grouping();
				group.setName(groupName);
				group.setId(this.icat.create(sessionId, group));
			} else {
				group = (Grouping) groups.get(0);
			}
		}
		User user = null;
		List<Object> users = search("User [name= '" + userName + "']");
		if (users.isEmpty()) {
			user = new User();
			user.setName(userName);
			user.setId(this.icat.create(sessionId, user));
		} else {
			user = (User) users.get(0);
		}

		UserGroup userGroup = new UserGroup();
		userGroup.setUser(user);
		userGroup.setGrouping(group);
		this.icat.create(sessionId, userGroup);
	}

	public void clear() throws Exception {
		deleteAll(Arrays.asList("Facility", "Log", "DataCollection"));
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
				sb.append(" " + lo.size() + " object" + (lo.size() == 1 ? "" : "s") + " of type "
						+ type);
				for (Object o : lo) {
					toDelete.add((EntityBaseBean) o);
				}
			}
		}
		if (sb.length() != 0) {
			System.out.println(sb);
		}
		this.deleteMany(toDelete);
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

	public Datafile createDatafile(String name, DatafileFormat format, Dataset ds)
			throws IcatException_Exception {
		final Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		datafile.setDataset(ds);
		datafile.setId(this.icat.create(this.sessionId, datafile));
		return datafile;
	}

	public DatafileFormat createDatafileFormat(Facility facility, String name, String formatType)
			throws Exception {
		final DatafileFormat dff = new DatafileFormat();
		dff.setName(name);
		dff.setVersion("1");
		dff.setType(formatType);
		dff.setFacility(facility);
		dff.setId(this.icat.create(this.sessionId, dff));
		return dff;
	}

	public Dataset createDataset(String name, DatasetType type, Investigation inv)
			throws IcatException_Exception {
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

	public Facility createFacility(String shortName, int daysUntilRelease) throws Exception {
		final Facility f = new Facility();
		f.setName(shortName);
		f.setDaysUntilRelease(daysUntilRelease);
		f.setId(this.icat.create(this.sessionId, f));
		return f;
	}

	public Investigation createInvestigation(Facility facility, String name, String title,
			InvestigationType invType) throws Exception {
		final Investigation i = new Investigation();
		i.setFacility(facility);
		i.setName(name);
		i.setTitle(title);
		i.setType(invType);
		i.setVisitId("42");
		i.setId(this.icat.create(this.sessionId, i));
		return i;
	}

	public InvestigationType createInvestigationType(Facility facility, String name)
			throws Exception {
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
		job.setId((Long) this.icat.create(this.sessionId, job));
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
		try {
			this.addUserGroupMember("root", "db/root");
		} catch (IcatException_Exception e) {
			if (e.getFaultInfo().getType() == IcatExceptionType.OBJECT_ALREADY_EXISTS) {
				System.out.println("root is already a member of root - carry on");
			} else {
				throw e;
			}
		}
		this.addRule("root", "SELECT x FROM Rule x", "CRUD");
		this.addRule("root", "SELECT x FROM User x", "CRUD");
		this.addRule("root", "SELECT x FROM Grouping x", "CRUD");
		this.addRule("root", "SELECT x FROM UserGroup x", "CRUD");
		this.addRule("root", "SELECT x FROM DatafileFormat x", "CRUD");
		this.addRule("root", "SELECT x FROM DatasetType x", "CRUD");
		this.addRule("root", "SELECT x FROM Facility x", "CRUD");
		this.addRule("root", "SELECT x FROM Investigation x", "CRUD");
		this.addRule("root", "SELECT x FROM InvestigationUser x", "CRUD");
		this.addRule("root", "SELECT x FROM InvestigationType x", "CRUD");
		this.addRule("root", "SELECT x FROM ParameterType x", "CRUD");
		this.addRule("root", "Dataset", "CRUD");
		this.addRule("root", "SELECT x FROM ParameterType x", "CRUD");
		this.addRule("root", "SELECT x FROM DatasetParameter x", "CRUD");
		this.addRule("root", "SELECT x FROM Datafile x", "CRUD");
		this.addRule("root", "SELECT x FROM DatafileFormat x", "CRUD");
		this.addRule("root", "SELECT x FROM DatasetType x", "CRUD");
		this.addRule("root", "SELECT x FROM Application x", "CRUD");
		this.addRule("root", "SELECT x FROM Job x", "CRUD");
		this.addRule("root", "SELECT x FROM DataCollection x", "CRUD");
		this.addRule("root", "SELECT x FROM DataCollectionParameter x", "CRUD");
		this.addRule("root", "SELECT x FROM DataCollectionDataset x", "CRUD");
		this.addRule("root", "SELECT x FROM DataCollectionDatafile x", "CRUD");
		this.addRule("root", "SELECT x FROM InvestigationParameter x", "CRUD");
		this.addRule("root", "SELECT x FROM Log x", "CRUD");
		this.addRule("root", "Instrument", "CRUD");
		this.addRule("root", "InvestigationInstrument", "CRUD");
		this.addRule("root", "InstrumentScientist", "CRUD");
		this.addRule("root", "SampleType", "CRUD");
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

	public void logout(String sessionId) throws IcatException_Exception {
		this.icat.logout(sessionId);
	}

	public List<Object> searchText(String query, int maxCount, String entity)
			throws IcatException_Exception {
		return this.icat.searchText(sessionId, query, maxCount, entity);
	}

	// This assumes that the lucene.commitSeconds is set to 1 for testing purposes
	public void synchLucene() throws InterruptedException {
		Thread.sleep(2000);
	}

	public DataCollection createDataCollection(EntityBaseBean... beans)
			throws IcatException_Exception {
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
		return icat.getProperties(sessionId);
	}

	public void luceneClear() throws IcatException_Exception {
		icat.luceneClear(sessionId);
	}

	public void luceneCommit() throws IcatException_Exception {
		icat.luceneCommit(sessionId);
	}

	public void lucenePopulate(String entityName) throws IcatException_Exception {
		icat.lucenePopulate(sessionId, entityName);
	}

	public List<String> luceneSearch(String query, int maxCount, String entityName)
			throws IcatException_Exception {
		return icat.luceneSearch(sessionId, query, maxCount, entityName);
	}

	public Instrument createInstrument(Facility facility, String name)
			throws IcatException_Exception {
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

	public InstrumentScientist createInstrumentScientist(Instrument ins, User user)
			throws IcatException_Exception {
		InstrumentScientist is = new InstrumentScientist();
		is.setInstrument(ins);
		is.setUser(user);
		is.setId(icat.create(sessionId, is));
		return is;

	}

	public InvestigationUser createInvestigationUser(Investigation inv, User user)
			throws IcatException_Exception {
		InvestigationUser iu = new InvestigationUser();
		iu.setInvestigation(inv);
		iu.setUser(user);
		iu.setId(icat.create(sessionId, iu));
		return iu;
	}

	public List<String> luceneGetPopulating() throws IcatException_Exception {
		return icat.luceneGetPopulating(sessionId);
	}

}