package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityBeanManager.PersistMode;
import org.icatproject.core.manager.search.SearchApi;

@Comment("A parameter associated with a sample")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SAMPLE_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class SampleParameter extends Parameter implements Serializable {

	@Comment("The associated sample")
	@JoinColumn(name = "SAMPLE_ID", nullable = false)
	@ManyToOne
	private Sample sample;

	/* Needed for JPA */
	public SampleParameter() {
	}

	public Sample getSample() {
		return this.sample;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager, PersistMode persistMode) throws IcatException {
		super.preparePersist(modId, manager, persistMode);
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION, "Type of parameter is not set");
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
	public void getDoc(EntityManager manager, JsonGenerator gen) throws IcatException {
		super.getDoc(manager, gen);
		SearchApi.encodeLong(gen, "sample.id", sample.id);
	}

}