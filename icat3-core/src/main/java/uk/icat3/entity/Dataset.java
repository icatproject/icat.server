package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.Queries;

@SuppressWarnings("serial")
@Entity
@Table(name = "DATASET", uniqueConstraints = { @UniqueConstraint(columnNames = { "SAMPLE_ID", "INVESTIGATION_ID",
		"NAME", "DATASET_TYPE" }) })
@NamedQueries({
		@NamedQuery(name = "Dataset.findById", query = "SELECT d FROM Dataset d WHERE d.id = :id"),
		@NamedQuery(name = "Dataset.findBySampleId", query = "SELECT d FROM Dataset d WHERE d.sampleId = :sampleId"),
		@NamedQuery(name = "Dataset.findByName", query = "SELECT d FROM Dataset d WHERE d.name = :name"),
		@NamedQuery(name = "Dataset.findByDescription", query = "SELECT d FROM Dataset d WHERE d.description = :description"),
		@NamedQuery(name = "Dataset.findByModTime", query = "SELECT d FROM Dataset d WHERE d.modTime = :modTime"),
		@NamedQuery(name = "Dataset.getBySampleId", query = "SELECT d FROM Dataset d where d.name = :sampleName"),
		@NamedQuery(name = "Dataset.findByModId", query = "SELECT d FROM Dataset d WHERE d.modId = :modId"),
		@NamedQuery(name = Queries.DATASET_FINDBY_UNIQUE, query = "SELECT d FROM Dataset d WHERE "
				+ "(d.sampleId = :sampleId OR d.sampleId IS NULL) AND d.name = :name AND d.investigation = :investigation AND d.datasetType = :datasetType"),
		@NamedQuery(name = Queries.DATASETS_BY_SAMPLES, query = Queries.DATASETS_BY_SAMPLES_JPQL),
		@NamedQuery(name = Queries.DATASET_FINDBY_NAME_NOTDELETED, query = Queries.DATASET_FINDBY_NAME_NOTDELETED_JPQL) })
@XmlRootElement
@SequenceGenerator(name = "DATASET_SEQ", sequenceName = "DATASET_ID_SEQ", allocationSize = 1)
public class Dataset extends EntityBaseBean implements Serializable {

	private final static Logger log = Logger.getLogger(Dataset.class);

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private Collection<Datafile> datafileCollection = new ArrayList<Datafile>();

	private transient DatasetInclude datasetInclude = DatasetInclude.NONE;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private Collection<DatasetParameter> datasetParameterCollection = new ArrayList<DatasetParameter>();

	@Column(name = "DATASET_STATUS")
	private String datasetStatus;

	@Column(name = "DATASET_TYPE", nullable = false)
	private String datasetType;

	@Column(name = "DESCRIPTION")
	private String description;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASET_SEQ")
	@Column(name = "ID", nullable = false)
	private Long id;

	@JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
	@ManyToOne(fetch = FetchType.LAZY)
	@XmlTransient
	private Investigation investigation;

	@Transient
	private transient Long investigationId;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "SAMPLE_ID")
	private Long sampleId;

	@Column(name = "START_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@OneToMany(mappedBy = "dataset")
	private Set<InputDataset> inputDatasets;

	@OneToMany(mappedBy = "dataset")
	private Set<OutputDataset> outputDatasets;

	@Column(name = "END_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	/** Creates a new instance of Dataset */
	public Dataset() {
	}

	/**
	 * Creates a new instance of Dataset with the specified values.
	 * 
	 * @param id
	 *            the id of the Dataset
	 */
	public Dataset(Long id) {
		this.id = id;
	}

	/**
	 * Creates a new instance of Dataset with the specified values.
	 * 
	 * @param id
	 *            the id of the Dataset
	 * @param name
	 *            the name of the Dataset
	 * @param modTime
	 *            the modTime of the Dataset
	 * @param modId
	 *            the modId of the Dataset
	 */
	public Dataset(Long id, String name, Date modTime, String modId) {
		this.id = id;
		this.name = name;
		this.modTime = modTime;
		this.modId = modId;
	}

	/**
	 * Adds a DataFile to the DataSet, also adds the DataSet to the DataFile.
	 */
	public void addDataFile(Datafile dataFile) {
		dataFile.setDataset(this);

		Collection<Datafile> datafiles = this.getDatafileCollection();
		if (datafiles == null) {
			datafiles = new ArrayList<Datafile>();
		}
		datafiles.add(dataFile);

		this.setDatafileCollection(datafiles);
	}

	/**
	 * Adds a dataset parameter to the data set in both directions for model
	 */
	public void addDataSetParamaeter(DatasetParameter datasetParameter) {
		datasetParameter.setDataset(this);

		Collection<DatasetParameter> datasetParameters = this.getDatasetParameterCollection();
		if (datasetParameters == null) {
			datasetParameters = new ArrayList<DatasetParameter>();
		}
		datasetParameters.add(datasetParameter);

		this.setDatasetParameterCollection(datasetParameters);
	}

	@Override
	public void canDelete(EntityManager manager) throws ValidationException {
		super.canDelete(manager);
		if (!this.inputDatasets.isEmpty()) {
			throw new ValidationException("Datasets may not be deleted while there are related InputDatasets");
		}
	}

	/**
	 * Determines whether another object is equal to this Dataset. The result is
	 * <code>true</code> if and only if the argument is not null and is a
	 * Dataset object that has the same id field values as this object.
	 * 
	 * @param object
	 *            the reference object with which to compare
	 * @return <code>true</code> if this object is the same as the argument;
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof Dataset)) {
			return false;
		}
		final Dataset other = (Dataset) object;
		if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@XmlTransient
	public Collection<Datafile> getDatafileCollection() {
		return this.datafileCollection;
	}

	@SuppressWarnings("unused")
	@XmlElement(name = "datafileCollection")
	private Collection<Datafile> getDatafileCollection_() {
		if (this.datasetInclude.isDatafiles() || this.includes.contains(Datafile.class)) {
			return this.datafileCollection;
		} else {
			return null;
		}
	}

	/**
	 * Gets the datasetParameterCollection of this Dataset.
	 * 
	 * @return the datasetParameterCollection
	 */
	@XmlTransient
	public Collection<DatasetParameter> getDatasetParameterCollection() {
		return this.datasetParameterCollection;
	}

	/**
	 * This method is used by JAXWS to map to datasetParameterCollection.
	 * Depending on what the include is set to depends on what is returned to
	 * JAXWS and serialised into XML. This is because without XmlTransient all
	 * the collections in the domain model are serialised into XML (meaning alot
	 * of DB hits and serialisation).
	 */
	@SuppressWarnings("unused")
	@XmlElement(name = "datasetParameterCollection")
	private Collection<DatasetParameter> getDatasetParameterCollection_() {
		if (this.datasetInclude.isDatasetParameters() || this.includes.contains(DatasetParameter.class)) {
			return this.datasetParameterCollection;
		} else {
			return null;
		}
	}

	public String getDatasetStatus() {
		return this.datasetStatus;
	}

	public String getDatasetType() {
		return this.datasetType;
	}

	/**
	 * Gets the description of this Dataset.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	/**
	 * Gets the id of this Dataset.
	 * 
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	@XmlTransient
	public Set<InputDataset> getInputDatasets() {
		return this.inputDatasets;
	}

	@SuppressWarnings("unused")
	@XmlElement(name = "inputDatasets")
	private Set<InputDataset> getInputDatasets_() {
		if (this.includes.contains(InputDataset.class)) {
			return this.inputDatasets;
		} else {
			return null;
		}
	}

	@XmlTransient
	public Investigation getInvestigation() {
		return this.investigation;
	}

	public Long getInvestigationId() {
		return this.investigationId;
	}

	public String getLocation() {
		return this.location;
	}

	public String getName() {
		return this.name;
	}

	@XmlTransient
	public Set<OutputDataset> getOutputDatasets() {
		return this.outputDatasets;
	}

	@SuppressWarnings("unused")
	@XmlElement(name = "outputDatasets")
	private Set<OutputDataset> getOutputDatasets_() {
		if (this.includes.contains(OutputDataset.class)) {
			return this.outputDatasets;
		} else {
			return null;
		}
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public Long getSampleId() {
		return this.sampleId;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	/**
	 * Returns a hash code value for the object. This implementation computes a
	 * hash code value based on the id fields in this object.
	 * 
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		hash += this.id != null ? this.id.hashCode() : 0;
		return hash;
	}

	@Override
	public void isUnique(EntityManager manager) throws ValidationException, ObjectAlreadyExistsException,
			IcatInternalException {
		super.isUnique(manager);
		if (manager.createNamedQuery(Queries.DATASET_FINDBY_UNIQUE, Dataset.class).setParameter("name", this.name)
				.setParameter("sampleId", this.sampleId).setParameter("investigation", this.investigation)
				.setParameter("datasetType", this.datasetType).getResultList().size() > 0) {
			throw new ObjectAlreadyExistsException(
					"Uniqueness constraint on (name, sampleId, investigation, datasetType): " + this.name + ", "
							+ this.sampleId + ", " + this.investigation + ", " + this.datasetType);
		}
	}

	/**
	 * Overrides the isValid function, checks each of the datafiles and datafile
	 * parameters are valid
	 * 
	 * @throws ValidationException
	 * @return
	 * @throws IcatInternalException
	 */
	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		super.isValid(manager, deepValidation);

		// check sample info, sample id must be a part of an investigations as
		// well
		outer: if (this.sampleId != null) {
			// check valid sample id

			final Sample sampleRef = manager.find(Sample.class, this.sampleId);
			if (sampleRef == null) {
				throw new ValidationException("Sample[id=" + this.sampleId + "] is not a valid sample id");
			}

			final Collection<Sample> samples = this.investigation.getSampleCollection();
			for (final Sample sample : samples) {
				Dataset.log.trace("Sample for Investigation is: " + sample);
				if (sample.getId().equals(this.sampleId)) {
					// invest has for this sample in
					break outer;
				}
			}
			// if here not got sample in
			throw new ValidationException("Sample[id=" + this.sampleId + "] is not associated with Dataset[id="
					+ this.id + "]'s investigation.");
		}

		if (deepValidation) {
			// check all datafiles now
			if (this.getDatafileCollection() != null) {
				for (final Datafile datafile : this.getDatafileCollection()) {
					datafile.isValid(manager);
				}
			}

			// check all datasetParameter now
			if (this.getDatasetParameterCollection() != null) {
				for (final DatasetParameter datasetParameter : this.getDatasetParameterCollection()) {
					datasetParameter.isValid(manager);
				}
			}
		}

		// check is valid status
		if (this.datasetStatus != null) {
			// datasetStatus.isValid(manager);

			// check datafile format is valid
			final DatasetStatus status = manager.find(DatasetStatus.class, this.datasetStatus);
			if (status == null) {
				throw new ValidationException(this.datasetStatus + " is not a valid DatasetStatus");
			}
		}

		// check is valid status
		if (this.datasetType != null) {
			// datasetType.isValid(manager);

			// check datafile format is valid
			final DatasetType type = manager.find(DatasetType.class, this.datasetType);
			if (type == null) {
				throw new ValidationException(this.datasetType + " is not a valid DatasetType");
			}
		}
	}

	/**
	 * This loads the investigation id from the investigation
	 */
	@PostLoad
	@PostPersist
	public void postLoad() {
		if (this.investigationId == null) {
			this.investigationId = this.getInvestigation().getId();
		}
	}

	@Override
	public void postMergeFixup(EntityManager manager) throws NoSuchObjectFoundException {
		this.investigation = ManagerUtil.find(Investigation.class, this.investigationId, manager);
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException {
		super.preparePersist(modId, manager);
		this.id = null;
		for (final DatasetParameter datasetParameter : this.datasetParameterCollection) {
			datasetParameter.preparePersist(modId, manager);
			datasetParameter.setDataset(this); // Must set the backwards
												// reference
		}
		for (final Datafile datafile : this.datafileCollection) {
			datafile.preparePersist(modId, manager);
			datafile.setDataset(this); // Must set the backwards reference
		}
	}

	@Override
	public void preparePersistTop(String modId, EntityManager manager) throws NoSuchObjectFoundException {
		super.preparePersistTop(modId, manager);
		if (this.investigation == null) {
			this.investigation = ManagerUtil.find(Investigation.class, this.investigationId, manager);
		}
	}

	/**
	 * Sets the datafileCollection of this Dataset to the specified value.
	 * 
	 * @param datafileCollection
	 *            the new datafileCollection
	 */
	public void setDatafileCollection(Collection<Datafile> datafileCollection) {
		this.datafileCollection = datafileCollection;
	}

	@SuppressWarnings("unused")
	private void setDatafileCollection_(Collection<Datafile> datafileCollection) {
		this.datafileCollection = datafileCollection;
	}

	/**
	 * See InvestigationIncude.getInvestigatorCollection_()
	 */
	public void setDatasetInclude(DatasetInclude datasetInclude) {
		this.datasetInclude = datasetInclude;
	}

	/**
	 * Sets the datasetParameterCollection of this Dataset to the specified
	 * value.
	 * 
	 * @param datasetParameterCollection
	 *            the new datasetParameterCollection
	 */
	public void setDatasetParameterCollection(Collection<DatasetParameter> datasetParameterCollection) {
		this.datasetParameterCollection = datasetParameterCollection;
	}

	@SuppressWarnings("unused")
	private void setDatasetParameterCollection_(Collection<DatasetParameter> datasetParameterCollection) {
		this.datasetParameterCollection = datasetParameterCollection;
	}

	public void setDatasetStatus(String datasetStatus) {
		this.datasetStatus = datasetStatus;
	}

	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}

	/**
	 * Sets the description of this Dataset to the specified value.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Sets the investigation of this Dataset to the specified value.
	 * 
	 * @param investigation
	 *            the new investigation
	 */
	void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	/**
	 * Sets the investigationId of this Dataset to the specified value.
	 * 
	 * @param investigationId
	 *            the new investigationId
	 */
	public void setInvestigationId(Long investigationId) {
		this.investigationId = investigationId;
	}

	/**
	 * Sets the location of this Datafile to the specified value.
	 * 
	 * @param location
	 *            the new location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Sets the name of this Dataset to the specified value.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the sampleId of this Dataset to the specified value.
	 * 
	 * @param sampleId
	 *            the new sampleId
	 */
	public void setSampleId(Long sampleId) {
		this.sampleId = sampleId;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * Returns a string representation of the object. This implementation
	 * constructs that representation based on the id fields.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "Dataset[id=" + this.id + "]";
	}

}
