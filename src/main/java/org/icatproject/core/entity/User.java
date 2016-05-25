package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("A user of the facility")
@SuppressWarnings("serial")
@Entity
@Table(name = "USER_", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class User extends EntityBaseBean implements Serializable {

	@Comment("The name of the user to match that provided by the authentication mechanism")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("May include title")
	private String fullName;

	@Comment("An email address for the user")
	private String email;

	@Comment("An ORCID iD for the user")
	private String orcidId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<UserGroup> userGroups = new ArrayList<UserGroup>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<InvestigationUser> investigationUsers = new ArrayList<InvestigationUser>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<InstrumentScientist> instrumentScientists = new ArrayList<InstrumentScientist>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<Study> studies = new ArrayList<Study>();

	public User() {
	}

	public String getEmail() {
		return email;
	}

	public String getFullName() {
		return fullName;
	}

	public List<InstrumentScientist> getInstrumentScientists() {
		return instrumentScientists;
	}

	public List<InvestigationUser> getInvestigationUsers() {
		return investigationUsers;
	}

	public String getName() {
		return this.name;
	}

	public String getOrcidId() {
		return orcidId;
	}

	public List<Study> getStudies() {
		return studies;
	}

	public List<UserGroup> getUserGroups() {
		return userGroups;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setInstrumentScientists(List<InstrumentScientist> instrumentScientists) {
		this.instrumentScientists = instrumentScientists;
	}

	public void setInvestigationUsers(List<InvestigationUser> investigationUsers) {
		this.investigationUsers = investigationUsers;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrcidId(String orcidId) {
		this.orcidId = orcidId;
	}

	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}

	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	@Override
	public String toString() {
		return "User[name=" + name + "]";
	}

}
