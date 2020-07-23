package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Represents an experimental technique")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class Technique extends EntityBaseBean implements Serializable {

	@Comment("A short name for the technique")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("A persistent identifier attributed to the technique, ideally referring to a vocabulary term")
	private String pid;

	@Comment("An informal description")
	private String description;

	public String getName() {
		return name;
	}

	public String getPid() {
		return pid;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
