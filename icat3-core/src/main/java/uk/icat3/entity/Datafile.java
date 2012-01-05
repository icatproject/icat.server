/*
 * Datafile.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.DatafileInclude;

/**
 * Entity class Datafile
 * 
 * @author gjd37 Modification: 02-Sep-2009 (SN): Removed commented code and
 *         redundant code
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "DATAFILE")
@NamedQueries({
		@NamedQuery(name = "Datafile.findById", query = "SELECT d FROM Datafile d WHERE d.id = :id"),
		@NamedQuery(name = "Datafile.findByName", query = "SELECT d FROM Datafile d WHERE d.name = :name"),
		@NamedQuery(name = "Datafile.findByDescription", query = "SELECT d FROM Datafile d WHERE d.description = :description"),
		@NamedQuery(name = "Datafile.findByDatafileVersion", query = "SELECT d FROM Datafile d WHERE d.datafileVersion = :datafileVersion"),
		@NamedQuery(name = "Datafile.findByDatafileVersionComment", query = "SELECT d FROM Datafile d WHERE d.datafileVersionComment = :datafileVersionComment"),
		@NamedQuery(name = "Datafile.findByLocation", query = "SELECT d FROM Datafile d WHERE d.location = :location"),
		@NamedQuery(name = "Datafile.findByDatafileCreateTime", query = "SELECT d FROM Datafile d WHERE d.datafileCreateTime = :datafileCreateTime"),
		@NamedQuery(name = "Datafile.findByDatafileModifyTime", query = "SELECT d FROM Datafile d WHERE d.datafileModifyTime = :datafileModifyTime"),
		@NamedQuery(name = "Datafile.findByFileSize", query = "SELECT d FROM Datafile d WHERE d.fileSize = :fileSize"),
		@NamedQuery(name = "Datafile.findByCommand", query = "SELECT d FROM Datafile d WHERE d.command = :command"),
		@NamedQuery(name = "Datafile.findByChecksum", query = "SELECT d FROM Datafile d WHERE d.checksum = :checksum"),
		@NamedQuery(name = "Datafile.findBySignature", query = "SELECT d FROM Datafile d WHERE d.signature = :signature"),
		@NamedQuery(name = "Datafile.findByModTime", query = "SELECT d FROM Datafile d WHERE d.modTime = :modTime"),
		@NamedQuery(name = "Datafile.findByModId", query = "SELECT d FROM Datafile d WHERE d.modId = :modId"),
		@NamedQuery(name = "Datafile.findByUnique", query = "SELECT d FROM Datafile d where d.name = :name and d.location = :location and d.dataset.id = :datasetId") })
@XmlRootElement
@SequenceGenerator(name = "DATAFILE_SEQ", sequenceName = "DATAFILE_ID_SEQ", allocationSize = 1)
public class Datafile extends EntityBaseBean implements Serializable {

	/**
	 * Override logger
	 */
	private static Logger log = Logger.getLogger(Datafile.class);
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATAFILE_SEQ")
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "DATAFILE_VERSION")
	private String datafileVersion;

	@Column(name = "DATAFILE_VERSION_COMMENT")
	private String datafileVersionComment;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "DATAFILE_CREATE_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileCreateTime;

	@Column(name = "DATAFILE_MODIFY_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileModifyTime;

	@Column(name = "FILE_SIZE")
	private Long fileSize;

	@Column(name = "COMMAND")
	private String command;

	@Column(name = "CHECKSUM")
	private String checksum;

	@Column(name = "SIGNATURE")
	private String signature;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private Collection<RelatedDatafiles> relatedDatafilesCollection;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile1")
	private Collection<RelatedDatafiles> relatedDatafilesCollection1;

	@JoinColumns(value = { @JoinColumn(name = "DATAFILE_FORMAT", referencedColumnName = "NAME"),
			@JoinColumn(name = "DATAFILE_FORMAT_VERSION", referencedColumnName = "VERSION") })
	@ManyToOne
	private DatafileFormat datafileFormat;

	@JoinColumn(name = "DATASET_ID", referencedColumnName = "ID")
	@ManyToOne
	@XmlTransient
	private Dataset dataset;

	@Transient
	private transient Long datasetId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private Collection<DatafileParameter> datafileParameterCollection = new ArrayList<DatafileParameter>();
	
	@OneToMany(mappedBy = "datafile")
	private Set<InputDatafile> inputDatafiles;

	@OneToMany(mappedBy = "datafile")
	private Set<OutputDatafile> outputDatafiles;

	/**
	 * What to include within the datafile for searches or gets
	 */
	private transient DatafileInclude datafileInclude = DatafileInclude.ALL;

	/** Creates a new instance of Datafile */
	public Datafile() {
	}

	/**
	 * Creates a new instance of Datafile with the specified values.
	 * 
	 * @param id
	 *            the id of the Datafile
	 */
	public Datafile(Long id) {
		this.id = id;
	}

	/**
	 * Creates a new instance of Datafile with the specified values.
	 * 
	 * @param id
	 *            the id of the Datafile
	 * @param modTime
	 *            the modTime of the Datafile
	 * @param modId
	 *            the modId of the Datafile
	 */
	public Datafile(Long id, Date modTime, String modId) {
		this.id = id;
		this.modTime = modTime;
		this.modId = modId;
	}

	/**
	 * Gets the id of this Datafile.
	 * 
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets the name of this Datafile.
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of this Datafile to the specified value.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description of this Datafile.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description of this Datafile to the specified value.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the datafileVersion of this Datafile.
	 * 
	 * @return the datafileVersion
	 */
	public String getDatafileVersion() {
		return this.datafileVersion;
	}

	/**
	 * Sets the datafileVersion of this Datafile to the specified value.
	 * 
	 * @param datafileVersion
	 *            the new datafileVersion
	 */
	public void setDatafileVersion(String datafileVersion) {
		this.datafileVersion = datafileVersion;
	}

	/**
	 * Gets the datafileVersionComment of this Datafile.
	 * 
	 * @return the datafileVersionComment
	 */
	public String getDatafileVersionComment() {
		return this.datafileVersionComment;
	}

	/**
	 * Sets the datafileVersionComment of this Datafile to the specified value.
	 * 
	 * @param datafileVersionComment
	 *            the new datafileVersionComment
	 */
	public void setDatafileVersionComment(String datafileVersionComment) {
		this.datafileVersionComment = datafileVersionComment;
	}

	/**
	 * Gets the location of this Datafile.
	 * 
	 * @return the location
	 */
	public String getLocation() {
		return this.location;
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
	 * Gets the datafileCreateTime of this Datafile.
	 * 
	 * @return the datafileCreateTime
	 */
	public Date getDatafileCreateTime() {
		return this.datafileCreateTime;
	}

	/**
	 * Sets the datafileCreateTime of this Datafile to the specified value.
	 * 
	 * @param datafileCreateTime
	 *            the new datafileCreateTime
	 */
	public void setDatafileCreateTime(Date datafileCreateTime) {
		this.datafileCreateTime = datafileCreateTime;
	}

	/**
	 * Gets the datafileModifyTime of this Datafile.
	 * 
	 * @return the datafileModifyTime
	 */
	public Date getDatafileModifyTime() {
		return this.datafileModifyTime;
	}

	/**
	 * Sets the datafileModifyTime of this Datafile to the specified value.
	 * 
	 * @param datafileModifyTime
	 *            the new datafileModifyTime
	 */
	public void setDatafileModifyTime(Date datafileModifyTime) {
		this.datafileModifyTime = datafileModifyTime;
	}

	/**
	 * Gets the fileSize of this Datafile.
	 * 
	 * @return the fileSize
	 */
	public Long getFileSize() {
		return this.fileSize;
	}

	/**
	 * Sets the fileSize of this Datafile to the specified value.
	 * 
	 * @param fileSize
	 *            the new fileSize
	 */
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * Gets the command of this Datafile.
	 * 
	 * @return the command
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * Sets the command of this Datafile to the specified value.
	 * 
	 * @param command
	 *            the new command
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * Gets the checksum of this Datafile.
	 * 
	 * @return the checksum
	 */
	public String getChecksum() {
		return this.checksum;
	}

	/**
	 * Sets the checksum of this Datafile to the specified value.
	 * 
	 * @param checksum
	 *            the new checksum
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * Gets the signature of this Datafile.
	 * 
	 * @return the signature
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * Sets the signature of this Datafile to the specified value.
	 * 
	 * @param signature
	 *            the new signature
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Gets the relatedDatafilesCollection of this Datafile.
	 * 
	 * @return the relatedDatafilesCollection
	 */
	@XmlTransient
	public Collection<RelatedDatafiles> getRelatedDatafilesCollection() {
		return this.relatedDatafilesCollection;
	}

	/**
	 * Sets the relatedDatafilesCollection of this Datafile to the specified
	 * value.
	 * 
	 * @param relatedDatafilesCollection
	 *            the new relatedDatafilesCollection
	 */
	public void setRelatedDatafilesCollection(Collection<RelatedDatafiles> relatedDatafilesCollection) {
		this.relatedDatafilesCollection = relatedDatafilesCollection;
	}

	/**
	 * This method is used by JAXWS to map to datasetParameterCollection.
	 * Depending on what the include is set to depends on what is returned to
	 * JAXWS and serialised into XML. This is because without XmlTransient all
	 * the collections in the domain model are serialised into XML (meaning alot
	 * of DB hits and serialisation).
	 */
	@SuppressWarnings("unused")
	@XmlElement(name = "relatedDatafilesCollection")
	private Collection<RelatedDatafiles> getRelatedDatafilesCollection_() {
		if (datafileInclude.isRelatedDatafiles() || includes.contains(RelatedDatafiles.class)) {
			return this.relatedDatafilesCollection;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private void setRelatedDatafilesCollection_(Collection<RelatedDatafiles> relatedDatafilesCollection) {
		this.relatedDatafilesCollection = relatedDatafilesCollection;
	}

	/**
	 * Gets the relatedDatafilesCollection1 of this Datafile.
	 * 
	 * @return the relatedDatafilesCollection1
	 */
	public Collection<RelatedDatafiles> getRelatedDatafilesCollection1() {
		return this.relatedDatafilesCollection1;
	}

	/**
	 * Sets the relatedDatafilesCollection1 of this Datafile to the specified
	 * value.
	 * 
	 * @param relatedDatafilesCollection1
	 *            the new relatedDatafilesCollection1
	 */
	public void setRelatedDatafilesCollection1(Collection<RelatedDatafiles> relatedDatafilesCollection1) {
		this.relatedDatafilesCollection1 = relatedDatafilesCollection1;
	}

	/**
	 * Gets the datafileFormat of this Datafile.
	 * 
	 * @return the datafileFormat
	 */
	public DatafileFormat getDatafileFormat() {
		return this.datafileFormat;
	}

	/**
	 * Sets the datafileFormat of this Datafile to the specified value.
	 * 
	 * @param datafileFormat
	 *            the new datafileFormat
	 */
	public void setDatafileFormat(DatafileFormat datafileFormat) {
		this.datafileFormat = datafileFormat;
	}

	/**
	 * Gets the dataset of this Datafile.
	 * 
	 * @return the dataset
	 */
	@XmlTransient
	public Dataset getDataset() {
		return this.dataset;
	}

	/**
	 * Sets the dataset of this Datafile to the specified value.
	 * 
	 * @param dataset
	 *            the new dataset
	 */
	void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * Gets the datasetId of this Datafile.
	 * 
	 * @return the datasetId
	 */
	public Long getDatasetId() {
		return this.datasetId;
	}

	/**
	 * Sets the datasetId of this Datafile to the specified value.
	 * 
	 * @param datasetId
	 *            the new datasetId
	 */
	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * Gets the datafileParameterCollection of this Datafile.
	 * 
	 * @return the datafileParameterCollection
	 */
	@XmlTransient
	public Collection<DatafileParameter> getDatafileParameterCollection() {
		return this.datafileParameterCollection;
	}

	/**
	 * Sets the datafileParameterCollection of this Datafile to the specified
	 * value.
	 * 
	 * @param datafileParameterCollection
	 *            the new datafileParameterCollection
	 */
	public void setDatafileParameterCollection(Collection<DatafileParameter> datafileParameterCollection) {
		this.datafileParameterCollection = datafileParameterCollection;
	}

	/**
	 * This method is used by JAXWS to map to datasetParameterCollection.
	 * Depending on what the include is set to depends on what is returned to
	 * JAXWS and serialised into XML. This is because without XmlTransient all
	 * the collections in the domain model are serialised into XML.
	 */
	@SuppressWarnings("unused")
	@XmlElement(name = "datafileParameterCollection")
	private Collection<DatafileParameter> getDatafileParameterCollection_() {
		if (datafileInclude.isDatafileParameters() || includes.contains(DatafileParameter.class)) {
			return this.datafileParameterCollection;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private void setDatafileParameterCollection_(Collection<DatafileParameter> datafileParameterCollection) {
		this.datafileParameterCollection = datafileParameterCollection;
	}

	/**
	 * Adds a datafile parameter to the data file in both directions for model
	 */
	public void addDataFileParameter(DatafileParameter dataFileParameter) {
		dataFileParameter.setDatafile(this);

		Collection<DatafileParameter> datafileParameters = this.getDatafileParameterCollection();
		if (datafileParameters == null) {
			datafileParameters = new ArrayList<DatafileParameter>();
		}
		datafileParameters.add(dataFileParameter);

		this.setDatafileParameterCollection(datafileParameters);
	}
	
	@XmlTransient
	public Set<InputDatafile> getInputDatafiles() {
		return this.inputDatafiles;
	}

	@SuppressWarnings("unused")
	@XmlElement(name = "inputDatafiles")
	private Set<InputDatafile> getInputDatafiles_() {
		if (this.includes.contains(InputDatafile.class)) {
			return this.inputDatafiles;
		} else {
			return null;
		}
	}
	
	@XmlTransient
	public Set<OutputDatafile> getOutputDatafiles() {
		return this.outputDatafiles;
	}

	@SuppressWarnings("unused")
	@XmlElement(name = "outputDatafiles")
	private Set<OutputDatafile> getOutputDatafiles_() {
		if (this.includes.contains(OutputDatafile.class)) {
			return this.outputDatafiles;
		} else {
			return null;
		}
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
		hash += (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	/**
	 * Determines whether another object is equal to this Datafile. The result
	 * is <code>true</code> if and only if the argument is not null and is a
	 * Datafile object that has the same id field values as this object.
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
		if (!(object instanceof Datafile)) {
			return false;
		}
		Datafile other = (Datafile) object;
		if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a string representation of the object. This implementation
	 * constructs that representation based on the id fields.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "Datafile[id=" + id + "]";
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
	public void isValid(EntityManager manager) throws ValidationException, IcatInternalException {
		super.isValid(manager);
		if (getDatafileParameterCollection() != null) {
			for (DatafileParameter datafileParameter : getDatafileParameterCollection()) {
				datafileParameter.isValid(manager);
			}
		}

		if (datafileFormat != null) {
			datafileFormat.isValid(manager);

			// check datafile format is valid
			DatafileFormat format = manager.find(DatafileFormat.class, datafileFormat.getDatafileFormatPK());
			if (format == null) {
				throw new ValidationException(datafileFormat + " is not a valid DatafileFormat");
			}
		}
	}

	/**
	 * This loads the investigation id from the investigation
	 */
	@PostLoad
	@PostPersist
	// @Override
	public void postLoad() {
		if (datasetId == null) {
			datasetId = getDataset().getId();
			// super.postLoad();
		}
	}

	public DatafileInclude getDatafileInclude() {
		return datafileInclude;
	}

	public void setDatafileInclude(DatafileInclude datafileInclude) {
		this.datafileInclude = datafileInclude;
	}

	/**
	 * This method checks whether the datafile already exists, if it does then
	 * it will throw a validation exception. The field that it check is Name,
	 * Location, and Dataset ID. These form Composite Unique key.
	 * 
	 * @param manager
	 * @return
	 * @throws ValidationException
	 */
	public void isUnique(EntityManager manager) throws ValidationException {
		Query query = manager.createNamedQuery("Datafile.findByUnique");
		query = query.setParameter("name", name);
		query = query.setParameter("location", location);
		query = query.setParameter("datasetId", dataset.getId());

		try {
			log.trace("Looking for: name: " + name);
			log.trace("Looking for: location: " + location);
			log.trace("Looking for: datasetId: " + datasetId);
			Datafile datafileFound = (Datafile) query.getSingleResult();
			log.trace("Returned: " + datafileFound);
			if (datafileFound.getId() != null && datafileFound.getId().equals(this.getId())) {
				System.out.println("Found this same object");
				log.trace("Datafile found is this datafile");
				return;
			} else {
				log.trace("Datafile found is not this datafile, so not unique");
				throw new ValidationException(this + " is not unique. Same unique key as " + datafileFound);
			}
		} catch (NoResultException nre) {
			log.trace("No results so unique");
			// means it is unique
			return;
		} catch (Throwable ex) {
			log.warn(ex);
			// means it is unique
			throw new ValidationException(this + " is not unique.");
		}
	}

	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException {
		super.preparePersist(modId, manager);
		id = null;
		for (DatafileParameter datafileParameter : datafileParameterCollection) {
			datafileParameter.preparePersist(modId, manager);
			datafileParameter.setDatafile(this); // Must set the backwards
													// reference
		}
	}

	@Override
	public void preparePersistTop(String modId, EntityManager manager) throws NoSuchObjectFoundException {
		super.preparePersistTop(modId, manager);
		if (dataset == null) {
			dataset = ManagerUtil.find(Dataset.class, datasetId, manager);
		}
	}

	@Override
	public Object getPK() {
		return id;
	}

}
