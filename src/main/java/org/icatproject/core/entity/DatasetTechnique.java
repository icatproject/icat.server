package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between a dataset and the experimental technique being used to create that Dataset")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATASET_ID", "TECHNIQUE_ID" }) })
public class DatasetTechnique extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATASET_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Dataset dataset;

	@JoinColumn(name = "TECHNIQUE_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Technique technique;

	public Dataset getDataset() {
		return dataset;
	}

	public Technique getTechnique() {
		return technique;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setTechnique(Technique technique) {
		this.technique = technique;
	}
}
