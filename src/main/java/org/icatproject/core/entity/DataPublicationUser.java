package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Author, e.g. creator of or contributor to a data publication")
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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<Affiliation> affiliations = new ArrayList<>();

	@Comment("Role of that user in the publication, see DataCite property contributorType for suggested values or use \"Creator\"")
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

	@Comment("The email address for the user that should be exposed in the publication, if any")
	private String email;

	/* Needed for JPA */
	public DataPublicationUser() {
	}

	public DataPublication getPublication() {
		return publication;
	}

	public User getUser() {
		return user;
	}

	public List<Affiliation> getAffiliations() {
		return affiliations;
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

	public String getEmail() {
		return email;
	}

	public void setPublication(DataPublication publication) {
		this.publication = publication;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setAffiliations(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
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

	public void setEmail(String email) {
		this.email = email;
	}
}
