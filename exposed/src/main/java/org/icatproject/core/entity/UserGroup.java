package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Many to many relationship between user and group")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID", "GROUP_ID" }) })
public class UserGroup extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "GROUP_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Grouping grouping;

	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	// Needed for JPA
	public UserGroup() {
	}

	public Grouping getGrouping() {
		return this.grouping;
	}

	public User getUser() {
		return this.user;
	}

	public void setGrouping(Grouping grouping) {
		this.grouping = grouping;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
