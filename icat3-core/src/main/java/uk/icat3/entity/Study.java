package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "studyGenerator", pkColumnValue = "Study")
public class Study extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Study.class);
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getRelatedMaterial() {
		return relatedMaterial;
	}

	public void setRelatedMaterial(String relatedMaterial) {
		this.relatedMaterial = relatedMaterial;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public BigInteger getManager() {
		return manager;
	}

	public void setManager(BigInteger manager) {
		this.manager = manager;
	}

	public StudyStatus getStatus() {
		return status;
	}

	public void setStatus(StudyStatus status) {
		this.status = status;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "studyGenerator")
	private Long id;

	@Column(nullable = false)
	private String name;


	private String purpose;

	
	private String relatedMaterial;


	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	
	private BigInteger manager;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "study")
	private List<StudyInvestigation> studyInvestigations;


	public List<StudyInvestigation> getStudyInvestigations() {
		return studyInvestigations;
	}

	public void setStudyInvestigations(List<StudyInvestigation> studyInvestigations) {
		this.studyInvestigations = studyInvestigations;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	private StudyStatus status;

	/* Needed for JPA */
	public Study() {
	}

	@Override
	public String toString() {
		return "Study[id=" + id + "]";
	}

	@Override
	public Object getPK() {
		return id;
	}
	
	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Study for " + includes);

		if (!this.includes.contains(StudyInvestigation.class)) {
			this.studyInvestigations = null;
		}
		if (!this.includes.contains(StudyStatus.class)) {
			this.status = null;
		}
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

}
