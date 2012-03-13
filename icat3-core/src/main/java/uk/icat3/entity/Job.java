package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("A run of an application with its related inputs and outputs")
@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "jobGenerator", pkColumnValue = "Job")
public class Job extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Job.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "jobGenerator")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Application application;

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Job for " + includes);
		if (!includes.contains(Application.class)) {
			this.application = null;
		}
		if (!includes.contains(InputDataset.class)) {
			this.inputDatasets = null;
		}
		if (!includes.contains(OutputDataset.class)) {
			this.outputDatasets = null;
		}
		if (!includes.contains(InputDatafile.class)) {
			this.inputDatafiles = null;
		}
		if (!includes.contains(OutputDatafile.class)) {
			this.outputDatafiles = null;
		}
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<InputDataset> inputDatasets;

	public List<InputDataset> getInputDatasets() {
		return inputDatasets;
	}

	public void setInputDatasets(List<InputDataset> inputDatasets) {
		this.inputDatasets = inputDatasets;
	}

	public void setOutputDatasets(List<OutputDataset> outputDatasets) {
		this.outputDatasets = outputDatasets;
	}

	public void setInputDatafiles(List<InputDatafile> inputDatafiles) {
		this.inputDatafiles = inputDatafiles;
	}

	public void setOutputDatafiles(List<OutputDatafile> outputDatafiles) {
		this.outputDatafiles = outputDatafiles;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<OutputDataset> outputDatasets;

	public List<OutputDataset> getOutputDatasets() {
		return outputDatasets;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<InputDatafile> inputDatafiles;

	public List<InputDatafile> getInputDatafiles() {
		return inputDatafiles;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "job")
	private List<OutputDatafile> outputDatafiles;

	public List<OutputDatafile> getOutputDatafiles() {
		return outputDatafiles;
	}

	// Needed for JPA
	public Job() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Object getPK() {
		return id;
	}

}
