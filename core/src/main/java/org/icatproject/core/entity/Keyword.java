package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("Must be related to an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "INVESTIGATION_ID" }) })
public class Keyword extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Keyword.class);

	@Comment("The investigation to which this keyword applies")
	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	@Comment("The name of the keyword")
	@Column(name = "NAME", nullable = false)
	private String name;

	/* Needed for JPA */
	public Keyword() {
	}

	@Override
	public String toString() {
		return "Keyword[id=" + id + "]";
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Keyword for " + includes);

		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
