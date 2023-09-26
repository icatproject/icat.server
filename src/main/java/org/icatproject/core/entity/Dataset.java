package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.search.SearchApi;

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

	@Comment("The cumulative total size of the data files within this dataset")
	private Long fileSize = 0L;

	@Comment("The total number of datafiles within this dataset")
	private Long fileCount = 0L;

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

	private static final Map<String, Relationship[]> documentFields = new HashMap<>();

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
		SearchApi.encodeString(gen, "name", name);
		if (description != null) {
			SearchApi.encodeString(gen, "description", description);
		}
		if (doi != null) {
			SearchApi.encodeString(gen, "doi", doi);
		}
		if (startDate != null) {
			SearchApi.encodeLong(gen, "startDate", startDate);
			SearchApi.encodeLong(gen, "date", startDate);
		} else {
			SearchApi.encodeLong(gen, "startDate", createTime);
			SearchApi.encodeLong(gen, "date", createTime);
		}
		if (endDate != null) {
			SearchApi.encodeLong(gen, "endDate", endDate);
		} else {
			SearchApi.encodeLong(gen, "endDate", modTime);
		}
		SearchApi.encodeLong(gen, "fileSize", fileSize);
		SearchApi.encodeLong(gen, "fileCount", fileCount);
		SearchApi.encodeLong(gen, "id", id);
		if (investigation != null) {
			SearchApi.encodeLong(gen, "investigation.id", investigation.id);
			SearchApi.encodeString(gen, "investigation.name", investigation.getName());
			SearchApi.encodeString(gen, "investigation.title", investigation.getTitle());
			SearchApi.encodeString(gen, "visitId", investigation.getVisitId());
			if (investigation.getStartDate() != null) {
				SearchApi.encodeLong(gen, "investigation.startDate", investigation.getStartDate());
			} else if (investigation.getCreateTime() != null) {
				SearchApi.encodeLong(gen, "investigation.startDate", investigation.getCreateTime());
			}
		}

		if (sample != null) {
			sample.getDoc(gen);
		}
		type.getDoc(gen);
	}

	/**
	 * Gets the fields used in the search component for this entity, and the
	 * relationships that would restrict the content of those fields.
	 * 
	 * @return Map of field names (as they appear on the search document) against
	 *         the Relationships that need to be allowed for that field to be
	 *         viewable. If there are no restrictive relationships, then the value
	 *         will be null.
	 * @throws IcatException If the EntityInfoHandler cannot find one of the
	 *                       Relationships.
	 */
	public static Map<String, Relationship[]> getDocumentFields() throws IcatException {
		if (documentFields.size() == 0) {
			EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();
			Relationship[] sampleRelationships = { eiHandler.getRelationshipsByName(Dataset.class).get("sample") };
			Relationship[] sampleTypeRelationships = { eiHandler.getRelationshipsByName(Dataset.class).get("sample"),
					eiHandler.getRelationshipsByName(Sample.class).get("type") };
			Relationship[] typeRelationships = { eiHandler.getRelationshipsByName(Dataset.class).get("type") };
			Relationship[] investigationRelationships = {
					eiHandler.getRelationshipsByName(Dataset.class).get("investigation") };
			Relationship[] investigationFacilityCyclesRelationships = {
					eiHandler.getRelationshipsByName(Dataset.class).get("investigation"),
					eiHandler.getRelationshipsByName(Investigation.class).get("investigationFacilityCycles") };
			Relationship[] instrumentRelationships = {
					eiHandler.getRelationshipsByName(Dataset.class).get("investigation"),
					eiHandler.getRelationshipsByName(Investigation.class).get("investigationInstruments"),
					eiHandler.getRelationshipsByName(InvestigationInstrument.class).get("instrument") };
			documentFields.put("name", null);
			documentFields.put("description", null);
			documentFields.put("doi", null);
			documentFields.put("startDate", null);
			documentFields.put("endDate", null);
			documentFields.put("date", null);
			documentFields.put("fileSize", null);
			documentFields.put("fileCount", null);
			documentFields.put("id", null);
			documentFields.put("investigation.id", null);
			documentFields.put("investigation.title", investigationRelationships);
			documentFields.put("investigation.name", investigationRelationships);
			documentFields.put("investigation.startDate", investigationRelationships);
			documentFields.put("visitId", investigationRelationships);
			documentFields.put("sample.id", null);
			documentFields.put("sample.name", sampleRelationships);
			documentFields.put("sample.investigation.id", sampleRelationships);
			documentFields.put("sample.type.id", sampleRelationships);
			documentFields.put("sample.type.name", sampleTypeRelationships);
			documentFields.put("type.id", null);
			documentFields.put("type.name", typeRelationships);
			documentFields.put("InvestigationFacilityCycle facilityCycle.id", investigationFacilityCyclesRelationships);
			documentFields.put("InvestigationInstrument instrument.id", instrumentRelationships);
		}
		return documentFields;
	}

}
