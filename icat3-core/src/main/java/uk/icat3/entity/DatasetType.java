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
public class DatasetType extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(DatasetType.class);

	@Id
	private String name;

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

	@OneToMany(mappedBy = "type")
	private List<Dataset> datasets = new ArrayList<Dataset>();

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	private String description;

	/* Needed for JPA */
	public DatasetType() {
	}

	@Override
	public String toString() {
		return "DatasetType[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatasetType for " + includes);
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
	}

}
