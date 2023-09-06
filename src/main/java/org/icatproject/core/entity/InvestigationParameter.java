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
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.LuceneApi;

@Comment("A parameter associated with an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class InvestigationParameter extends Parameter implements Serializable {

	@Comment("The associated investigation")
	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	/* Needed for JPA */
	public InvestigationParameter() {
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager, GateKeeper gateKeeper, PersistMode persistMode)
			throws IcatException {
		super.preparePersist(modId, manager, gateKeeper, persistMode);
		this.id = null;
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION, "Type of parameter is not set");
		}
		if (!type.isApplicableToInvestigation()) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Parameter of type " + type.getName() + " is not applicable to an Investigation");
		}
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	@Override
	public void getDoc(JsonGenerator gen) {
		super.getDoc(gen);
		LuceneApi.encodeSortedDocValuesField(gen, "investigation", investigation.id);
	}
}