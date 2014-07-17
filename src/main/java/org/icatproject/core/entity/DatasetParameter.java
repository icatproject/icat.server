package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.GateKeeper;

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
	public void preparePersist(String modId, EntityManager manager, GateKeeper gateKeeper, boolean rootUser)
			throws IcatException {
		super.preparePersist(modId, manager, gateKeeper, rootUser);
		this.id = null;
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Type of parameter is not set");
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
	public String toString() {
		return "DatasetParameter[id=" + this.id + "]";
	}
}