package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A parameter associated with a sample")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SAMPLE_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class SampleParameter extends Parameter implements Serializable {

	private static Logger logger = Logger.getLogger(SampleParameter.class);

	@Comment("The associated sample")
	@JoinColumn(name = "SAMPLE_ID", nullable = false)
	@ManyToOne
	private Sample sample;

	/* Needed for JPA */
	public SampleParameter() {
	}

	@Override
	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling SampleParameter for " + this.includes);
		if (!this.includes.contains(Sample.class)) {
			this.sample = null;
		}
	}

	public Sample getSample() {
		return this.sample;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Type of parameter is not set");
		}
		if (!type.isApplicableToSample()) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Parameter of type " + type.getName() + " is not applicable to a Sample");
		}
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	@Override
	public String toString() {
		return "SampleParameter[id=" + this.id + "]";
	}
}