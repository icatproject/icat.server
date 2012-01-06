import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import uk.icat3.client.Application;
import uk.icat3.client.BadParameterException_Exception;
import uk.icat3.client.Datafile;
import uk.icat3.client.DatafileFormat;
import uk.icat3.client.DatafileFormatPK;
import uk.icat3.client.Dataset;
import uk.icat3.client.DatasetParameter;
import uk.icat3.client.DatasetParameterPK;
import uk.icat3.client.DatasetType;
import uk.icat3.client.EntityBaseBean;
import uk.icat3.client.Facility;
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
import uk.icat3.client.ObjectAlreadyExistsException_Exception;
import uk.icat3.client.OutputDatafile;
import uk.icat3.client.OutputDataset;
import uk.icat3.client.Parameter;
import uk.icat3.client.ParameterPK;
import uk.icat3.client.ParameterValueType;
import uk.icat3.client.SessionException_Exception;
import uk.icat3.client.ValidationException_Exception;

class Session {
	public enum ParameterType {
		DATASET, DATAFILE, SAMPLE
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

	public void addRule(String groupName, String what, String crud, String restriction)
			throws BadParameterException_Exception, IcatInternalException_Exception, SessionException_Exception {
		this.icatEP.addRule(this.sessionId, groupName, what, crud, restriction);
	}

	public void addUserGroupMember(String group, String member) throws ObjectAlreadyExistsException_Exception,
			SessionException_Exception {
		this.icatEP.addUserGroupMember(this.sessionId, group, member);
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
		lo = this.search("Parameter");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((Parameter) o);
		}
		lo = this.search("Facility");
		for (final Object o : lo) {
			System.out.println("Deleting " + o);
			this.delete((Facility) o);
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
		datafile.setDatasetId(ds.getId());
		datafile.setId((Long) this.icatEP.create(this.sessionId, datafile));
		return datafile;
	}

	public DatafileFormat createDatafileFormat(String name, String formatType) throws Exception {
		final DatafileFormatPK dffpk = new DatafileFormatPK();
		dffpk.setName(name);
		dffpk.setVersion("1");
		final DatafileFormat dff = new DatafileFormat();
		dff.setDatafileFormatPK(dffpk);
		dff.setFormatType(formatType);
		this.icatEP.create(this.sessionId, dff);
		return dff;
	}

	public Dataset createDataset(String name, String type, Investigation inv) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setDatasetType(type);
		dataset.setInvestigationId(inv.getId());
		dataset.setId((Long) this.icatEP.create(this.sessionId, dataset));
		return dataset;
	}

	public DatasetParameter createDatasetParameter(Object value, Parameter p, Dataset ds)
			throws IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, ObjectAlreadyExistsException_Exception, SessionException_Exception,
			ValidationException_Exception {
		final ParameterPK pk = p.getParameterPK();
		final DatasetParameterPK dPK = new DatasetParameterPK();
		dPK.setName(pk.getName());
		dPK.setUnits(pk.getUnits());
		dPK.setDatasetId(ds.getId());

		final DatasetParameter dsp = new DatasetParameter();
		dsp.setDatasetParameterPK(dPK);
		if (p.getValueType() == ParameterValueType.DATE_AND_TIME) {
			dsp.setDateTimeValue((XMLGregorianCalendar) value);
		}
		dsp.setParameter(p);
		this.icatEP.create(this.sessionId, dsp);
		return dsp;
	}

	public void createDatasetType(String name) throws Exception {
		final DatasetType dst = new DatasetType();
		dst.setName(name);
		this.icatEP.create(this.sessionId, dst);
	}

	public void createFacility(String shortName, long daysUntilRelease) throws Exception {
		final Facility f = new Facility();
		f.setFacilityShortName(shortName);
		f.setDaysUntilRelease(daysUntilRelease);
		this.icatEP.create(this.sessionId, f);
	}

	public Investigation createInvestigation(String facility, String invNumber, String title, String invType)
			throws Exception {
		final Investigation i = new Investigation();
		i.setFacility(facility);
		i.setInvNumber(invNumber);
		i.setTitle(title);
		i.setInvType(invType);
		i.setId((Long) this.icatEP.create(this.sessionId, i));
		return i;
	}

	public void createInvestigationType(String name) throws Exception {
		final InvestigationType type = new InvestigationType();
		type.setName(name);
		this.icatEP.create(this.sessionId, type);
	}

	public Job createJob(Application application) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			ObjectAlreadyExistsException_Exception, SessionException_Exception, ValidationException_Exception {
		final Job job = new Job();
		job.setApplication(application);
		job.setId((Long) this.icatEP.create(this.sessionId, job));
		return job;
	}

	public Parameter createParameterPK(String name, String units, String description, ParameterType pt,
			ParameterValueType pvt) throws IcatInternalException_Exception, InsufficientPrivilegesException_Exception,
			NoSuchObjectFoundException_Exception, ObjectAlreadyExistsException_Exception, SessionException_Exception,
			ValidationException_Exception {

		final ParameterPK ppk = new ParameterPK();
		ppk.setName(name);
		ppk.setUnits(units);
		final Parameter p = new Parameter();

		p.setParameterPK(ppk);
		p.setDescription(description);
		if (pt == ParameterType.DATASET) {
			p.setDatasetParameter(true);
		} else if (pt == ParameterType.DATAFILE) {
			p.setDatafileParameter(true);
		} else if (pt == ParameterType.SAMPLE) {
			p.setSampleParameter(true);
		}
		p.setValueType(ParameterValueType.DATE_AND_TIME);
		this.icatEP.create(this.sessionId, p);
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
		this.addRule("root", "DatafileFormat", "CRUD", null);
		this.addRule("root", "DatasetType", "CRUD", null);
		this.addRule("root", "Facility", "CRUD", null);
		this.addRule("root", "Investigation", "CRUD", null);
		this.addRule("root", "InvestigationType", "CRUD", null);
		this.addRule("root", "Parameter", "CRUD", null);
		this.addRule("root", "Investigation", "CRUD", null);
		this.addRule("root", "Dataset", "CRUD", null);
		this.addRule("root", "Parameter", "CRUD", null);
		this.addRule("root", "DatasetParameter", "CRUD", null);
		this.addRule("root", "Datafile", "CRUD", null);
		this.addRule("root", "DatafileFormat", "CRUD", null);
		this.addRule("root", "DatasetType", "CRUD", null);
		this.addRule("root", "Application", "CRUD", null);
		this.addRule("root", "Job", "CRUD", null);
		this.addRule("root", "InputDataset", "CRUD", null);
		this.addRule("root", "OutputDataset", "CRUD", null);
		this.addRule("root", "InputDatafile", "CRUD", null);
		this.addRule("root", "OutputDatafile", "CRUD", null);
	}

	public void update(EntityBaseBean df) throws IcatInternalException_Exception,
			InsufficientPrivilegesException_Exception, NoSuchObjectFoundException_Exception,
			SessionException_Exception, ValidationException_Exception {
		this.icatEP.update(this.sessionId, df);
	}

}