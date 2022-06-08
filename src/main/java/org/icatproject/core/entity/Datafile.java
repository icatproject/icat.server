package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

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
	private Long fileSize;

	@Comment("The logical location of the file - which may also be the physical location")
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
	public void getDoc(JsonGenerator gen) {
		SearchApi.encodeString(gen, "name", name);
		if (description != null) {
			SearchApi.encodeString(gen, "description", description);
		}
		if (location != null) {
			SearchApi.encodeString(gen, "location", location);
		}
		if (doi != null) {
			SearchApi.encodeString(gen, "doi", doi);
		}
		if (datafileFormat != null) {
			datafileFormat.getDoc(gen);
		}
		if (datafileModTime != null) {
			SearchApi.encodeLong(gen, "date", datafileModTime);
		} else if (datafileCreateTime != null) {
			SearchApi.encodeLong(gen, "date", datafileCreateTime);
		} else {
			SearchApi.encodeLong(gen, "date", modTime);
		}
		SearchApi.encodeString(gen, "id", id);
		if (dataset != null) {
			SearchApi.encodeString(gen, "dataset.id", dataset.id);
			SearchApi.encodeString(gen, "dataset.name", dataset.getName());
			Investigation investigation = dataset.getInvestigation();
			if (investigation != null) {
				SearchApi.encodeString(gen, "investigation.id", investigation.id);
				SearchApi.encodeString(gen, "investigation.name", investigation.getName());
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
			EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();
			Relationship[] datafileFormatRelationships = {
					eiHandler.getRelationshipsByName(Datafile.class).get("datafileFormat") };
			Relationship[] datasetRelationships = {
					eiHandler.getRelationshipsByName(Datafile.class).get("dataset") };
			Relationship[] investigationRelationships = {
					eiHandler.getRelationshipsByName(Datafile.class).get("dataset"),
					eiHandler.getRelationshipsByName(Dataset.class).get("investigation") };
			Relationship[] instrumentRelationships = {
					eiHandler.getRelationshipsByName(Investigation.class).get("investigationInstruments"),
					eiHandler.getRelationshipsByName(InvestigationInstrument.class).get("instrument") };
			documentFields.put("name", null);
			documentFields.put("description", null);
			documentFields.put("location", null);
			documentFields.put("doi", null);
			documentFields.put("date", null);
			documentFields.put("id", null);
			documentFields.put("dataset.id", null);
			documentFields.put("dataset.name", datasetRelationships);
			documentFields.put("investigation.id", datasetRelationships);
			documentFields.put("investigation.name", investigationRelationships);
			documentFields.put("datafileFormat.id", null);
			documentFields.put("datafileFormat.name", datafileFormatRelationships);
			documentFields.put("InvestigationInstrument instrument.id", instrumentRelationships);
		}
		return documentFields;
	}

}
