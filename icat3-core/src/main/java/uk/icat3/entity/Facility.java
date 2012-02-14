package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
public class Facility extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Facility.class);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDaysUntilRelease() {
		return daysUntilRelease;
	}

	public void setDaysUntilRelease(Integer daysUntilRelease) {
		this.daysUntilRelease = daysUntilRelease;
	}

	@Id
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
	private List<Investigation> investigations = new ArrayList<Investigation>();

	private String fullName;

	private String url;

	private String description;

	@Column(nullable = false)
	private Integer daysUntilRelease;

	/* Needed for JPA */
	public Facility() {
	}

	@Override
	public String toString() {
		return "Facility[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Investigation for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigations = null;
		}

	}

}
