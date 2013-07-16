package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Comment("A run of an application with its related inputs and outputs")
@SuppressWarnings("serial")
@Entity
public class Job extends EntityBaseBean implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Application application;

	private String arguments;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<InputDatafile> inputDatafiles = new ArrayList<InputDatafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<InputDataset> inputDatasets = new ArrayList<InputDataset>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<OutputDatafile> outputDatafiles = new ArrayList<OutputDatafile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<OutputDataset> outputDatasets = new ArrayList<OutputDataset>();

	// Needed for JPA
	public Job() {
	}

	public Application getApplication() {
		return application;
	}

	public String getArguments() {
		return arguments;
	}

	public List<InputDatafile> getInputDatafiles() {
		return inputDatafiles;
	}

	public List<InputDataset> getInputDatasets() {
		return inputDatasets;
	}

	public List<OutputDatafile> getOutputDatafiles() {
		return outputDatafiles;
	}

	public List<OutputDataset> getOutputDatasets() {
		return outputDatasets;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void setInputDatafiles(List<InputDatafile> inputDatafiles) {
		this.inputDatafiles = inputDatafiles;
	}

	public void setInputDatasets(List<InputDataset> inputDatasets) {
		this.inputDatasets = inputDatasets;
	}

	public void setOutputDatafiles(List<OutputDatafile> outputDatafiles) {
		this.outputDatafiles = outputDatafiles;
	}

	public void setOutputDatasets(List<OutputDataset> outputDatasets) {
		this.outputDatasets = outputDatasets;
	}

}
