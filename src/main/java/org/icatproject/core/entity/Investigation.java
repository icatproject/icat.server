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

import org.icatproject.core.manager.LuceneApi;

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
	private List<InvestigationSample> samples = new ArrayList<>();

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

	public List<InvestigationSample> getSamples() {
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

	public void setSamples(List<InvestigationSample> samples) {
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
	public void getDoc(JsonGenerator gen) {
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

		LuceneApi.encodeSortedDocValuesField(gen, "id", id);

		LuceneApi.encodeStoredId(gen, id);
	}
}
