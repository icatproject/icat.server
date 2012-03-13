package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("A type of data set")
@SuppressWarnings("serial")
@Entity
public class DatasetType extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(DatasetType.class);

	@OneToMany(mappedBy = "type")
	private List<Dataset> datasets = new ArrayList<Dataset>();

	@Comment("A description of this data set type")
	private String description;

	@Comment("A short name identifying this data set type")
	@Id
	private String name;

	/* Needed for JPA */
	public DatasetType() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatasetType for " + this.includes);
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.name;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "DatasetType[name=" + this.name + "]";
	}

}
