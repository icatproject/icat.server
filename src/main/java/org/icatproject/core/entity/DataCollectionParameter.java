package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityBeanManager.PersistMode;

@Comment("A parameter associated with a DataCollection")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATACOLLECTION_ID", "PARAMETER_TYPE_ID" }) })
public class DataCollectionParameter extends Parameter implements Serializable {

	@Comment("The associated DataCollection")
	@JoinColumn(name = "DATACOLLECTION_ID", nullable = false)
	@ManyToOne
	private DataCollection dataCollection;

	/* Needed for JPA */
	public DataCollectionParameter() {
	}

	public DataCollection getDataCollection() {
		return dataCollection;
	}

	@Override
	public void preparePersist(String modId, EntityManager entityManager, PersistMode persistMode) throws IcatException {
		super.preparePersist(modId, entityManager, persistMode);
		if (!type.isApplicableToDataCollection()) { // type has been checked as
													// not null by super
													// call
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Parameter of type " + type.getName() + " is not applicable to a DataCollection");
		}
	}

	public void setDataCollection(DataCollection dataCollection) {
		this.dataCollection = dataCollection;
	}

}