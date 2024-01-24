package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("The home institute or other affiliation of a user in the context of a data publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATIONUSER_ID", "NAME" }) })
public class Affiliation extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATIONUSER_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublicationUser user;

	@Comment("An internal name for that affiliation entry, possibly the organization name")
	@Column(name = "NAME", nullable = false, length = 255)
	private String name;

	@Comment("The full reference of the affiliation, optionally including street address and department, as it should appear in the publication")
	@Column(length = 1023)
	private String fullReference;

	@Comment("Identifier such as ROR or ISNI")
	private String pid;

	// Needed for JPA
	public Affiliation() {
	}

	public DataPublicationUser getUser() {
		return user;
	}

	public String getName() {
		return name;
	}

	public String getFullReference() {
		return fullReference;
	}

	public String getPid() {
		return pid;
	}

	public void setUser(DataPublicationUser user) {
		this.user = user;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFullReference(String fullReference) {
		this.fullReference = fullReference;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}
}
