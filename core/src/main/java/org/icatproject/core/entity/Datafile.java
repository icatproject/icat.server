package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A data file")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "LOCATION", "DATASET_ID" }) })
public class Datafile extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Datafile.class);

	@Comment("Checksum of file represented as a string")
	private String checksum;

	@Comment("Date of creation of the actual file rather than storing the metadata")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileCreateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	private DatafileFormat datafileFormat;

	@Comment("Date of modification of the actual file rather than of the metadata")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileModTime;

	@Comment("The dataset which holds this file")
	@JoinColumn(name = "DATASET_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Dataset dataset;

	@Comment("A full description of the file contents")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "destDatafile")
	private List<RelatedDatafile> destDatafiles = new ArrayList<RelatedDatafile>();

	@Comment("The Digital Object Identifier associated with this data file")
	private String doi;

	@Comment("Expressed in bytes")
	private Long fileSize;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<InputDatafile> inputDatafiles = new ArrayList<InputDatafile>();

	@Comment("The logical location of the file - which may also be the physical location")
	@Column(name = "LOCATION")
	private String location;

	@Comment("A name given to the file")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<OutputDatafile> outputDatafiles = new ArrayList<OutputDatafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<DatafileParameter> parameters = new ArrayList<DatafileParameter>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sourceDatafile")
	private List<RelatedDatafile> sourceDatafiles = new ArrayList<RelatedDatafile>();

	/* Needed for JPA */
	public Datafile() {
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

	public String getChecksum() {
		return checksum;
	}

	public Date getDatafileCreateTime() {
		return datafileCreateTime;
	}

	public DatafileFormat getDatafileFormat() {
		return datafileFormat;
	}

	public Date getDatafileModTime() {
		return datafileModTime;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public String getDescription() {
		return description;
	}

	public List<RelatedDatafile> getDestDatafiles() {
		return destDatafiles;
	}

	public String getDoi() {
		return doi;
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

	public Long getFileSize() {
		return fileSize;
	}

	public List<InputDatafile> getInputDatafiles() {
		return inputDatafiles;
	}

	public String getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public List<OutputDatafile> getOutputDatafiles() {
		return outputDatafiles;
	}

	public List<DatafileParameter> getParameters() {
		return parameters;
	}

	public List<RelatedDatafile> getSourceDatafiles() {
		return sourceDatafiles;
	}

	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		id = null;
		for (DatafileParameter datafileParameter : parameters) {
			datafileParameter.preparePersist(modId, manager);
			datafileParameter.setDatafile(this);
		}
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public void setDatafileCreateTime(Date datafileCreateTime) {
		this.datafileCreateTime = datafileCreateTime;
	}

	public void setDatafileFormat(DatafileFormat datafileFormat) {
		this.datafileFormat = datafileFormat;
	}

	public void setDatafileModTime(Date datafileModTime) {
		this.datafileModTime = datafileModTime;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDestDatafiles(List<RelatedDatafile> destDatafiles) {
		this.destDatafiles = destDatafiles;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setInputDatafiles(List<InputDatafile> inputDatafiles) {
		this.inputDatafiles = inputDatafiles;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOutputDatafiles(List<OutputDatafile> outputDatafiles) {
		this.outputDatafiles = outputDatafiles;
	}

	public void setParameters(List<DatafileParameter> parameters) {
		this.parameters = parameters;
	}

	public void setSourceDatafiles(List<RelatedDatafile> sourceDatafiles) {
		this.sourceDatafiles = sourceDatafiles;
	}

	@Override
	public String toString() {
		return "Datafile[id=" + id + "]";
	}

}
