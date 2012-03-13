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

@Comment("A publication")
@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "publicationGenerator", pkColumnValue = "Publication")
public class Publication extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Publication.class);

	@Comment("A reference in the form to be used for citation")
	@Column(nullable = false)
	private String fullReference;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "publicationGenerator")
	private Long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Comment("The name of a repository where the publication is held")
	private String repository;

	@Comment("The id of the publication within the repository")
	private String repositoryId;

	@Comment("A URL from which the publication may be downloaded")
	private String url;

	/* Needed for JPA */
	public Publication() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling InvestigationType for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
	}

	public String getFullReference() {
		return this.fullReference;
	}

	public Long getId() {
		return this.id;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	@Override
	public Object getPK() {
		return id;
	}

	public String getRepository() {
		return this.repository;
	}

	public String getRepositoryId() {
		return this.repositoryId;
	}

	public String getUrl() {
		return this.url;
	}

	public void setFullReference(String fullReference) {
		this.fullReference = fullReference;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Publication[id=" + id + "]";
	}

}
