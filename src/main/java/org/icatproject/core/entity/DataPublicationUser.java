package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Author, e.g. creator of a or contributor to a data publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "USER_ID", "CONTRIBUTORTYPE" }) })
public class DataPublicationUser extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublication publication;

	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Comment("See DataCite property contributorType or use \"Creator\"")
	@Column(name = "CONTRIBUTORTYPE", nullable = false)
	private String contributorType;

	@Comment("Defines an order among the contributors")
	private String orderKey;

	@Comment("May include title")
	private String fullName;

	@Comment("The given name of the user")
	private String givenName;

	@Comment("The family name of the user")
	private String familyName;

	@Comment("The home institute or other affiliation of the user")
	@Column(length = 1023)
	private String affiliation;

	/* Needed for JPA */
	public DataPublicationUser() {
	}

	public DataPublication getPublication() {
		return publication;
	}

	public User getUser() {
		return user;
	}

	public String getContributorType() {
		return contributorType;
	}

	public String getOrderKey() {
		return orderKey;
	}

	public String getFullName() {
		return fullName;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setPublication(DataPublication publication) {
		this.publication = publication;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setContributorType(String contributorType) {
		this.contributorType = contributorType;
	}

	public void setOrderKey(String orderKey) {
		this.orderKey = orderKey;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
}
