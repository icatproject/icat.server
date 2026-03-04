package org.icatproject.core.entity;

import java.io.Serializable;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between an investigation and a sample that has been used in that investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "SAMPLE_ID" }) })
public class InvestigationSample extends EntityBaseBean implements Serializable {

	@JoinColumn(nullable = false, name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@JoinColumn(nullable = false, name = "SAMPLE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Sample sample;

	public Investigation getInvestigation() {
		return this.investigation;
	}

	public Sample getSample() {
		return this.sample;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	@Override
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		if (sample.getName() == null) {
			sample = entityManager.find(sample.getClass(), sample.id);
		}
		sample.getDoc(entityManager, gen);
		SearchApi.encodeLong(gen, "investigation.id", investigation.id);
		SearchApi.encodeLong(gen, "id", id);
	}
}
