package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A study which may be related to an investigation")
@SuppressWarnings("serial")
@Entity
public class Study extends EntityBaseBean implements Serializable {

	public enum StudyStatus {
		NEW, IN_PROGRESS, COMPLETE, CANCELLED
	};

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private final static Logger logger = Logger.getLogger(Study.class);

	@Comment("The start date of this study")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Comment("The user responsible for the study")
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Comment("The name of the study")
	@Column(nullable = false)
	private String name;

	@Comment("A description of the study and its purpose")
	@Column(length = 4000)
	private String description;

	@Comment("The status of the study. Possible values are: NEW, IN_PROGRESS, COMPLETE, CANCELLED")
	private StudyStatus status;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "study")
	private List<StudyInvestigation> studyInvestigations;

	/* Needed for JPA */
	public Study() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Study for " + this.includes);

		if (!this.includes.contains(StudyInvestigation.class)) {
			this.studyInvestigations = null;
		}
	}

	public String getName() {
		return this.name;
	}

	public StudyStatus getStatus() {
		return this.status;
	}

	public List<StudyInvestigation> getStudyInvestigations() {
		return this.studyInvestigations;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setStatus(StudyStatus status) {
		this.status = status;
	}

	public void setStudyInvestigations(List<StudyInvestigation> studyInvestigations) {
		this.studyInvestigations = studyInvestigations;
	}

	@Override
	public String toString() {
		return "Study[id=" + this.id + "]";
	}

}
