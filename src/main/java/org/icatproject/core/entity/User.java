package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

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

	@Comment("The given name of the user")
	private String givenName;

	@Comment("The family name of the user")
	private String familyName;

	@Comment("The home institute or other affiliation of the user")
	private String affiliation;

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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<DataPublicationUser> dataPublicationUsers = new ArrayList<DataPublicationUser>();

	public static Set<String> docFields = new HashSet<>(Arrays.asList("user.name", "user.fullName", "user.id"));

	public User() {
	}

	public String getEmail() {
		return email;
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

	public List<DataPublicationUser> getDataPublicationUsers() {
		return dataPublicationUsers;
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

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public void setDataPublicationUsers(List<DataPublicationUser> dataPublicationUsers) {
		this.dataPublicationUsers = dataPublicationUsers;
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

	@Override
	public void getDoc(EntityManager manager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeNullableString(gen, "user.fullName", fullName);
		SearchApi.encodeString(gen, "user.name", name);
		SearchApi.encodeLong(gen, "user.id", id);
	}

}
