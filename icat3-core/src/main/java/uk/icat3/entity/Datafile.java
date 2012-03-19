package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToMany;
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

@Comment("A data file")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "LOCATION", "DATASET_ID" }) })
@TableGenerator(name = "datafileGenerator", pkColumnValue = "Datafile")
public class Datafile extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Datafile.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "datafileGenerator")
	private Long id;

	@Comment("A name given to the file")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("A full description of the file contents")
	private String description;

	@Comment("**** Do we want this - it is not part of the constraint - not used by ISIS")
	private String version;

	@Comment("**** Do we want this - it is not part of the constraint - not used by ISIS")
	private String versionComment;

	@Comment("The logical location of the file - which may also be the physical location")
	@Column(name = "LOCATION")
	private String location;

	@Comment("**** Do we want this")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileCreateTime;

	@Comment("**** Do we want this")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileModTime;

	@Comment("Expressed in bytes")
	private Long fileSize;

	@Comment("**** Do we want this - not used by ISIS")
	private String command;

	@Comment("**** Nobody would search for a file with this field")
	private String checksum;

	@Comment("**** Nobody would search for a file with this field - not used by ISIS")
	private String signature;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sourceDatafile")
	private List<RelatedDatafile> sourceDatafiles = new ArrayList<RelatedDatafile>();

	public List<RelatedDatafile> getSourceDatafiles() {
		return sourceDatafiles;
	}

	public void setSourceDatafiles(List<RelatedDatafile> sourceDatafiles) {
		this.sourceDatafiles = sourceDatafiles;
	}

	public List<RelatedDatafile> getDestDatafiles() {
		return destDatafiles;
	}

	public void setDestDatafiles(List<RelatedDatafile> destDatafiles) {
		this.destDatafiles = destDatafiles;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "destDatafile")
	private List<RelatedDatafile> destDatafiles = new ArrayList<RelatedDatafile>();

	@ManyToOne(fetch = FetchType.LAZY)
	private DatafileFormat datafileFormat;

	@Comment("The dataset which holds this file")
	@JoinColumn(name = "DATASET_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Dataset dataset;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<DatafileParameter> parameters = new ArrayList<DatafileParameter>();

	public List<DatafileParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<DatafileParameter> parameters) {
		this.parameters = parameters;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<InputDatafile> inputDatafiles = new ArrayList<InputDatafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<OutputDatafile> outputDatafiles = new ArrayList<OutputDatafile>();

	/* Needed for JPA */
	public Datafile() {
	}

	@Override
	public String toString() {
		return "Datafile[id=" + id + "]";
	}

	// TODO fix this
	// @Override
	// public void isValid(EntityManager manager) throws ValidationException,
	// IcatInternalException {
	// super.isValid(manager);
	// if (getDatafileParameterCollection() != null) {
	// for (DatafileParameter datafileParameter :
	// getDatafileParameterCollection()) {
	// datafileParameter.isValid(manager);
	// }
	// }
	//
	// if (datafileFormat != null) {
	// datafileFormat.isValid(manager);
	//
	// // check datafile format is valid
	// DatafileFormat format = manager.find(DatafileFormat.class,
	// datafileFormat.getDatafileFormatPK());
	// if (format == null) {
	// throw new ValidationException(datafileFormat +
	// " is not a valid DatafileFormat");
	// }
	// }
	// }

	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		id = null;
		for (DatafileParameter datafileParameter : parameters) {
			datafileParameter.preparePersist(modId, manager);
			datafileParameter.setDatafile(this);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersionComment() {
		return versionComment;
	}

	public void setVersionComment(String versionComment) {
		this.versionComment = versionComment;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getDatafileCreateTime() {
		return datafileCreateTime;
	}

	public void setDatafileCreateTime(Date datafileCreateTime) {
		this.datafileCreateTime = datafileCreateTime;
	}

	public Date getDatafileModTime() {
		return datafileModTime;
	}

	public void setDatafileModTime(Date datafileModTime) {
		this.datafileModTime = datafileModTime;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public DatafileFormat getDatafileFormat() {
		return datafileFormat;
	}

	public void setDatafileFormat(DatafileFormat datafileFormat) {
		this.datafileFormat = datafileFormat;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public List<InputDatafile> getInputDatafiles() {
		return inputDatafiles;
	}

	public void setInputDatafiles(List<InputDatafile> inputDatafiles) {
		this.inputDatafiles = inputDatafiles;
	}

	public List<OutputDatafile> getOutputDatafiles() {
		return outputDatafiles;
	}

	public void setOutputDatafiles(List<OutputDatafile> outputDatafiles) {
		this.outputDatafiles = outputDatafiles;
	}

	@Override
	public Object getPK() {
		return id;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Datafile for " + includes);
		if (!this.includes.contains(RelatedDatafile.class)) {
			this.sourceDatafiles = null;
			this.destDatafiles = null;
		}
		if (!this.includes.contains(DatafileFormat.class)) {
			this.datafileFormat = null;
		}
		if (!this.includes.contains(Dataset.class)) {
			this.dataset = null;
		}
		if (!this.includes.contains(DatafileParameter.class)) {
			this.parameters = null;
		}
		if (!this.includes.contains(InputDatafile.class)) {
			this.inputDatafiles = null;
		}
		if (!this.includes.contains(OutputDatafile.class)) {
			this.outputDatafiles = null;
		}

	}

}
