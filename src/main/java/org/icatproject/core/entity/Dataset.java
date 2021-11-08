package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.stream.JsonGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.icatproject.core.manager.LuceneApi;

@Comment("A collection of data files and part of an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "NAME" }) })
@XmlRootElement
public class Dataset extends EntityBaseBean implements Serializable {

	@Comment("May be set to true when all data files and parameters have been added to the data set. The precise meaning is facility dependent.")
	@Column(nullable = false)
	private boolean complete;

	@Comment("The data files within the dataset")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<Datafile> datafiles = new ArrayList<Datafile>();

	@Comment("The cumulated sizes of the data files within this dataset")
	private Long fileSize;

	@Comment("The total number of datafiles within this dataset")
	private Long fileCount;

	@Comment("An informal description of the data set")
	private String description;

	@Comment("The Digital Object Identifier associated with this data set")
	private String doi;

	@Column(name = "END_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	public List<DataCollectionDataset> getDataCollectionDatasets() {
		return dataCollectionDatasets;
	}

	public void setDataCollectionDatasets(List<DataCollectionDataset> dataCollectionDatasets) {
		this.dataCollectionDatasets = dataCollectionDatasets;
	}

	@Comment("Identifies a location from which all the files of the data set might be accessed. It might be a directory")
	private String location;

	@Comment("A short name for the data set")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<DataCollectionDataset> dataCollectionDatasets = new ArrayList<DataCollectionDataset>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<DatasetInstrument> datasetInstruments = new ArrayList<DatasetInstrument>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<DatasetTechnique> datasetTechniques = new ArrayList<DatasetTechnique>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
	private List<DatasetParameter> parameters = new ArrayList<DatasetParameter>();

	@ManyToOne(fetch = FetchType.LAZY)
	private Sample sample;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DatasetType type;

	/* Needed for JPA */
	public Dataset() {
	}

	public List<Datafile> getDatafiles() {
		return datafiles;
	}

	public Long getFileSize() {
		return this.fileSize;
	}

	public Long getFileCount() {
		return this.fileCount;
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

	public Investigation getInvestigation() {
		return investigation;
	}

	public String getLocation() {
		return this.location;
	}

	public String getName() {
		return this.name;
	}

	public List<DatasetInstrument> getDatasetInstruments() {
		return datasetInstruments;
	}

	public List<DatasetTechnique> getDatasetTechniques() {
		return datasetTechniques;
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

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public void setDatafiles(List<Datafile> datafiles) {
		this.datafiles = datafiles;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setFileCount(Long fileCount) {
		this.fileCount = fileCount;
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

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDatasetInstruments(List<DatasetInstrument> datasetInstruments) {
		this.datasetInstruments = datasetInstruments;
	}

	public void setDatasetTechniques(List<DatasetTechnique> datasetTechniques) {
		this.datasetTechniques = datasetTechniques;
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
	public void getDoc(JsonGenerator gen) {

		StringBuilder sb = new StringBuilder(name + " " + type.getName() + " " + type.getName());
		if (description != null) {
			sb.append(" " + description);
		}

		if (doi != null) {
			sb.append(" " + doi);
		}

		if (sample != null) {
			sb.append(" " + sample.getName());
			if (sample.getType() != null) {
				sb.append(" " + sample.getType().getName());
			}
		}

		LuceneApi.encodeTextfield(gen, "text", sb.toString());

		if (startDate != null) {
			LuceneApi.encodeStringField(gen, "startDate", startDate);
		} else {
			LuceneApi.encodeStringField(gen, "startDate", createTime);
		}

		if (endDate != null) {
			LuceneApi.encodeStringField(gen, "endDate", endDate);
		} else {
			LuceneApi.encodeStringField(gen, "endDate", modTime);
		}
		LuceneApi.encodeStoredId(gen, id);

		LuceneApi.encodeSortedDocValuesField(gen, "id", id);

		LuceneApi.encodeStringField(gen, "investigation", investigation.id);

	}

}
