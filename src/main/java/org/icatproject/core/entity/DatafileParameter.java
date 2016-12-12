package org.icatproject.core.entity;

import java.io.Serializable;
import java.security.InvalidParameterException;

import javax.json.stream.JsonGenerator;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityBeanManager.PersistMode;
import org.icatproject.core.manager.GateKeeper;

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
		// TODO doc.add(new SortedDocValuesField("datafile", new
		// BytesRef(Long.toString(datafile.id))));
		throw new InvalidParameterException("Datafile parameter.java need attention");
	}

}
