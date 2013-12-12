package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Comment("A set of Datafiles and Datasets which can span investigations and facilities. Note that it has "
		+ "no constraint fields. "
		+ "It is expected that a DataCollection would be identified by its DataCollectionParameters or its "
		+ "relationship to a Job.")
@SuppressWarnings("serial")
@Entity
public class DataCollection extends EntityBaseBean implements Serializable {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionDatafile> dataCollectionDatafiles = new ArrayList<DataCollectionDatafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionDataset> dataCollectionDatasets = new ArrayList<DataCollectionDataset>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataCollection")
	private List<DataCollectionParameter> parameters = new ArrayList<DataCollectionParameter>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "inputDataCollection")
	private List<Job> jobsAsInput = new ArrayList<Job>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "outputDataCollection")
	private List<Job> jobsAsOutput = new ArrayList<Job>();

	public List<DataCollectionDatafile> getDataCollectionDatafiles() {
		return dataCollectionDatafiles;
	}

	public List<DataCollectionDataset> getDataCollectionDatasets() {
		return dataCollectionDatasets;
	}

	public List<DataCollectionParameter> getParameters() {
		return parameters;
	}

	public List<Job> getJobsAsInput() {
		return jobsAsInput;
	}

	public List<Job> getJobsAsOutput() {
		return jobsAsOutput;
	}

	public void setDataCollectionDatafiles(List<DataCollectionDatafile> dataCollectionDatafiles) {
		this.dataCollectionDatafiles = dataCollectionDatafiles;
	}

	public void setDataCollectionDatasets(List<DataCollectionDataset> dataCollectionDatasets) {
		this.dataCollectionDatasets = dataCollectionDatasets;
	}

	public void setParameters(List<DataCollectionParameter> parameters) {
		this.parameters = parameters;
	}

	public void setJobsAsInput(List<Job> jobsAsInput) {
		this.jobsAsInput = jobsAsInput;
	}

	public void setJobsAsOutput(List<Job> jobsAsOutput) {
		this.jobsAsOutput = jobsAsOutput;
	}

}
