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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

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

	@Comment("A short name for the investigation")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<InvestigationParameter> parameters = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
	private List<Publication> publications = new ArrayList<>();

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

	public String getName() {
		return name;
	}

	public List<InvestigationParameter> getParameters() {
		return parameters;
	}

	public List<Publication> getPublications() {
		return publications;
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

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(List<InvestigationParameter> parameters) {
		this.parameters = parameters;
	}

	public void setPublications(List<Publication> publications) {
		this.publications = publications;
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
	public void getDoc(JsonGenerator gen) {
		SearchApi.encodeString(gen, "name", name);
		SearchApi.encodeString(gen, "visitId", visitId);
		SearchApi.encodeString(gen, "title", title);
		if (summary != null) {
			SearchApi.encodeString(gen, "summary", summary);
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
		SearchApi.encodeLong(gen, "fileSize", -1L); // This is a placeholder to allow us to dynamically build size

		SearchApi.encodeString(gen, "id", id);
		facility.getDoc(gen);
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
			Relationship[] typeRelationships = { eiHandler.getRelationshipsByName(Investigation.class).get("type") };
			Relationship[] facilityRelationships = {
					eiHandler.getRelationshipsByName(Investigation.class).get("facility") };
			Relationship[] instrumentRelationships = {
					eiHandler.getRelationshipsByName(Investigation.class).get("investigationInstruments"),
					eiHandler.getRelationshipsByName(InvestigationInstrument.class).get("instrument") };
			Relationship[] parameterRelationships = {
					eiHandler.getRelationshipsByName(Investigation.class).get("parameters") };
			Relationship[] parameterTypeRelationships = {
					eiHandler.getRelationshipsByName(Investigation.class).get("parameters"),
					eiHandler.getRelationshipsByName(InvestigationParameter.class).get("type") };
			Relationship[] sampleRelationships = {
					eiHandler.getRelationshipsByName(Investigation.class).get("samples"),
					eiHandler.getRelationshipsByName(Sample.class).get("type") };
			documentFields.put("name", null);
			documentFields.put("visitId", null);
			documentFields.put("title", null);
			documentFields.put("summary", null);
			documentFields.put("doi", null);
			documentFields.put("startDate", null);
			documentFields.put("endDate", null);
			documentFields.put("date", null);
			documentFields.put("fileSize", null);
			documentFields.put("id", null);
			documentFields.put("facility.name", facilityRelationships);
			documentFields.put("facility.id", null);
			documentFields.put("type.name", typeRelationships);
			documentFields.put("type.id", null);
			documentFields.put("InvestigationInstrument instrument.fullName", instrumentRelationships);
			documentFields.put("InvestigationInstrument instrument.id", instrumentRelationships);
			documentFields.put("InvestigationInstrument instrument.name", instrumentRelationships);
			documentFields.put("InvestigationParameter type.name", parameterTypeRelationships);
			documentFields.put("InvestigationParameter stringValue", parameterRelationships);
			documentFields.put("InvestigationParameter numericValue", parameterRelationships);
			documentFields.put("InvestigationParameter dateTimeValue", parameterRelationships);
			documentFields.put("Sample type.name", sampleRelationships);
		}
		return documentFields;
	}
}
