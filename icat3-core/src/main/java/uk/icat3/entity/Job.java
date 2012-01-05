package uk.icat3.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "jobGenerator", pkColumnValue = "Job")
public class Job extends EntityBaseBean implements Serializable {
	
	private final static Logger logger = Logger.getLogger(Job.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "jobGenerator")
	private Long id;

	@ManyToOne(optional = false)
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

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "job")
	@XmlElement
	private Set<InputDataset> inputDatasets;

	public Set<InputDataset> getInputDatasets() {
		return inputDatasets;
	}

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "job")
	@XmlElement
	private Set<OutputDataset> outputDatasets;

	public Set<OutputDataset> getOutputDatasets() {
		return outputDatasets;
	}

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "job")
	@XmlElement
	private Set<InputDatafile> inputDatafiles;

	public Set<InputDatafile> getInputDatafiles() {
		return inputDatafiles;
	}

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "job")
	@XmlElement
	private Set<OutputDatafile> outputDatafiles;

	public Set<OutputDatafile> getOutputDatafiles() {
		return outputDatafiles;
	}

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
