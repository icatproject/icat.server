import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import uk.icat3.client.Application;
import uk.icat3.client.BadParameterException_Exception;
import uk.icat3.client.Datafile;
import uk.icat3.client.DatafileFormat;
import uk.icat3.client.Dataset;
import uk.icat3.client.DatasetParameter;
import uk.icat3.client.DatasetType;
import uk.icat3.client.DestType;
import uk.icat3.client.EntityBaseBean;
import uk.icat3.client.Facility;
import uk.icat3.client.Group;
import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;
import uk.icat3.client.IcatInternalException_Exception;
import uk.icat3.client.InputDatafile;
import uk.icat3.client.InputDataset;
import uk.icat3.client.InsufficientPrivilegesException_Exception;
import uk.icat3.client.Investigation;
import uk.icat3.client.InvestigationType;
import uk.icat3.client.Job;
import uk.icat3.client.NoSuchObjectFoundException_Exception;
import uk.icat3.client.NotificationRequest;
import uk.icat3.client.ObjectAlreadyExistsException_Exception;
import uk.icat3.client.OutputDatafile;
import uk.icat3.client.OutputDataset;
import uk.icat3.client.ParameterType;
import uk.icat3.client.ParameterValueType;
import uk.icat3.client.Rule;
import uk.icat3.client.SessionException_Exception;
import uk.icat3.client.User;
import uk.icat3.client.UserGroup;
import uk.icat3.client.ValidationException_Exception;

class Session {
	public enum ParameterApplicability {
		DATASET, DATAFILE, SAMPLE, INVESTIGATION
	};

	private final ICAT icatEP;
	private final String sessionId;

	public Session() throws MalformedURLException, SessionException_Exception, IcatInternalException_Exception {
		final String urlString = "http://localhost:8080";
		final URL icatUrl = new URL(urlString + "/ICATService/ICAT?wsdl");
		final ICATService icatService = new ICATService(icatUrl, new QName("client.icat3.uk", "ICATService"));
		this.icatEP = icatService.getICATPort();
		this.sessionId = this.icatEP.login("root", "password");
	}

	public void addInputDatafile(Job job, Datafile df) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final InputDatafile idf = new InputDatafile();
		idf.setDatafile(df);
		idf.setJob(job);
		this.icatEP.create(this.sessionId, idf);
	}

	public void addInputDataset(Job job, Dataset ds) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final InputDataset ids = new InputDataset();
		ids.setDataset(ds);
		ids.setJob(job);
		this.icatEP.create(this.sessionId, ids);
	}

	public void addOutputDatafile(Job job, Datafile df) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final OutputDatafile odf = new OutputDatafile();
		odf.setDatafile(df);
		odf.setJob(job);
		this.icatEP.create(this.sessionId, odf);
	}

	public void addOutputDataset(Job job, Dataset ds) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final OutputDataset ods = new OutputDataset();
		ods.setDataset(ds);
		ods.setJob(job);
		this.icatEP.create(this.sessionId, ods);
	}

	public void addRule(String groupName, String what, String crudFlags) throws Exception {
		Rule rule = new Rule();
		if (groupName != null) {
			Group g = (Group) this.icatEP.get(this.sessionId, "Group", groupName);
			rule.setGroup(g);
		}
		rule.setWhat(what);
		rule.setCrudFlags(crudFlags);
		this.icatEP.create(this.sessionId, rule);
	}

	public void addUserGroupMember(String groupName, String userName) throws Exception {
		Group group = null;
		if (groupName != null) {
			try {
				group = (Group) this.icatEP.get(this.sessionId, "Group", groupName);

			} catch (NoSuchObjectFoundException_Exception e) {
				group = new Group();
				group.setName(groupName);
				this.icatEP.create(sessionId, group);
			}
		}
		User user = null;
		try {
			user = (User) this.icatEP.get(this.sessionId, "User", userName);
		} catch (NoSuchObjectFoundException_Exception e) {
			user = new User();
			user.setName(userName);
			this.icatEP.create(sessionId, user);
		}
		UserGroup userGroup = new UserGroup();
		userGroup.setUser(user);
		userGroup.setGroup(group);
		this.icatEP.create(sessionId, userGroup);
	}

	public void clear() throws Exception {
		List<Object> lo = this.search("Job");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((Job) o);
		}
		lo = this.search("Application");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((Application) o);
		}
		lo = this.search("Investigation");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((Investigation) o);
		}
		lo = this.search("DatasetType");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((DatasetType) o);
		}
		lo = this.search("DatafileFormat");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((DatafileFormat) o);
		}
		lo = this.search("InvestigationType");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((InvestigationType) o);
		}
		lo = this.search("ParameterType");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((ParameterType) o);
		}
		lo = this.search("Facility");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((Facility) o);
		}
		lo = this.search("NotificationRequest");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((NotificationRequest) o);
		}
	}

	public void clearAuthz() throws Exception {

		List<Object> lo1 = this.search("Rule");
		List<Object> lo2 = this.search("UserGroup");
		List<Object> lo3 = this.search("User");
		List<Object> lo4 = this.search("Group");
		for (final Object o : lo1) {
			System.out.println("Deleting " + o);
			this.delete((Rule) o);
		}
		for (final Object o : lo2) {
			System.out.println("Deleting " + o);
			this.delete((UserGroup) o);
		}
		for (final Object o : lo3) {
			System.out.println("Deleting " + o);
			this.delete((User) o);
		}
		for (final Object o : lo4) {
			System.out.println("Deleting " + o);
			this.delete((Group) o);
		}
	}

	public Application createApplication(String name, String version) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final Application application = new Application();
		application.setName(name);
		application.setVersion(version);
		application.setId((Long) this.icatEP.create(this.sessionId, application));
		return application;
	}

	public Datafile createDatafile(String name, DatafileFormat format, Dataset ds)
			throws IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, ObjectAlreadyExistsException_Exception, SessionException_Exception,
			ValidationException_Exception {
		final Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		datafile.setDataset(ds);
		datafile.setId((Long) this.icatEP.create(this.sessionId, datafile));
		return datafile;
	}

	public DatafileFormat createDatafileFormat(String name, String formatType) throws Exception {
		final DatafileFormat dff = new DatafileFormat();
		dff.setName(name);
		dff.setVersion("1");
		dff.setFormatType(formatType);
		dff.setId((Long) this.icatEP.create(this.sessionId, dff));
		return dff;
	}

	public Dataset createDataset(String name, DatasetType type, Investigation inv)
			throws IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, ObjectAlreadyExistsException_Exception, SessionException_Exception,
			ValidationException_Exception {
		final Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(type);
		dataset.setInvestigation(inv);
		dataset.setId((Long) this.icatEP.create(this.sessionId, dataset));
		return dataset;
	}

	public DatasetParameter createDatasetParameter(Object value, ParameterType p, Dataset ds)
			throws IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, ObjectAlreadyExistsException_Exception, SessionException_Exception,
			ValidationException_Exception {

		final DatasetParameter dsp = new DatasetParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			dsp.setDateTimeValue((XMLGregorianCalendar) value);
		}
		dsp.setParameterType(p);
		dsp.setDataset(ds);
		this.icatEP.create(this.sessionId, dsp);
		return dsp;
	}

	public DatasetType createDatasetType(String name) throws Exception {
		final DatasetType dst = new DatasetType();
		dst.setName(name);
		this.icatEP.create(this.sessionId, dst);
		return dst;
	}

	public Facility createFacility(String shortName, int daysUntilRelease) throws Exception {
		final Facility f = new Facility();
		f.setName(shortName);
		f.setDaysUntilRelease(daysUntilRelease);
		this.icatEP.create(this.sessionId, f);
		return f;
	}

	public Investigation createInvestigation(Facility facility, String invNumber, String title,
			InvestigationType invType) throws Exception {
		final Investigation i = new Investigation();
		i.setFacility(facility);
		i.setName(invNumber);
		i.setTitle(title);
		i.setType(invType);
		i.setId((Long) this.icatEP.create(this.sessionId, i));
		return i;
	}

	public InvestigationType createInvestigationType(String name) throws Exception {
		final InvestigationType type = new InvestigationType();
		type.setName(name);
		this.icatEP.create(this.sessionId, type);
		return type;
	}

	public Job createJob(Application application) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final Job job = new Job();
		job.setApplication(application);
		job.setId((Long) this.icatEP.create(this.sessionId, job));
		return job;
	}

	public ParameterType createParameterType(String name, String units, String description, ParameterApplicability pt,
			ParameterValueType pvt) throws IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, ObjectAlreadyExistsException_Exception, SessionException_Exception,
			ValidationException_Exception {

		final ParameterType p = new ParameterType();
		p.setName(name);
		p.setUnits(units);
		p.setDescription(description);
		if (pt == ParameterApplicability.DATASET) {
			p.setApplicableToDataset(true);
		} else if (pt == ParameterApplicability.DATAFILE) {
			p.setApplicableToDatafile(true);
		} else if (pt == ParameterApplicability.SAMPLE) {
			p.setApplicableToSample(true);
		} else if (pt == ParameterApplicability.INVESTIGATION) {
			p.setApplicableToInvestigation(true);
		}
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		p.setId((Long) this.icatEP.create(this.sessionId, p));
		return p;
	}

	public void delete(EntityBaseBean bean) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		this.icatEP.delete(this.sessionId, bean);
	}

	public EntityBaseBean get(String query, Object key) throws BadParameterException_Exception,
			IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, SessionException_Exception {
		return this.icatEP.get(this.sessionId, query, key);
	}

	public List<Object> search(String query) throws BadParameterException_Exception, IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, SessionException_Exception {
		return this.icatEP.search(this.sessionId, query);
	}

	public void setAuthz() throws Exception {
		this.addUserGroupMember("root", "root");
		this.addRule("root", "Rule", "CRUD");
		this.addRule("root", "User", "CRUD");
		this.addRule("root", "Group", "CRUD");
		this.addRule("root", "UserGroup", "CRUD");
		this.addRule("root", "DatafileFormat", "CRUD");
		this.addRule("root", "DatasetType", "CRUD");
		this.addRule("root", "Facility", "CRUD");
		this.addRule("root", "Investigation", "CRUD");
		this.addRule("root", "InvestigationType", "CRUD");
		this.addRule("root", "ParameterType", "CRUD");
		this.addRule("root", "Investigation", "CRUD");
		this.addRule("root", "Dataset", "CRUD");
		this.addRule("root", "ParameterType", "CRUD");
		this.addRule("root", "DatasetParameter", "CRUD");
		this.addRule("root", "Datafile", "CRUD");
		this.addRule("root", "DatafileFormat", "CRUD");
		this.addRule("root", "DatasetType", "CRUD");
		this.addRule("root", "Application", "CRUD");
		this.addRule("root", "Job", "CRUD");
		this.addRule("root", "InputDataset", "CRUD");
		this.addRule("root", "OutputDataset", "CRUD");
		this.addRule("root", "InputDatafile", "CRUD");
		this.addRule("root", "OutputDatafile", "CRUD");
		this.addRule("root", "NotificationRequest", "CRUD");
	}

	public void update(EntityBaseBean df) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			SessionException_Exception, ValidationException_Exception {
		this.icatEP.update(this.sessionId, df);
	}

	public NotificationRequest createNotificationRequest(String name, DestType destType, String what, String crudFlags,
			String jmsOptions, String dataTypes) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setName(name);
		notificationRequest.setDestType(destType);
		notificationRequest.setWhat(what);
		notificationRequest.setCrudFlags(crudFlags);
		notificationRequest.setJmsOptions(jmsOptions);
		notificationRequest.setDatatypes(dataTypes);
		icatEP.create(this.sessionId, notificationRequest);
		return notificationRequest;
	}

	public Dataset addDataset(Investigation inv, String name, DatasetType type) {
		Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(type);
		inv.getDatasets().add(dataset);
		return dataset;
	}

	public Datafile addDatafile(Dataset dataset, String name, DatafileFormat format) {
		Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		dataset.getDatafiles().add(datafile);
		return datafile;
	}

	public DatasetParameter addDatasetParameter(Dataset dataset, Object o, ParameterType p) {
		DatasetParameter dsp = new DatasetParameter();
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			dsp.setDateTimeValue((XMLGregorianCalendar) o);
		}
		dsp.setParameterType(p);
		dataset.getDatasetParameters().add(dsp);
		return dsp;
	}

	public void registerInvestigation(Investigation inv) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		inv.setId((Long) this.icatEP.create(this.sessionId, inv));
	}

}