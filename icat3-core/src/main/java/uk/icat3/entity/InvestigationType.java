package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
public class InvestigationType extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(InvestigationType.class);

	@Id
	private String name;

	private String description;

	@OneToMany(mappedBy = "type")
	private List<Investigation> investigations;

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	/* Needed for JPA */
	public InvestigationType() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "InvestigationType[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling InvestigationType for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigations = null;
		}
	}

}
