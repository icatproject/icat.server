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
import javax.xml.bind.annotation.XmlRootElement;

import org.icatproject.core.IcatException;

@Comment("A collection of data files and part of an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SAMPLE_ID", "INVESTIGATION_ID",
		"NAME", "TYPE" }) })
@XmlRootElement
public class Dataset extends EntityBaseBean implements Serializable {

	@Comment("May be set to true when all data files and parameters have been added to the data set. The precise meaning is facility dependent.")
	private boolean complete;

	@Comment("The data files within the dataset")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<Datafile> datafiles = new ArrayList<Datafile>();

	@Comment("An informal description of the data set")
	private String description;

	@Comment("The Digital Object Identifier associated with this data set")
	private String doi;

	@Column(name = "END_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<InputDataset> inputDatasets;

	@JoinColumn(name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Comment("Identifies a location from which all the files of the data set might be accessed. It might be a directory")
	private String location;

	@Comment("A short name for the data set")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<OutputDataset> outputDatasets;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<DatasetParameter> parameters = new ArrayList<DatasetParameter>();

	@JoinColumn(name = "SAMPLE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Sample sample;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@JoinColumn(name = "TYPE", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DatasetType type;

	/* Needed for JPA */
	public Dataset() {
	}

	@Override
	public void canDelete(EntityManager manager) throws IcatException {
		super.canDelete(manager);
		if (!this.inputDatasets.isEmpty()) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Datasets may not be deleted while there are related InputDatasets");
		}
	}

	public List<Datafile> getDatafiles() {
		return datafiles;
	}

	public String getDescription() {
		return this.description;
	}

	public String getDoi() {
		return doi;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public List<InputDataset> getInputDatasets() {
		return inputDatasets;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public String getLocation() {
		return this.location;
	}

	public String getName() {
		return this.name;
	}

	public List<OutputDataset> getOutputDatasets() {
		return outputDatasets;
	}

	public List<DatasetParameter> getParameters() {
		return parameters;
	}

	public Sample getSample() {
		return sample;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public DatasetType getType() {
		return type;
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws IcatException {
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

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
		for (final DatasetParameter datasetParameter : this.parameters) {
			datasetParameter.preparePersist(modId, manager);
			datasetParameter.setDataset(this);
		}
		for (final Datafile datafile : this.datafiles) {
			datafile.preparePersist(modId, manager);
			datafile.setDataset(this);
		}
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public void setDatafiles(List<Datafile> datafiles) {
		this.datafiles = datafiles;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setInputDatasets(List<InputDataset> inputDatasets) {
		this.inputDatasets = inputDatasets;
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

	public void setOutputDatasets(List<OutputDataset> outputDatasets) {
		this.outputDatasets = outputDatasets;
	}

	public void setParameters(List<DatasetParameter> parameters) {
		this.parameters = parameters;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setType(DatasetType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Dataset[id=" + this.id + "]";
	}

}
