package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("Many to many relationship between study and investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "STUDY_ID", "INVESTIGATION_ID" }) })
public class StudyInvestigation extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(StudyInvestigation.class);

	@JoinColumn(name = "STUDY_ID", nullable = false)
	@ManyToOne
	private Study study;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	/* Needed for JPA */
	public StudyInvestigation() {
	}

	@Override
	public String toString() {
		return "StudyInvestigation[id=" + id + "]";
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling StudyInvestigation for " + includes);
		if (!this.includes.contains(Study.class)) {
			this.study = null;
		}
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
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
