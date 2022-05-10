package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Used to represent an arbitrary relationship between data files")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SOURCE_DATAFILE_ID",
		"DEST_DATAFILE_ID" }) })
public class RelatedDatafile extends EntityBaseBean implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_DATAFILE_ID", nullable = false)
	private Datafile sourceDatafile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEST_DATAFILE_ID", nullable = false)
	private Datafile destDatafile;

	public Datafile getSourceDatafile() {
		return sourceDatafile;
	}

	public void setSourceDatafile(Datafile sourceDatafile) {
		this.sourceDatafile = sourceDatafile;
	}

	public Datafile getDestDatafile() {
		return destDatafile;
	}

	public void setDestDatafile(Datafile destDatafile) {
		this.destDatafile = destDatafile;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	@Comment("Identifies the type of relationship between the two datafiles - e.g. \"COPY\"")
	@Column(name = "RELATION", nullable = false)
	private String relation;

	/* Needed for JPA */
	public RelatedDatafile() {
	}

}
