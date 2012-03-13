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
import uk.icat3.exceptions.ValidationException;

@Comment("A collection of data files and part of an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SAMPLE_ID", "INVESTIGATION_ID", "NAME", "TYPE" }) })
@XmlRootElement
@TableGenerator(name = "datasetGenerator", pkColumnValue = "Dataset")
public class Dataset extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Dataset.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "datasetGenerator")
	private Long id;

	@Comment("The data files within the dataset")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<Datafile> datafiles = new ArrayList<Datafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<DatasetParameter> datasetParameters = new ArrayList<DatasetParameter>();

	@ManyToOne(fetch = FetchType.LAZY)
	private DatasetStatus status;

	@JoinColumn(name = "TYPE", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DatasetType type;

	@Comment("An informal description of the data set")
	private String description;

	@JoinColumn(name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Comment("Identifies a location from which all the files of the data set might be accessed. It might be a directory")
	private String location;

	@Comment("A short name for the data set")
	@Column(name = "NAME", nullable = false)
	private String name;

	@JoinColumn(name = "SAMPLE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Sample sample;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<InputDataset> inputDatasets;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<OutputDataset> outputDatasets;

	@Column(name = "END_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	/* Needed for JPA */
	public Dataset() {
	}

	@Override
	public void canDelete(EntityManager manager) throws ValidationException {
		super.canDelete(manager);
		if (!this.inputDatasets.isEmpty()) {
			throw new ValidationException("Datasets may not be deleted while there are related InputDatasets");
		}
	}

	public String getDescription() {
		return this.description;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public Long getId() {
		return this.id;
	}

	public String getLocation() {
		return this.location;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		super.isValid(manager, deepValidation);

		// TODO put this code back if needed

		// // check sample info, sample id must be a part of an investigations
		// as
		// // well
		// outer: if (this.sample != null) {
		// // check valid sample id
		//
		// final List<Sample> samples = this.investigation.getSamples();
		// for (final Sample sample : samples) {
		// Dataset.logger.trace("Sample for Investigation is: " + sample);
		// if (sample.getId().equals(this.sampleId)) {
		// // invest has for this sample in
		// break outer;
		// }
		// }
		// // if here not got sample in
		// throw new ValidationException("Sample[id=" + this.sampleId +
		// "] is not associated with Dataset[id="
		// + this.id + "]'s investigation.");
		// }

		// if (deepValidation) {
		// // check all datafiles now
		// for (final Datafile datafile : datafiles) {
		// datafile.isValid(manager);
		// }
		//
		// // check all datasetParameter now
		// for (final DatasetParameter datasetParameter :
		// this.datasetParameters) {
		// datasetParameter.isValid(manager);
		// }
		// }
		//
		// // check is valid status
		// if (this.datasetStatus != null) {
		// // datasetStatus.isValid(manager);
		//
		// // check datafile format is valid
		// final DatasetStatus status = manager.find(DatasetStatus.class,
		// this.datasetStatus);
		// if (status == null) {
		// throw new ValidationException(this.datasetStatus +
		// " is not a valid DatasetStatus");
		// }
		// }
		//
		// // check is valid status
		// if (this.datasetType != null) {
		// // datasetType.isValid(manager);
		//
		// // check datafile format is valid
		// final DatasetType type = manager.find(DatasetType.class,
		// this.datasetType);
		// if (type == null) {
		// throw new ValidationException(this.datasetType +
		// " is not a valid DatasetType");
		// }
		// }
	}

	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
		for (final DatasetParameter datasetParameter : this.datasetParameters) {
			datasetParameter.preparePersist(modId, manager);
			datasetParameter.setDataset(this);
		}
		for (final Datafile datafile : this.datafiles) {
			datafile.preparePersist(modId, manager);
			datafile.setDataset(this);
		}
	}

	public List<Datafile> getDatafiles() {
		return datafiles;
	}

	public void setDatafiles(List<Datafile> datafiles) {
		this.datafiles = datafiles;
	}

	public List<DatasetParameter> getDatasetParameters() {
		return datasetParameters;
	}

	public void setDatasetParameters(List<DatasetParameter> datasetParameters) {
		this.datasetParameters = datasetParameters;
	}

	public List<InputDataset> getInputDatasets() {
		return inputDatasets;
	}

	public void setInputDatasets(List<InputDataset> inputDatasets) {
		this.inputDatasets = inputDatasets;
	}

	public List<OutputDataset> getOutputDatasets() {
		return outputDatasets;
	}

	public void setOutputDatasets(List<OutputDataset> outputDatasets) {
		this.outputDatasets = outputDatasets;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public String toString() {
		return "Dataset[id=" + this.id + "]";
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Dataset for " + includes);
		if (!this.includes.contains(InputDataset.class)) {
			this.inputDatasets = null;
		}
		if (!this.includes.contains(OutputDataset.class)) {
			this.outputDatasets = null;
		}
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
		if (!this.includes.contains(Datafile.class)) {
			this.datafiles = null;
		}
		if (!this.includes.contains(DatasetParameter.class)) {
			this.datasetParameters = null;
		}
		if (!this.includes.contains(Sample.class)) {
			this.sample = null;
		}
		if (!this.includes.contains(DatasetStatus.class)) {
			this.status = null;
		}
		if (!this.includes.contains(DatasetType.class)) {
			this.type = null;
		}
	}

	public DatasetStatus getStatus() {
		return status;
	}

	public void setStatus(DatasetStatus status) {
		this.status = status;
	}

	public DatasetType getType() {
		return type;
	}

	public void setType(DatasetType type) {
		this.type = type;
	}

}
