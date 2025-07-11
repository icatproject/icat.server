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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.search.SearchApi;

@Comment("An investigation or experiment")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME", "VISIT_ID" }) })
public class Investigation extends EntityBaseBean implements Serializable {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Dataset> datasets = new ArrayList<>();

	@Comment("The cumulative total size of the datasets within this investigation")
	private Long fileSize = 0L;

	@Comment("The total number of datafiles within this investigation")
	private Long fileCount = 0L;

	@Comment("The Digital Object Identifier associated with this investigation")
	private String doi;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationGroup> investigationGroups = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationInstrument> investigationInstruments = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationUser> investigationUsers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Keyword> keywords = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationFacilityCycle> investigationFacilityCycles = new ArrayList<>();

	@Comment("A short name for the investigation")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationParameter> parameters = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Publication> publications = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationFunding> fundingReferences = new ArrayList<>();

	@Comment("When the data will be made freely available")
	@Temporal(TemporalType.TIMESTAMP)
	private Date releaseDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Sample> samples = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Shift> shifts = new ArrayList<>();

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<StudyInvestigation> studyInvestigations = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<DataCollectionInvestigation> dataCollectionInvestigations = new ArrayList<DataCollectionInvestigation>();

	@Comment("Summary or abstract")
	@Column(length = 4000)
	private String summary;

	@Comment("Full title of the investigation")
	@Column(nullable = false)
	private String title;

	@JoinColumn(nullable = false)
	@ManyToOne
	private InvestigationType type;

	@Comment("Identifier for the visit to which this investigation is related")
	@Column(name = "VISIT_ID", nullable = false)
	private String visitId;

	private static final Map<String, Relationship[]> documentFields = new HashMap<>();

	/* Needed for JPA */
	public Investigation() {
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public Long getFileCount() {
		return fileCount;
	}

	public String getDoi() {
		return doi;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Facility getFacility() {
		return facility;
	}

	public List<InvestigationGroup> getInvestigationGroups() {
		return investigationGroups;
	}

	public List<InvestigationInstrument> getInvestigationInstruments() {
		return investigationInstruments;
	}

	public List<InvestigationUser> getInvestigationUsers() {
		return investigationUsers;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public List<InvestigationFacilityCycle> getInvestigationFacilityCycles() {
		return investigationFacilityCycles;
	}

	public String getName() {
		return name;
	}

	public List<InvestigationParameter> getParameters() {
		return parameters;
	}

	public List<Publication> getPublications() {
		return publications;
	}

	public List<InvestigationFunding> getFundingReferences() {
		return fundingReferences;
	}

	public Date getReleaseDate() {
		return this.releaseDate;
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public List<Shift> getShifts() {
		return shifts;
	}

	public Date getStartDate() {
		return startDate;
	}

	public List<StudyInvestigation> getStudyInvestigations() {
		return studyInvestigations;
	}

	public List<DataCollectionInvestigation> getDataCollectionInvestigations() {
		return dataCollectionInvestigations;
	}

	public String getSummary() {
		return summary;
	}

	public String getTitle() {
		return this.title;
	}

	public InvestigationType getType() {
		return type;
	}

	public String getVisitId() {
		return this.visitId;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setFileCount(Long fileCount) {
		this.fileCount = fileCount;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setInvestigationGroups(List<InvestigationGroup> investigationGroups) {
		this.investigationGroups = investigationGroups;
	}

	public void setInvestigationInstruments(List<InvestigationInstrument> investigationInstruments) {
		this.investigationInstruments = investigationInstruments;
	}

	public void setInvestigationUsers(List<InvestigationUser> investigationUsers) {
		this.investigationUsers = investigationUsers;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public void setInvestigationFacilityCycles(List<InvestigationFacilityCycle> investigationFacilityCycles) {
		this.investigationFacilityCycles = investigationFacilityCycles;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(List<InvestigationParameter> parameters) {
		this.parameters = parameters;
	}

	public void setPublications(List<Publication> publications) {
		this.publications = publications;
	}

	public void setFundingReferences(List<InvestigationFunding> fundingReferences) {
		this.fundingReferences = fundingReferences;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setStudyInvestigations(List<StudyInvestigation> studyInvestigations) {
		this.studyInvestigations = studyInvestigations;
	}

	public void setDataCollectionInvestigations(List<DataCollectionInvestigation> dataCollectionInvestigations) {
		this.dataCollectionInvestigations = dataCollectionInvestigations;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(InvestigationType type) {
		this.type = type;
	}

	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}

	@Override
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeString(gen, "name", name);
		SearchApi.encodeString(gen, "visitId", visitId);
		SearchApi.encodeString(gen, "title", title);
		SearchApi.encodeNullableString(gen, "summary", summary);
		SearchApi.encodeNullableString(gen, "doi", doi);

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
		SearchApi.encodeLong(gen, "fileSize", fileSize, 0L);
		SearchApi.encodeLong(gen, "fileCount", fileCount, 0L);

		SearchApi.encodeLong(gen, "id", id);

		if (facility.getName() == null) {
			facility = entityManager.find(facility.getClass(), facility.id);
		}
		facility.getDoc(entityManager, gen);

		if (type.getName() == null) {
			type = entityManager.find(type.getClass(), type.id);
		}
		type.getDoc(entityManager, gen);
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
			Relationship[] typeRelationships = { EntityInfoHandler.getRelationshipsByName(Investigation.class).get("type") };
			Relationship[] facilityRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("facility") };
			Relationship[] investigationFacilityCyclesRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("investigationFacilityCycles") };
			Relationship[] instrumentRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("investigationInstruments"),
					EntityInfoHandler.getRelationshipsByName(InvestigationInstrument.class).get("instrument") };
			Relationship[] parameterRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("parameters") };
			Relationship[] parameterTypeRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("parameters"),
					EntityInfoHandler.getRelationshipsByName(InvestigationParameter.class).get("type") };
			Relationship[] sampleRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("samples") };
			Relationship[] sampleTypeRelationships = {
					EntityInfoHandler.getRelationshipsByName(Investigation.class).get("samples"),
					EntityInfoHandler.getRelationshipsByName(Sample.class).get("type") };
			documentFields.put("name", null);
			documentFields.put("visitId", null);
			documentFields.put("title", null);
			documentFields.put("summary", null);
			documentFields.put("doi", null);
			documentFields.put("startDate", null);
			documentFields.put("endDate", null);
			documentFields.put("date", null);
			documentFields.put("fileSize", null);
			documentFields.put("fileCount", null);
			documentFields.put("id", null);
			documentFields.put("facility.name", facilityRelationships);
			documentFields.put("facility.id", null);
			documentFields.put("type.name", typeRelationships);
			documentFields.put("type.id", null);
			documentFields.put("InvestigationFacilityCycle facilityCycle.id", investigationFacilityCyclesRelationships);
			documentFields.put("InvestigationInstrument instrument.fullName", instrumentRelationships);
			documentFields.put("InvestigationInstrument instrument.id", instrumentRelationships);
			documentFields.put("InvestigationInstrument instrument.name", instrumentRelationships);
			documentFields.put("InvestigationParameter type.name", parameterTypeRelationships);
			documentFields.put("InvestigationParameter stringValue", parameterRelationships);
			documentFields.put("InvestigationParameter numericValue", parameterRelationships);
			documentFields.put("InvestigationParameter dateTimeValue", parameterRelationships);
			documentFields.put("Sample sample.id", sampleRelationships);
			documentFields.put("Sample sample.name", sampleRelationships);
			documentFields.put("Sample type.name", sampleTypeRelationships);
		}
		return documentFields;
	}
}
