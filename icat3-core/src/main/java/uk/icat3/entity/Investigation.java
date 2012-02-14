package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "VISIT_ID", "FACILITY_CYCLE_ID", "INSTRUMENT_ID" }) })
@XmlRootElement
@NamedQuery(name = "Investigation.findByUnique", query = "SELECT i FROM Investigation i WHERE (i.name = :name OR i.name is NULL) AND (i.visitId = :visitId OR i.visitId IS NULL) AND (i.facilityCycle = :facilityCycle OR i.facilityCycle IS NULL) AND (i.instrument = :instrument OR i.instrument IS NULL)")
@TableGenerator(name = "investigationGenerator", pkColumnValue = "Investigation")
public class Investigation extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Investigation.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "investigationGenerator")
	private Long id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "VISIT_ID")
	private String visitId;

	@JoinColumn(name = "FACILITY_CYCLE_ID")
	@ManyToOne
	private FacilityCycle facilityCycle;

	@JoinColumn(name = "INSTRUMENT_ID")
	@ManyToOne
	private Instrument instrument;

	@Column(nullable = false)
	private String title;

	@Column(length = 4000)
	private String summary;

	@Temporal(TemporalType.TIMESTAMP)
	private Date releaseDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Publication> publications = new ArrayList<Publication>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationParameter> investigationParameters = new ArrayList<InvestigationParameter>();

	public List<InvestigationParameter> getInvestigationParameters() {
		return investigationParameters;
	}

	public void setInvestigationParameters(List<InvestigationParameter> investigationParameters) {
		this.investigationParameters = investigationParameters;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Sample> samples = new ArrayList<Sample>();

	@JoinColumn(nullable = false)
	@ManyToOne
	private Facility facility;

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	@JoinColumn(nullable = false)
	@ManyToOne
	private InvestigationType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<Publication> getPublications() {
		return publications;
	}

	public void setPublications(List<Publication> publications) {
		this.publications = publications;
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public InvestigationType getType() {
		return type;
	}

	public void setType(InvestigationType type) {
		this.type = type;
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public List<Shift> getShifts() {
		return shifts;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public List<StudyInvestigation> getStudyInvestigations() {
		return studyInvestigations;
	}

	public void setStudyInvestigations(List<StudyInvestigation> studyInvestigations) {
		this.studyInvestigations = studyInvestigations;
	}

	public List<Investigator> getInvestigators() {
		return investigators;
	}

	public void setInvestigators(List<Investigator> investigators) {
		this.investigators = investigators;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Dataset> datasets = new ArrayList<Dataset>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Shift> shifts = new ArrayList<Shift>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Keyword> keywords = new ArrayList<Keyword>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<StudyInvestigation> studyInvestigations = new ArrayList<StudyInvestigation>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Investigator> investigators = new ArrayList<Investigator>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<TopicInvestigation> topicInvestigations = new ArrayList<TopicInvestigation>();

	/* Needed for JPA */
	public Investigation() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVisitId() {
		return this.visitId;
	}

	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getReleaseDate() {
		return this.releaseDate;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public FacilityCycle getFacilityCycle() {
		return this.facilityCycle;
	}

	public void setFacilityCycle(FacilityCycle facilityCycle) {
		this.facilityCycle = facilityCycle;
	}

	@Override
	public String toString() {
		return "Investigation[id=" + id + "]";
	}

	// TODO restire as needed
	// public void isValid(EntityManager manager, boolean deepValidation) throws
	// ValidationException,
	// IcatInternalException {
	// super.isValid(manager, deepValidation);
	//
	// if (deepValidation) {
	//
	// if (this.instrument != null) {
	// // this.instrument.isValid(manager);
	// // check instrument is correct.
	// // check investigation type is correct.
	// Instrument instrument = manager.find(Instrument.class, this.instrument);
	// if (instrument == null)
	// throw new ValidationException(this.instrument +
	// " is not a valid instrument.");
	// }
	//
	// if (this.type != null) {
	// // this.invType.isValid(manager);
	//
	// // check investigation type is correct.
	// InvestigationType investigationType =
	// manager.find(InvestigationType.class,
	// this.type.getName());
	// if (investigationType == null)
	// throw new ValidationException(this.type +
	// " is not a valid investigation type.");
	// }
	//
	// // check all datasets now
	//
	// for (Dataset dataset : datasets) {
	// dataset.isValid(manager);
	// }
	//
	// }
	// }

	/**
	 * Checks weather the investigation is unique in the database.
	 * 
	 * @throws ObjectAlreadyExistsException
	 */
	public void isUnique(EntityManager manager) throws ObjectAlreadyExistsException {
		logger.trace("isUnique?");
		Query query = manager.createNamedQuery("Investigation.findByUnique");
		query = query.setParameter("name", name);
		query = query.setParameter("visitId", visitId);
		query = query.setParameter("facilityCycle", facilityCycle);
		query = query.setParameter("instrument", instrument);

		try {
			logger.trace("Looking for: name: " + name);
			logger.trace("Looking for: visitId: " + visitId);
			logger.trace("Looking for: facilityCycle: " + facilityCycle);
			logger.trace("Looking for: instrument: " + instrument);

			Investigation investigationFound = (Investigation) query.getSingleResult();
			logger.trace("Returned: " + investigationFound);
			if (investigationFound.getId() != null && investigationFound.getId().equals(this.getId())) {
				logger.trace("investigation found is this dataset");
				return;
			} else {
				logger.trace("investigation found is not this investigation, so no unique");
				throw new ObjectAlreadyExistsException(this + " is not unique.  Same unique key as "
						+ investigationFound);
			}
		} catch (NoResultException nre) {
			logger.trace("No results so unique");
			// means it is unique
			return;
		} catch (Throwable ex) {
			logger.warn(ex);
			// means it is unique
			if (ex instanceof ObjectAlreadyExistsException)
				throw (ObjectAlreadyExistsException) ex;
			else
				throw new ObjectAlreadyExistsException(this + " is not unique.");
		}
	}

	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		id = null;
		for (Dataset dataset : datasets) {
			dataset.preparePersist(modId, manager);
			dataset.setInvestigation(this); // Must set the backwards reference
		}
		for (InvestigationParameter investigationParameter : investigationParameters) {
			investigationParameter.preparePersist(modId, manager);
			investigationParameter.setInvestigation(this); // Must set the
															// backwards
															// reference
		}
	}

	@Override
	public Object getPK() {
		return id;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Investigation for " + includes);
		if (!this.includes.contains(FacilityCycle.class)) {
			this.facilityCycle = null;
		}
		if (!this.includes.contains(Publication.class)) {
			this.publications = null;
		}
		if (!this.includes.contains(Sample.class)) {
			this.samples = null;
		}
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
		if (!this.includes.contains(Shift.class)) {
			this.shifts = null;
		}
		if (!this.includes.contains(Keyword.class)) {
			this.keywords = null;
		}
		if (!this.includes.contains(StudyInvestigation.class)) {
			this.studyInvestigations = null;
		}
		if (!this.includes.contains(Investigator.class)) {
			this.investigators = null;
		}
		if (!this.includes.contains(TopicInvestigation.class)) {
			this.topicInvestigations = null;
		}
		if (!this.includes.contains(InvestigationParameter.class)) {
			this.investigationParameters = null;
		}
		if (!this.includes.contains(InvestigationType.class)) {
			this.type = null;
		}
	}

	public List<TopicInvestigation> getTopicInvestigations() {
		return topicInvestigations;
	}

	public void setTopicInvestigations(List<TopicInvestigation> topicInvestigations) {
		this.topicInvestigations = topicInvestigations;
	}
}
