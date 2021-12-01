package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("The home institute or other affiliation of a user")
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
