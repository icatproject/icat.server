package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between a dataset and the experimental technique being used to create that Dataset.")
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
