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
import org.icatproject.core.manager.SearchApi;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;

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
	public void getDoc(JsonGenerator gen, SearchApi searchApi) {
		StringBuilder sb = new StringBuilder(visitId + " " + name + " " + facility.getName() + " " + type.getName());
		if (summary != null) {
			sb.append(" " + summary);
		}
		if (doi != null) {
			sb.append(" " + doi);
		}
		if (title != null) {
			sb.append(" " + title);
		}
		searchApi.encodeTextField(gen, "text", sb.toString());
		searchApi.encodeSortedDocValuesField(gen, "name", name);

		if (startDate != null) {
			searchApi.encodeSortedDocValuesField(gen, "startDate", startDate);
		} else {
			searchApi.encodeSortedDocValuesField(gen, "startDate", createTime);
		}

		if (endDate != null) {
			searchApi.encodeSortedDocValuesField(gen, "endDate", endDate);
		} else {
			searchApi.encodeSortedDocValuesField(gen, "endDate", modTime);
		}

		investigationUsers.forEach((investigationUser) -> {
			searchApi.encodeStringField(gen, "userName", investigationUser.getUser().getName());
			searchApi.encodeTextField(gen, "userFullName", investigationUser.getUser().getFullName());
		});

		samples.forEach((sample) -> {
			// searchApi.encodeSortedSetDocValuesFacetField(gen, "sampleName",
			// sample.getName());
			searchApi.encodeTextField(gen, "sampleText", sample.getDocText());
		});

		for (InvestigationParameter parameter : parameters) {
			ParameterType type = parameter.type;
			String parameterName = type.getName();
			String parameterUnits = type.getUnits();
			// searchApi.encodeSortedSetDocValuesFacetField(gen, "parameterName",
			// parameterName);
			searchApi.encodeStringField(gen, "parameterName", parameterName);
			searchApi.encodeStringField(gen, "parameterUnits", parameterUnits);
			// TODO make all value types facetable...
			if (type.getValueType() == ParameterValueType.STRING) {
				// searchApi.encodeSortedSetDocValuesFacetField(gen, "parameterStringValue",
				// parameter.getStringValue());
				searchApi.encodeStringField(gen, "parameterStringValue", parameter.getStringValue());
			} else if (type.getValueType() == ParameterValueType.DATE_AND_TIME) {
				searchApi.encodeStringField(gen, "parameterDateValue", parameter.getDateTimeValue());
			} else if (type.getValueType() == ParameterValueType.NUMERIC) {
				searchApi.encodeDoublePoint(gen, "parameterNumericValue", parameter.getNumericValue());
			}
		}

		searchApi.encodeSortedDocValuesField(gen, "id", id);

		searchApi.encodeStringField(gen, "id", id, true);
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
			Relationship[] textRelationships = { eiHandler.getRelationshipsByName(Investigation.class).get("type"),
					eiHandler.getRelationshipsByName(Investigation.class).get("facility") };
			documentFields.put("text", textRelationships);
			documentFields.put("name", null);
			documentFields.put("startDate", null);
			documentFields.put("endDate", null);
			documentFields.put("id", null);
		}
		return documentFields;
	}
}
