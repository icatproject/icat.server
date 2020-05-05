package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Information about financial support for a data publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "FUNDERNAME", "AWARDNUMBER" }) })
public class FundingReference extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublication publication;

	@Column(name = "FUNDERNAME", nullable = false)
	private String funderName;

	private String funderIdentifier;

	@Column(name = "AWARDNUMBER", nullable = false)
	private String awardNumber;

	private String awardTitle;

	/* Needed for JPA */
	public FundingReference() {
	}

	public DataPublication getPublication() {
		return publication;
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

	public void setPublication(DataPublication publication) {
		this.publication = publication;
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
}
