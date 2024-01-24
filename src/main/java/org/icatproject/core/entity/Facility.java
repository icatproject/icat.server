package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("An experimental facility")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class Facility extends EntityBaseBean implements Serializable {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<Application> applications = new ArrayList<Application>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<DatafileFormat> datafileFormats = new ArrayList<DatafileFormat>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<DatasetType> datasetTypes = new ArrayList<DatasetType>();

	@Comment("The number of days before data is made freely available after collecting it.")
	private Integer daysUntilRelease;

	@Comment("A description of this facility")
	@Column(length = 1023)
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<FacilityCycle> facilityCycles = new ArrayList<FacilityCycle>();

	@Comment("The full name of the facility")
	private String fullName;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<Instrument> instruments = new ArrayList<Instrument>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<Investigation> investigations = new ArrayList<Investigation>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<InvestigationType> investigationTypes = new ArrayList<InvestigationType>();

	@Comment("A short name identifying this facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<ParameterType> parameterTypes = new ArrayList<ParameterType>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<SampleType> sampleTypes = new ArrayList<SampleType>();

	@Comment("A URL associated with this facility")
	private String url;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<DataPublication> dataPublications = new ArrayList<DataPublication>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<DataPublicationType> dataPublicationTypes = new ArrayList<DataPublicationType>();

	/* Needed for JPA */
	public Facility() {
	}

	public List<Application> getApplications() {
		return applications;
	}

	public List<DatafileFormat> getDatafileFormats() {
		return datafileFormats;
	}

	public List<DatasetType> getDatasetTypes() {
		return datasetTypes;
	}

	public Integer getDaysUntilRelease() {
		return this.daysUntilRelease;
	}

	public String getDescription() {
		return this.description;
	}

	public List<FacilityCycle> getFacilityCycles() {
		return facilityCycles;
	}

	public String getFullName() {
		return this.fullName;
	}

	public List<Instrument> getInstruments() {
		return instruments;
	}

	public List<Investigation> getInvestigations() {
		return this.investigations;
	}

	public List<InvestigationType> getInvestigationTypes() {
		return investigationTypes;
	}

	public String getName() {
		return this.name;
	}

	public List<ParameterType> getParameterTypes() {
		return parameterTypes;
	}

	public List<SampleType> getSampleTypes() {
		return sampleTypes;
	}

	public String getUrl() {
		return url;
	}

	public List<DataPublication> getDataPublications() {
		return dataPublications;
	}

	public List<DataPublicationType> getDataPublicationTypes() {
		return dataPublicationTypes;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

	public void setDatafileFormats(List<DatafileFormat> datafileFormats) {
		this.datafileFormats = datafileFormats;
	}

	public void setDatasetTypes(List<DatasetType> datasetTypes) {
		this.datasetTypes = datasetTypes;
	}

	public void setDaysUntilRelease(Integer daysUntilRelease) {
		this.daysUntilRelease = daysUntilRelease;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFacilityCycles(List<FacilityCycle> facilityCycles) {
		this.facilityCycles = facilityCycles;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setInstruments(List<Instrument> instruments) {
		this.instruments = instruments;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	public void setInvestigationTypes(List<InvestigationType> investigationTypes) {
		this.investigationTypes = investigationTypes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameterTypes(List<ParameterType> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public void setSampleTypes(List<SampleType> sampleTypes) {
		this.sampleTypes = sampleTypes;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setDataPublications(List<DataPublication> dataPublications) {
		this.dataPublications = dataPublications;
	}

	public void setDataPublicationTypes(List<DataPublicationType> dataPublicationTypes) {
		this.dataPublicationTypes = dataPublicationTypes;
	}

}
