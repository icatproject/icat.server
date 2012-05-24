package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("Many to many relationship between data file as input and a job")
@SuppressWarnings("serial")
@Entity
public class InputDatafile extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(InputDatafile.class);

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

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling InputDatafile for " + includes);
		if (!this.includes.contains(Job.class)) {
			this.job = null;
		}
		if (!this.includes.contains(Datafile.class)) {
			this.datafile = null;
		}
	}

}
