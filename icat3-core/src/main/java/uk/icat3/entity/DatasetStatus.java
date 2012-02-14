package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
public class DatasetStatus extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(DatasetType.class);

	@Id
	private String name;

	private String description;

	@OneToMany(mappedBy = "status")
	private List<Dataset> datasets = new ArrayList<Dataset>();

	/* Needed for JPA */
	public DatasetStatus() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatasetStatus for " + includes);
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	@Override
	public String toString() {
		return "DatasetStatus[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

}
