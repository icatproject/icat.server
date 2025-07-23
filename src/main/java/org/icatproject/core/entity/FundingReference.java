package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Information about financial support")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FUNDERNAME", "AWARDNUMBER" }) })
public class FundingReference extends EntityBaseBean implements Serializable {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "funding")
	private List<InvestigationFunding> investigations = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "funding")
	private List<DataPublicationFunding> publications = new ArrayList<>();

	@Comment("Name of the funding entity")
	@Column(name = "FUNDERNAME", nullable = false)
	private String funderName;

	@Comment("Unique identifier of the funding entity, such as a Crossref Funder ID")
	private String funderIdentifier;

	@Comment("Code assigned by the funder to identify the grant, suggest to use \":unas\" from the Datacite schema if there is no such identifier")
	@Column(name = "AWARDNUMBER", nullable = false)
	private String awardNumber;

	@Comment("Title or name of the grant")
	private String awardTitle;

	@Comment("Human readable acknowledgement for the funding, maybe using a specific formulation requested by the funder")
	@Column(length = 1023)
	private String acknowledgement;

	/* Needed for JPA */
	public FundingReference() {
	}

	public List<InvestigationFunding> getInvestigations() {
		return investigations;
	}

	public List<DataPublicationFunding> getPublications() {
		return publications;
	}

	public String getFunderName() {
		return funderName;
	}

	public String getFunderIdentifier() {
		return funderIdentifier;
	}

	public String getAwardNumber() {
		return awardNumber;
	}

	public String getAwardTitle() {
		return awardTitle;
	}

	public String getAcknowledgement() {
		return acknowledgement;
	}

	public void setInvestigations(List<InvestigationFunding> investigations) {
		this.investigations = investigations;
	}

	public void setPublications(List<DataPublicationFunding> publications) {
		this.publications = publications;
	}

	public void setFunderName(String funderName) {
		this.funderName = funderName;
	}

	public void setFunderIdentifier(String funderIdentifier) {
		this.funderIdentifier = funderIdentifier;
	}

	public void setAwardNumber(String awardNumber) {
		this.awardNumber = awardNumber;
	}

	public void setAwardTitle(String awardTitle) {
		this.awardTitle = awardTitle;
	}

	public void setAcknowledgement(String acknowledgement) {
		this.acknowledgement = acknowledgement;
	}
}
