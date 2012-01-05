package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "inputDatasetGenerator", pkColumnValue = "InputDataset")
public class InputDataset extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(InputDataset.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "inputDatasetGenerator")
	private Long id;

	public Long getId() {
		return this.id;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public InputDataset() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	private Job job;

	public Job getJob() {
		return this.job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	private Dataset dataset;

	public Dataset getDataset() {
		return this.dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Application for " + includes);
		if (!this.includes.contains(Job.class)) {
			this.job = null;
		}
		if (!this.includes.contains(Dataset.class)) {
			this.dataset = null;
		}
	}

}
