package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "publicationGenerator", pkColumnValue = "Publication")
public class Publication extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Publication.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "publicationGenerator")
	private Long id;

	@Column(nullable = false)
	private String fullReference;

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	private String url;

	private String repositoryId;

	private String repository;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)	
	private Investigation investigation;

	/* Needed for JPA */
	public Publication() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullReference() {
		return this.fullReference;
	}

	public void setFullReference(String fullReference) {
		this.fullReference = fullReference;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRepositoryId() {
		return this.repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRepository() {
		return this.repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	@Override
	public String toString() {
		return "Publication[id=" + id + "]";
	}

	@Override
	public Object getPK() {
		return id;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling InvestigationType for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
	}

}
