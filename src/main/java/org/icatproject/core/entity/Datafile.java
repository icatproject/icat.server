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
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
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

@Comment("A data file")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATASET_ID", "NAME" }) }, indexes = {
		@Index(columnList = "location") })
public class Datafile extends EntityBaseBean implements Serializable {

	@Comment("Checksum of file represented as a string")
	private String checksum;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<DataCollectionDatafile> dataCollectionDatafiles = new ArrayList<DataCollectionDatafile>();

	@Comment("Date of creation of the actual file rather than storing the metadata")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileCreateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	private DatafileFormat datafileFormat;

	@Comment("Date of modification of the actual file rather than of the metadata")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datafileModTime;

	@Comment("The dataset which holds this file")
	@JoinColumn(name = "DATASET_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Dataset dataset;

	@Comment("A full description of the file contents")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "destDatafile")
	private List<RelatedDatafile> destDatafiles = new ArrayList<RelatedDatafile>();

	@Comment("The Digital Object Identifier associated with this data file")
	private String doi;

	@Comment("Expressed in bytes")
	private Long fileSize = 0L;

	@Comment("The logical location of the file - which may also be the physical location")
	@Column(length = 511)
	private String location;

	@Comment("A name given to the file")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
	private List<DatafileParameter> parameters = new ArrayList<DatafileParameter>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sourceDatafile")
	private List<RelatedDatafile> sourceDatafiles = new ArrayList<RelatedDatafile>();

	private static final Map<String, Relationship[]> documentFields = new HashMap<>();

	/* Needed for JPA */
	public Datafile() {
	}

	public String getChecksum() {
		return checksum;
	}

	public List<DataCollectionDatafile> getDataCollectionDatafiles() {
		return dataCollectionDatafiles;
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

	public Long getFileSize() {
		return fileSize;
	}

	public String getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public List<DatafileParameter> getParameters() {
		return parameters;
	}

	public List<RelatedDatafile> getSourceDatafiles() {
		return sourceDatafiles;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public void setDataCollectionDatafiles(List<DataCollectionDatafile> dataCollectionDatafiles) {
		this.dataCollectionDatafiles = dataCollectionDatafiles;
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

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(List<DatafileParameter> parameters) {
		this.parameters = parameters;
	}

	public void setSourceDatafiles(List<RelatedDatafile> sourceDatafiles) {
		this.sourceDatafiles = sourceDatafiles;
	}

	@Override
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeString(gen, "name", name);
		SearchApi.encodeNullableString(gen, "description", description);
		SearchApi.encodeNullableString(gen, "location", location);
		SearchApi.encodeNullableString(gen, "doi", doi);
		SearchApi.encodeLong(gen, "fileSize", fileSize, 0L);
		SearchApi.encodeLong(gen, "fileCount", 1L); // Always 1, but makes sorting on fields consistent
		if (datafileFormat != null) {
			if (datafileFormat.getName() == null) {
				datafileFormat = entityManager.find(datafileFormat.getClass(), datafileFormat.id);
			}
			datafileFormat.getDoc(entityManager, gen);
		}
		if (datafileModTime != null) {
			SearchApi.encodeLong(gen, "date", datafileModTime);
		} else if (datafileCreateTime != null) {
			SearchApi.encodeLong(gen, "date", datafileCreateTime);
		} else {
			SearchApi.encodeLong(gen, "date", modTime);
		}
		SearchApi.encodeLong(gen, "id", id);

		if (dataset != null) {
			if (dataset.getName() == null || dataset.getInvestigation() == null) {
				dataset = entityManager.find(dataset.getClass(), dataset.id);
			}
			SearchApi.encodeLong(gen, "dataset.id", dataset.id);
			SearchApi.encodeString(gen, "dataset.name", dataset.getName());
			Sample sample = dataset.getSample();
			if (sample != null) {
				if (sample.getName() == null) {
					sample = entityManager.find(sample.getClass(), sample.id);
				}
				sample.getDoc(entityManager, gen);
			}
			Investigation investigation = dataset.getInvestigation();
			if (investigation != null) {
				if (investigation.getName() == null || investigation.getVisitId() == null
						|| investigation.getTitle() == null || investigation.getCreateTime() == null) {
					investigation = entityManager.find(investigation.getClass(), investigation.id);
				}
				SearchApi.encodeLong(gen, "investigation.id", investigation.id);
				SearchApi.encodeString(gen, "investigation.name", investigation.getName());
				SearchApi.encodeString(gen, "visitId", investigation.getVisitId());
				if (investigation.getStartDate() != null) {
					SearchApi.encodeLong(gen, "investigation.startDate", investigation.getStartDate());
				} else if (investigation.getCreateTime() != null) {
					SearchApi.encodeLong(gen, "investigation.startDate", investigation.getCreateTime());
				}
			}
		}
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
			Relationship[] datafileFormatRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("datafileFormat") };
			Relationship[] datasetRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset") };
			Relationship[] investigationRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset"),
					EntityInfoHandler.getRelationshipsByName(Dataset.class).get("investigation") };
			Relationship[] investigationFacilityCyclesRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset"),
					EntityInfoHandler.getRelationshipsByName(Dataset.class).get("investigation"),
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("investigationFacilityCycles") };
			Relationship[] instrumentRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset"),
					EntityInfoHandler.getRelationshipsByName(Dataset.class).get("investigation"),
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("investigationInstruments"),
					EntityInfoHandler.getRelationshipsByName(InvestigationInstrument.class).get("instrument") };
			Relationship[] sampleRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset"),
					EntityInfoHandler.getRelationshipsByName(Dataset.class).get("sample"),
					EntityInfoHandler.getRelationshipsByName(Sample.class).get("type") };
			Relationship[] sampleTypeRelationships = {
					EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset"),
					EntityInfoHandler.getRelationshipsByName(Dataset.class).get("sample") };
			documentFields.put("name", null);
			documentFields.put("description", null);
			documentFields.put("location", null);
			documentFields.put("doi", null);
			documentFields.put("date", null);
			documentFields.put("fileSize", null);
			documentFields.put("fileCount", null);
			documentFields.put("id", null);
			documentFields.put("dataset.id", null);
			documentFields.put("dataset.name", datasetRelationships);
			documentFields.put("sample.id", datasetRelationships);
			documentFields.put("sample.name", sampleRelationships);
			documentFields.put("sample.investigation.id", sampleRelationships);
			documentFields.put("sample.type.id", sampleRelationships);
			documentFields.put("sample.type.name", sampleTypeRelationships);
			documentFields.put("investigation.id", datasetRelationships);
			documentFields.put("investigation.name", investigationRelationships);
			documentFields.put("investigation.startDate", investigationRelationships);
			documentFields.put("visitId", investigationRelationships);
			documentFields.put("datafileFormat.id", null);
			documentFields.put("datafileFormat.name", datafileFormatRelationships);
			documentFields.put("InvestigationFacilityCycle facilityCycle.id", investigationFacilityCyclesRelationships);
			documentFields.put("InvestigationInstrument instrument.id", instrumentRelationships);
		}
		return documentFields;
	}

}
