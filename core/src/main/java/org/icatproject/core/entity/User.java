package org.icatproject.core.entity;

import java.io.Serializable;
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<UserGroup> userGroups;

	public List<UserGroup> getUserGroups() {
		return userGroups;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<InvestigationUser> investigationUsers;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<InstrumentScientist> instrumentScientists;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<Study> studies;

	public List<Study> getStudies() {
		return studies;
	}

	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}

	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	public User() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User[name=" + name + "]";
	}

	public List<InvestigationUser> getInvestigationUsers() {
		return investigationUsers;
	}

	public void setInvestigationUsers(List<InvestigationUser> investigationUsers) {
		this.investigationUsers = investigationUsers;
	}

	public List<InstrumentScientist> getInstrumentScientists() {
		return instrumentScientists;
	}

	public void setInstrumentScientists(List<InstrumentScientist> instrumentScientists) {
		this.instrumentScientists = instrumentScientists;
	}

}
