package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Comment("A study which may be related to an investigation")
@SuppressWarnings("serial")
@Entity
public class Study extends EntityBaseBean implements Serializable {

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Comment("The start date of this study")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Comment("The end date of this study")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

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

	@Comment("A persistent identifier attributed to this study")
	private String pid;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "study")
	private List<StudyInvestigation> studyInvestigations = new ArrayList<>();;

	/* Needed for JPA */
	public Study() {
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

}
