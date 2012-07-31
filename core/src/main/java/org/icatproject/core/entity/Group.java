package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("A group of users")
@SuppressWarnings("serial")
@Entity
@Table(name = "GROUP_", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class Group extends EntityBaseBean implements Serializable {

	@Comment("A short name identifying this group of users")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
	private Set<Rule> rules;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
	private Set<UserGroup> userGroups;;

	// Needed for JPA
	public Group() {
	}

	public String getName() {
		return this.name;
	}

	public Set<Rule> getRules() {
		return rules;
	}

	public Set<UserGroup> getUserGroups() {
		return userGroups;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRules(Set<Rule> rules) {
		this.rules = rules;
	}

	public void setUserGroups(Set<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	@Override
	public String toString() {
		return "Group[name=" + name + "]";
	}
}
