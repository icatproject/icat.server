package uk.icat3.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
@Table(name = "\"GROUP\"")
public class Group extends EntityBaseBean implements Serializable {

	public void setName(String name) {
		this.name = name;
	}

	public void setUserGroups(Set<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	private final static Logger logger = Logger.getLogger(Group.class);

	// Needed for JPA
	public Group() {
	};

	public String getName() {
		return this.name;
	}

	@Id
	private String name;

	@OneToMany(mappedBy = "group")
	private Set<UserGroup> userGroups;

	@OneToMany(mappedBy = "group")
	private Set<Rule> rules;

	public Set<Rule> getRules() {
		return rules;
	}

	public void setRules(Set<Rule> rules) {
		this.rules = rules;
	}

	public Set<UserGroup> getUserGroups() {
		return userGroups;
	}

	@Override
	public Object getPK() {
		return this.name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Group for " + includes);
		if (!this.includes.contains(UserGroup.class)) {
			this.userGroups = null;
		}
		if (!this.includes.contains(Rule.class)) {
			this.rules = null;
		}
	}
}
