package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A parameter associated with a data file")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAFILE_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class DatafileParameter extends Parameter implements Serializable {

	private static Logger logger = Logger.getLogger(DatafileParameter.class);

	@Comment("The associated data file")
	@JoinColumn(name = "DATAFILE_ID", nullable = false)
	@ManyToOne
	private Datafile datafile;

	/* Needed for JPA */
	public DatafileParameter() {
	}

	@Override
	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatafileParameter for " + this.includes);
		if (!this.includes.contains(Datafile.class)) {
			this.datafile = null;
		}
	}

	public Datafile getDatafile() {
		return this.datafile;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Type of parameter is not set");
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
	public String toString() {
		return "DatafileParameter[id=" + this.id + "]";
	}

	// @Override
	// public void isValid(EntityManager manager, boolean deepValidation) throws
	// ValidationException,
	// IcatInternalException {
	// super.isValid(manager, deepValidation);
	// if (datafileParameterPK == null)
	// throw new ValidationException(this + " primary key cannot be null");
	//
	// // check private key
	// datafileParameterPK.isValid();
	//
	// // check valid
	// String paramName = this.getDatafileParameterPK().getName();
	// String paramUnits = this.getDatafileParameterPK().getUnits();
	//
	// // check if this name is parameter table
	// ParameterPK paramPK = new ParameterPK();
	// paramPK.setName(paramName);
	// paramPK.setUnits(paramUnits);
	//
	// ParameterType parameterDB = manager.find(ParameterType.class, paramPK);
	//
	// // check paramPK is in the parameter table
	// if (parameterDB == null) {
	// logger.info(datafileParameterPK
	// +
	// " is not in the parameter table as a data file parameter so been marked as unverified and inserting new row in ParameterType table");
	// // add new parameter into database
	// parameterDB = ManagerUtil.addParameter(this.modId, manager, paramName,
	// paramUnits, getValueType());
	// if (parameterDB == null)
	// throw new ValidationException("ParameterType: " + paramName +
	// " with units: "
	// + paramUnits
	// + " cannot be inserted into the ParameterType table.");
	// }
	//
	// // check that it is a dataset parameter
	// if (!parameterDB.isDatafileParameter())
	// throw new ValidationException("DatafileParameter: " + paramName +
	// " with units: " + paramUnits
	// + " is not a data file parameter.");
	//
	// // check is numeric
	// if (parameterDB.isNumeric()) {
	// if (this.getStringValue() != null)
	// throw new ValidationException("DatafileParameter: " + paramName +
	// " with units: " + paramUnits
	// + " must be a numeric value only.");
	// }
	//
	// // check if string
	// if (!parameterDB.isNumeric()) {
	// if (this.getNumericValue() != null)
	// throw new ValidationException("DatafileParameter: " + paramName +
	// " with units: " + paramUnits
	// + " must be a string value only.");
	// }
	//
	// // check if datafile parameter is already in DB
	// DatafileParameter paramDB = manager.find(DatafileParameter.class,
	// datafileParameterPK);
	// if (paramDB != null &&
	// !paramDB.getDatafileParameterPK().equals(datafileParameterPK))
	// throw new ValidationException("DatafileParameter: " + paramName +
	// " with units: " + paramUnits
	// + " is already is a parameter of the datafile.");User
	//
	// // check that the parameter datafile id is the same as actual datafile
	// // id
	// if (!datafileParameterPK.getDatafileId().equals(getDatafile().getId())) {
	// throw new ValidationException("DatafileParameter: " + paramName +
	// " with units: " + paramUnits
	// + " has datafile id: " + datafileParameterPK.getDatafileId()
	// + " that does not corresponds to its parent datafile id: " +
	// getDatafile().getId());
	// }
	// }

}
