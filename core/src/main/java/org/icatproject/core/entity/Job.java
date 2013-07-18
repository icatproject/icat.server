package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Comment("A run of an application with its related inputs and outputs")
@SuppressWarnings("serial")
@Entity
public class Job extends EntityBaseBean implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Application application;

	private String arguments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private DataCollection inputDataCollection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private DataCollection outputDataCollection;

	// Needed for JPA
	public Job() {
	}

	public Application getApplication() {
		return application;
	}

	public String getArguments() {
		return arguments;
	}

	public DataCollection getInputDataCollection() {
		return inputDataCollection;
	}

	public DataCollection getOutputDataCollection() {
		return outputDataCollection;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void setInputDataCollection(DataCollection inputDataCollection) {
		this.inputDataCollection = inputDataCollection;
	}

	public void setOutputDataCollection(DataCollection outputDataCollection) {
		this.outputDataCollection = outputDataCollection;
	}

}
