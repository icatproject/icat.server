package uk.icat3.manager;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.util.ParameterValueType;

public class ManagerUtil {
	/* TODO eliminate class when possible */

	static Logger log = Logger.getLogger(ManagerUtil.class);

	public static <T> T find(Class<T> entityClass, Object primaryKey, EntityManager manager)
			throws NoSuchObjectFoundException {

		if (primaryKey == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] was null.");
		}

		T object = manager.find(entityClass, primaryKey);

		if (object == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}
		return object;
	}
	
	public static Parameter addParameter(String userId, EntityManager manager, String name, String units,
			ParameterValueType type) {
		log.trace("Adding '" + name + "', '" + units + "' to the parameter table");

		if (userId == null) {
			throw new RuntimeException("UserId should not be null for adding parameter");
		}

		ParameterPK pk = new ParameterPK();
		pk.setName(name);
		pk.setUnits(units);
		Parameter parameter = new Parameter();
		parameter.setParameterPK(pk);
		parameter.setValueType(type);
		parameter.setDatasetParameter(true);
		parameter.setDatafileParameter(true);
		parameter.setSampleParameter(true);
		parameter.setDescription("Added by ICAT API as an unverified parameter.");
		parameter.setUnitsLongVersion(units);
		parameter.setVerified(false);
		parameter.setModId(userId);

		try {
			manager.persist(parameter);
			return parameter;
		} catch (Exception ex) {
			log.error("Unable to insert new unverified parameter: " + parameter + " into parameter table.",
					ex);
			return null;
		}
	}

}
