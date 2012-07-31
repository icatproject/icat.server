package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Comment("Many to many relationship between data file as input and a job")
@SuppressWarnings("serial")
@Entity
public class InputDatafile extends EntityBaseBean implements Serializable {

	public InputDatafile() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Job job;

	public Job getJob() {
		return this.job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Datafile datafile;

	public Datafile getDatafile() {
		return this.datafile;
	}

	public void setDatafile(Datafile datafile) {
		this.datafile = datafile;
	}

}
