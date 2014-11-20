package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Comment("A publication")
@SuppressWarnings("serial")
@Entity
public class Publication extends EntityBaseBean implements Serializable {

	@Comment("A reference in the form to be used for citation")
	@Column(nullable = false, length=511)
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

}
