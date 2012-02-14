package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
public class StudyStatus extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(StudyStatus.class);

	@Id
	private String name;

	@Column(nullable = false)
	private String description;

	@OneToMany(mappedBy = "status")
	private List<Study> studies;

	/* Needed for JPA */
	public StudyStatus() {
	}

	@Override
	public String toString() {
		return "StudyStatus[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling StudyStatus for " + includes);

		if (!this.includes.contains(Study.class)) {
			this.studies = null;
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

	public List<Study> getStudies() {
		return studies;
	}

	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}

}
