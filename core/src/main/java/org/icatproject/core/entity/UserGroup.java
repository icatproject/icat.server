package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("Many to many relationship between user and group")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_NAME", "GROUP_NAME" }) })
public class UserGroup extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(UserGroup.class);

	@JoinColumn(name = "GROUP_NAME", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Group group;

	@JoinColumn(name = "USER_NAME", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	// Needed for JPA
	public UserGroup() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling UserGroup for " + this.includes);
		if (!this.includes.contains(User.class)) {
			this.user = null;
		}
		if (!this.includes.contains(Group.class)) {
			this.group = null;
		}
	}

	public User getUser() {
		return this.user;
	}

	public Group getGroup() {
		return this.group;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
