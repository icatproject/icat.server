package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("A publication")
@SuppressWarnings("serial")
@Entity
public class Publication extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Publication.class);

	@Comment("A reference in the form to be used for citation")
	@Column(nullable = false)
	private String fullReference;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;
	
	@Comment("The Digital Object Identifier associated with this publication")
	private String doi;

	@Comment("The name of a repository where the publication is held")
	private String repository;

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

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

	public Investigation getInvestigation() {
		return investigation;
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
