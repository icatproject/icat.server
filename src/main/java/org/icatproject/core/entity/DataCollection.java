package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Comment("A set of Investigations, Datasets and Datafiles which can span Facilities. "
		+ "Note that it has no constraint fields. "
		+ "It is expected that a DataCollection would be identified by its parameters or its "
		+ "relationship to a Job.")
@SuppressWarnings("serial")
@Entity
public class DataCollection extends EntityBaseBean implements Serializable {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionDatafile> dataCollectionDatafiles = new ArrayList<DataCollectionDatafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionDataset> dataCollectionDatasets = new ArrayList<DataCollectionDataset>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionInvestigation> dataCollectionInvestigations = new ArrayList<DataCollectionInvestigation>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionParameter> parameters = new ArrayList<DataCollectionParameter>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "inputDataCollection")
	private List<Job> jobsAsInput = new ArrayList<Job>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "outputDataCollection")
	private List<Job> jobsAsOutput = new ArrayList<Job>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "content")
	private List<DataPublication> dataPublications = new ArrayList<DataPublication>();

	@Comment("The Digital Object Identifier associated with this data file")
	private String doi;

	public List<DataCollectionDatafile> getDataCollectionDatafiles() {
		return dataCollectionDatafiles;
	}

	public List<DataCollectionDataset> getDataCollectionDatasets() {
		return dataCollectionDatasets;
	}

	public List<DataCollectionInvestigation> getDataCollectionInvestigations() {
		return dataCollectionInvestigations;
	}

	public String getDoi() {
		return doi;
	}

	public List<Job> getJobsAsInput() {
		return jobsAsInput;
	}

	public List<Job> getJobsAsOutput() {
		return jobsAsOutput;
	}

	public List<DataPublication> getDataPublications() {
		return dataPublications;
	}

	public List<DataCollectionParameter> getParameters() {
		return parameters;
	}

	public void setDataCollectionDatafiles(List<DataCollectionDatafile> dataCollectionDatafiles) {
		this.dataCollectionDatafiles = dataCollectionDatafiles;
	}

	public void setDataCollectionDatasets(List<DataCollectionDataset> dataCollectionDatasets) {
		this.dataCollectionDatasets = dataCollectionDatasets;
	}

	public void setDataCollectionInvestigations(List<DataCollectionInvestigation> dataCollectionInvestigations) {
		this.dataCollectionInvestigations = dataCollectionInvestigations;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public void setJobsAsInput(List<Job> jobsAsInput) {
		this.jobsAsInput = jobsAsInput;
	}

	public void setJobsAsOutput(List<Job> jobsAsOutput) {
		this.jobsAsOutput = jobsAsOutput;
	}

	public void setDataPublications(List<DataPublication> dataPublications) {
		this.dataPublications = dataPublications;
	}

	public void setParameters(List<DataCollectionParameter> parameters) {
		this.parameters = parameters;
	}

}
