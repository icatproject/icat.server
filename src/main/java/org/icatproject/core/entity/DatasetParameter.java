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

@Comment("A parameter associated with a data set")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATASET_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class DatasetParameter extends Parameter implements Serializable {

	@Comment("The associated data set")
	@JoinColumn(name = "DATASET_ID", nullable = false)
	@ManyToOne
	private Dataset dataset;

	/* Needed for JPA */
	public DatasetParameter() {
	}

	public Dataset getDataset() {
		return this.dataset;
	}

	@Override
	public void preparePersist(String modId, EntityManager entityManager, PersistMode persistMode) throws IcatException {
		super.preparePersist(modId, entityManager, persistMode);
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION, "Type of parameter is not set");
		}
		if (!type.isApplicableToDataset()) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Parameter of type " + type.getName() + " is not applicable to a Dataset");
		}
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		super.getDoc(entityManager, gen);
		SearchApi.encodeLong(gen, "dataset.id", dataset.id);
	}
}