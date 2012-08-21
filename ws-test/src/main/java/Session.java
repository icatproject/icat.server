import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.icatproject.Application;
import org.icatproject.Datafile;
import org.icatproject.DatafileFormat;
import org.icatproject.Dataset;
import org.icatproject.DatasetParameter;
import org.icatproject.DatasetType;
import org.icatproject.EntityBaseBean;
import org.icatproject.EntityInfo;
import org.icatproject.Facility;
import org.icatproject.Group;
import org.icatproject.ICAT;
import org.icatproject.ICATService;
import org.icatproject.IcatExceptionType;
import org.icatproject.IcatException_Exception;
import org.icatproject.InputDatafile;
import org.icatproject.InputDataset;
import org.icatproject.Investigation;
import org.icatproject.InvestigationType;
import org.icatproject.Job;
import org.icatproject.Login.Credentials;
import org.icatproject.Login.Credentials.Entry;
import org.icatproject.OutputDatafile;
import org.icatproject.OutputDataset;
import org.icatproject.ParameterType;
import org.icatproject.ParameterValueType;
import org.icatproject.Rule;
import org.icatproject.User;
import org.icatproject.UserGroup;

public class Session {
	public enum ParameterApplicability {
		DATASET, DATAFILE, SAMPLE, INVESTIGATION
	};

	public String dump(EntityBaseBean bean, int i) throws IllegalArgumentException,
			IllegalAccessException, SecurityException, NoSuchMethodException,
			InvocationTargetException {
		StringBuilder result = new StringBuilder();
		for (Field field : new ArrayList<Field>(Arrays.asList(bean.getClass().getDeclaredFields()))) {
			String name = field.getName();
			String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			Method m = bean.getClass().getMethod(getterName);
			Object o = m.invoke(bean);
			if (result.length() != 0) {
				result.append(", ");
			}
			if (o instanceof EntityBaseBean) {
				if (i > 0) {
					result.append(name + ":[" + dump((EntityBaseBean) o, i - 1) + "]");
				} else {
					result.append(name + ":...");
				}
			} else if (o instanceof List) {
				result.append(name + ":" + ((List<?>) o).size());
			} else {
				result.append(name + ": '" + o + "'");
			}
		}
		return result.toString();
	}

	private final ICAT icatEP;
	private final String sessionId;
	private CompatSession compatSession;

	public Session() throws MalformedURLException, IcatException_Exception {
		String host = System.getProperty("serverHost");
		String port = System.getProperty("serverPort");
		final String urlString = "https://" + host + ":" + port;
		final URL icatUrl = new URL(urlString + "/ICATService/ICAT?wsdl");
		final ICATService icatService = new ICATService(icatUrl, new QName(
				"http://icatproject.org", "ICATService"));
		this.icatEP = icatService.getICATPort();

		Credentials credentials = new Credentials();
		List<Entry> entries = credentials.getEntry();
		Entry e;

		e = new Entry();
		e.setKey("username");
		e.setValue("root");
		entries.add(e);

		e = new Entry();
		e.setKey("password");
		e.setValue("password");
		entries.add(e);

		this.sessionId = this.icatEP.login("db", credentials);
		this.compatSession = new CompatSession(this.sessionId);
	}

	public void addInputDatafile(Job job, Datafile df) throws IcatException_Exception {
		final InputDatafile idf = new InputDatafile();
		idf.setDatafile(df);
		idf.setJob(job);
		this.icatEP.create(this.sessionId, idf);
	}

	public void addInputDataset(Job job, Dataset ds) throws IcatException_Exception {
		final InputDataset ids = new InputDataset();
		ids.setDataset(ds);
		ids.setJob(job);
		this.icatEP.create(this.sessionId, ids);
	}

	public void addOutputDatafile(Job job, Datafile df) throws IcatException_Exception {
		final OutputDatafile odf = new OutputDatafile();
		odf.setDatafile(df);
		odf.setJob(job);
		this.icatEP.create(this.sessionId, odf);
	}

	public void addOutputDataset(Job job, Dataset ds) throws IcatException_Exception {
		final OutputDataset ods = new OutputDataset();
		ods.setDataset(ds);
		ods.setJob(job);
		this.icatEP.create(this.sessionId, ods);
	}

	public void addRule(String groupName, String what, String crudFlags) throws Exception {
		Rule rule = new Rule();
		if (groupName != null) {
			Group g = (Group) search("Group [name= '" + groupName + "']").get(0);
			rule.setGroup(g);
		}
		rule.setWhat(what);
		rule.setCrudFlags(crudFlags);
		this.icatEP.create(this.sessionId, rule);
	}

	public void delRule(String groupName, String what, String crudFlags) throws Exception {
		what = what.replace("'","''");
		List<Object> rules = search("Rule [what = '" + what
				+ "' and crudFlags = '" + crudFlags + "'] <-> Group [name= '" + groupName + "']");
		if (rules.size() == 1) {
			delete((EntityBaseBean) rules.get(0));
		} else {
			throw new Exception(rules.size() + " rules match " + groupName + ", " + what + ", "
					+ crudFlags);
		}
	}

	public void addUserGroupMember(String groupName, String userName) throws Exception {
		Group group = null;
		if (groupName != null) {
			List<Object> groups = search("Group [name= '" + groupName + "']");
			if (groups.isEmpty()) {
				group = new Group();
				group.setName(groupName);
				group.setId(this.icatEP.create(sessionId, group));
			} else {
				group = (Group) groups.get(0);
			}
		}
		User user = null;
		List<Object> users = search("User [name= '" + userName + "']");
		if (users.isEmpty()) {
			user = new User();
			user.setName(userName);
			user.setId(this.icatEP.create(sessionId, user));
		} else {
			user = (User) users.get(0);
		}

		UserGroup userGroup = new UserGroup();
		userGroup.setUser(user);
		userGroup.setGroup(group);
		this.icatEP.create(sessionId, userGroup);
	}

	public void clear() throws Exception {

		List<EntityBaseBean> toDelete = new ArrayList<EntityBaseBean>();
		for (String type : Arrays.asList("Facility", "Job", "Application", "NotificationRequest")) {
			List<Object> lo = this.search(type);
			if (lo.size() > 0) {
				System.out.println("Will delete " + lo.size() + " objects of type " + type);
				for (Object o : lo) {
					toDelete.add((EntityBaseBean) o);
				}
			}
		}
		this.deleteMany(toDelete);
	}

	public void clearAuthz() throws Exception {

		List<EntityBaseBean> toDelete = new ArrayList<EntityBaseBean>();
		for (String type : Arrays.asList("Rule", "UserGroup", "User", "Group")) {
			List<Object> lo = this.search(type);
			if (lo.size() > 0) {
				System.out.println("Will delete " + lo.size() + " objects of type " + type);
				for (Object o : lo) {
					toDelete.add((EntityBaseBean) o);
				}
			}
		}
		this.deleteMany(toDelete);
	}

	public Application createApplication(String name, String version)
			throws IcatException_Exception {
		final Application application = new Application();
		application.setName(name);
		application.setVersion(version);
		application.setId(this.icatEP.create(this.sessionId, application));
		return application;
	}

	public Datafile createDatafile(String name, DatafileFormat format, Dataset ds)
			throws IcatException_Exception {
		final Datafile datafile = new Datafile();
		datafile.setDatafileFormat(format);
		datafile.setName(name);
		datafile.setDataset(ds);
		datafile.setId(this.icatEP.create(this.sessionId, datafile));
		return datafile;
	}

	public DatafileFormat createDatafileFormat(Facility facility, String name, String formatType)
			throws Exception {
		final DatafileFormat dff = new DatafileFormat();
		dff.setName(name);
		dff.setVersion("1");
		dff.setType(formatType);
		dff.setFacility(facility);
		dff.setId(this.icatEP.create(this.sessionId, dff));
		return dff;
	}

	public Dataset createDataset(String name, DatasetType type, Investigation inv)
			throws IcatException_Exception {
		final Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setType(type);
		dataset.setInvestigation(inv);
		dataset.setId(this.icatEP.create(this.sessionId, dataset));
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
		this.icatEP.create(this.sessionId, dsp);
		return dsp;
	}

	public DatasetType createDatasetType(Facility facility, String name) throws Exception {
		final DatasetType dst = new DatasetType();
		dst.setName(name);
		dst.setFacility(facility);
		dst.setId(this.icatEP.create(this.sessionId, dst));
		return dst;
	}

	public Facility createFacility(String shortName, int daysUntilRelease) throws Exception {
		final Facility f = new Facility();
		f.setName(shortName);
		f.setDaysUntilRelease(daysUntilRelease);
		f.setId(this.icatEP.create(this.sessionId, f));
		return f;
	}

	public Investigation createInvestigation(Facility facility, String invNumber, String title,
			InvestigationType invType) throws Exception {
		final Investigation i = new Investigation();
		i.setFacility(facility);
		i.setName(invNumber);
		i.setTitle(title);
		i.setType(invType);
		i.setId(this.icatEP.create(this.sessionId, i));
		return i;
	}

	public InvestigationType createInvestigationType(Facility facility, String name)
			throws Exception {
		final InvestigationType type = new InvestigationType();
		type.setFacility(facility);
		type.setName(name);
		type.setId(this.icatEP.create(this.sessionId, type));
		return type;
	}

	public Job createJob(Application application) throws IcatException_Exception {
		final Job job = new Job();
		job.setApplication(application);
		job.setId((Long) this.icatEP.create(this.sessionId, job));
		return job;
	}

	public void delete(EntityBaseBean bean) throws IcatException_Exception {
		this.icatEP.delete(this.sessionId, bean);
	}

	public EntityBaseBean get(String query, long key) throws IcatException_Exception {
		return this.icatEP.get(this.sessionId, query, key);
	}

	public List<Object> search(String query) throws IcatException_Exception {
		return this.icatEP.search(this.sessionId, query);
	}

	public void setAuthz() throws Exception {
		try {
			this.addUserGroupMember("root", "root");
		} catch (IcatException_Exception e) {
			if (e.getFaultInfo().getType() == IcatExceptionType.OBJECT_ALREADY_EXISTS) {
				System.out.println("root is already a member of root - carry on");
			} else {
				throw e;
			}
		}
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
		this.addRule("root", "InvestigationParameter", "CRUD");
	}

	public void update(EntityBaseBean df) throws IcatException_Exception {
		this.icatEP.update(this.sessionId, df);
	}

	public void registerInvestigation(Investigation inv) throws IcatException_Exception {
		inv.setId(this.icatEP.create(this.sessionId, inv));
	}

	public EntityInfo getEntityInfo(String beanName) throws IcatException_Exception {
		return icatEP.getEntityInfo(beanName);
	}

	public Long create(EntityBaseBean bean) throws IcatException_Exception {
		return this.icatEP.create(this.sessionId, bean);
	}

	public List<Long> createMany(List<EntityBaseBean> beans) throws IcatException_Exception {
		return this.icatEP.createMany(this.sessionId, beans);
	}

	public double getRemainingMinutes() throws IcatException_Exception {
		return this.icatEP.getRemainingMinutes(this.sessionId);
	}

	public String getApiVersion() throws IcatException_Exception {
		return this.icatEP.getApiVersion();
	}

	public String getUserName() throws IcatException_Exception {
		return this.icatEP.getUserName(this.sessionId);
	}

	public CompatSession getCompatSession() {
		return compatSession;
	}

	public void registerDatafile(Datafile datafile) throws IcatException_Exception {
		datafile.setId(this.icatEP.create(this.sessionId, datafile));
	}

	public void deleteMany(List<EntityBaseBean> beans) throws IcatException_Exception {
		this.icatEP.deleteMany(sessionId, beans);
	}

}