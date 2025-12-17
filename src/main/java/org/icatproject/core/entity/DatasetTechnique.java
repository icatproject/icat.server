package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

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

	@Override
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeLong(gen, "id", id);
		SearchApi.encodeLong(gen, "dataset.id", dataset.id);
		if (technique.getName() == null) {
			technique = entityManager.find(technique.getClass(), technique.id);
		}
		technique.getDoc(entityManager, gen);
	}
}
