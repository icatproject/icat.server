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

@Comment("A parameter associated with a data file")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAFILE_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class DatafileParameter extends Parameter implements Serializable {

	@Comment("The associated data file")
	@JoinColumn(name = "DATAFILE_ID", nullable = false)
	@ManyToOne
	private Datafile datafile;

	/* Needed for JPA */
	public DatafileParameter() {
	}

	public Datafile getDatafile() {
		return this.datafile;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager, GateKeeper gateKeeper, PersistMode persistMode)
			throws IcatException {
		super.preparePersist(modId, manager, gateKeeper, persistMode);
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION, "Type of parameter is not set");
		}
		if (!type.isApplicableToDatafile()) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Parameter of type " + type.getName() + " is not applicable to a Datafile");
		}
	}

	public void setDatafile(Datafile datafile) {
		this.datafile = datafile;
	}

	@Override
	public void getDoc(JsonGenerator gen) {
		super.getDoc(gen);
		LuceneApi.encodeSortedDocValuesField(gen, "datafile", datafile.id);
	}

}
