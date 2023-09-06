package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Many to many relationship between study and investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "STUDY_ID", "INVESTIGATION_ID" }) })
public class StudyInvestigation extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "STUDY_ID", nullable = false)
	@ManyToOne
	private Study study;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	/* Needed for JPA */
	public StudyInvestigation() {
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

}
