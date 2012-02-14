package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(name = "\"USER\"")
public class User extends EntityBaseBean implements Serializable {

	@Id
	private String name;

	private String title;

	private String initials;

	private String firstName;

	private String middleName;

	private String lastName;

	@OneToMany(mappedBy = "user")
	private Set<UserGroup> userGroups;

	public Set<UserGroup> getUserGroups() {
		return userGroups;
	}

	@XmlTransient
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private Collection<Investigator> investigatorCollection;

	public User() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String federalId) {
		this.name = federalId;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInitials() {
		return this.initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@XmlTransient
	public Collection<Investigator> getInvestigatorCollection() {
		return this.investigatorCollection;
	}

	public void setInvestigatorCollection(Collection<Investigator> investigatorCollection) {
		this.investigatorCollection = investigatorCollection;
	}

	@Override
	public Object getPK() {
		return name;
	}

}
