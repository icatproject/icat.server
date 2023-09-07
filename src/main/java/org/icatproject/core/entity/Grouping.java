package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("A group of users")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class Grouping extends EntityBaseBean implements Serializable {

	@OneToMany(mappedBy = "grouping", cascade = CascadeType.ALL)
	private List<InvestigationGroup> investigationGroups = new ArrayList<>();

	@Comment("A short name identifying this group of users")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(mappedBy = "grouping", cascade = CascadeType.ALL)
	private List<Rule> rules = new ArrayList<Rule>();

	@OneToMany(mappedBy = "grouping", cascade = CascadeType.ALL)
	private List<UserGroup> userGroups = new ArrayList<>();

	// Needed for JPA
	public Grouping() {
	}

	public List<InvestigationGroup> getInvestigationGroups() {
		return investigationGroups;
	}

	public String getName() {
		return this.name;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public List<UserGroup> getUserGroups() {
		return userGroups;
	}

	public void setInvestigationGroups(List<InvestigationGroup> investigationGroups) {
		this.investigationGroups = investigationGroups;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

}
