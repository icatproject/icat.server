package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

@Comment("Many to many relationship between study and investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "STUDY_ID", "INVESTIGATION_ID" }) })
@TableGenerator(name = "studyInvestigationGenerator", pkColumnValue = "StudyInvestigation")
public class StudyInvestigation extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(StudyInvestigation.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "studyInvestigationGenerator")
	private Long id;

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
	public Object getPK() {
		return id;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
