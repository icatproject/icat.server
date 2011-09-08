/*
 * EntityBaseBean.java
 *
 * Created on 08 February 2007, 09:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.EntityInfoHandler;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityBaseBean implements Serializable {

	/**
	 * global static logger
	 */
	private static Logger logger = Logger.getLogger(EntityBaseBean.class);
	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	@Column(name = "MOD_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement
	protected Date modTime;

	@Column(name = "CREATE_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement
	protected Date createTime;

	@Column(name = "CREATE_ID", nullable = false)
	@XmlElement
	protected String createId;

	@Column(name = "MOD_ID", nullable = false)
	protected String modId;

	/**
	 * Gets the modTime of this entity.
	 * 
	 * @return the modTime
	 */
	public Date getModTime() {
		return this.modTime;
	}

	/**
	 * Gets the createTime of this entity.
	 * 
	 * @return the modTime
	 */
	public Date getCreateTime() {
		return this.createTime;
	}

	/**
	 * Gets the createId of this entity.
	 * 
	 * @return the createId
	 */
	public String getCreateId() {
		return createId;
	}

	/**
	 * Gets the modId of this entity.
	 * 
	 * @return the modId
	 */
	public String getModId() {
		return this.modId;
	}

	/**
	 * Sets the modId of this entity to the specified value.
	 * 
	 * @param modId
	 *            the new modId
	 */
	public void setModId(String modId) {
		this.modId = modId;
	}

	/**
	 * Automatically updates modTime when entity is persisted or merged
	 * 
	 * @throws uk.icat3.exceptions.EntityNotModifiableError
	 */
	@PreUpdate
	public void preUpdate() {
		modTime = new Date();
	}

	/**
	 * Automatically updates deleted, modTime, createTime and modId when entity is created
	 * 
	 * @throws IcatInternalException
	 */
	@PrePersist
	public void prePersist() {
		if (modId == null) {
			throw new RuntimeException("modId not set on " + this);
		}
		createId = modId;
		createTime = modTime = new Date();
	}

	private void isValid() throws ValidationException, IcatInternalException {

		Class<? extends EntityBaseBean> klass = this.getClass();
		logger.trace("Checking validity of " + klass.getSimpleName());
		List<Field> notNullFields = eiHandler.getNotNullableFields(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);

		for (Field field : notNullFields) {

			Object value;
			try {
				Method method = getters.get(field);
				value = method.invoke(this, (Object[]) new Class[] {});
			} catch (Exception e) {
				throw new IcatInternalException("" + e);
			}

			if (value == null) {
				throw new ValidationException(this.getClass().getSimpleName() + ": " + field.getName()
						+ " cannot be null.");
			}
		}

		Map<Field, Integer> stringFields = eiHandler.getStringFields(klass);
		for (Entry<Field, Integer> entry : stringFields.entrySet()) {
			Field field = entry.getKey();
			Integer length = entry.getValue();
			Method method = getters.get(field);
			Object value;
			try {
				value = method.invoke(this, (Object[]) new Class[] {});
			} catch (Exception e) {
				throw new IcatInternalException("" + e);
			}
			if (value != null) {
				System.out.println(field);
				if (((String) value).length() > length) {
					throw new ValidationException(getClass().getSimpleName() + ": " + field.getName()
							+ " cannot have length > " + length);
				}
			}
		}

	}

	/*
	 * If this method is overridden it should normally be called as well by super.isValid()
	 */
	public void isValid(EntityManager manager) throws ValidationException, IcatInternalException {
		isValid(manager, true);
	}

	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		isValid();
	}

	/*
	 * If this method is overridden it should normally be called as well by super.isValid()
	 */
	public void merge(Object from, EntityManager manager) throws ValidationException, IcatInternalException, NoSuchObjectFoundException {

		Class<? extends EntityBaseBean> klass = this.getClass();
		Map<Field, Method> setters = eiHandler.getSetters(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);

		for (Entry<Field, Method> entry : setters.entrySet()) {
			Field field = entry.getKey();
			try {
				Method m = getters.get(field);
				Object value = m.invoke(from, new Object[0]);
				entry.getValue().invoke(this, new Object[] { value });
				logger.trace("Updated " + klass.getSimpleName() + "." + field.getName() + " to " + value);
			} catch (Exception e) {
				throw new IcatInternalException("" + e);
			}
		}
		this.postMergeFixup(manager);
	}

	/*
	 * If this method is overridden it should normally be called as well by super.isValid()
	 */
	public void postMergeFixup(EntityManager manager) throws NoSuchObjectFoundException {
		// Do nothing by default
	}

	/*
	 * If this method is overridden it should normally be called as well by super.isUnique()
	 */
	public void isUnique(EntityManager manager) throws ValidationException, ObjectAlreadyExistsException,
			IcatInternalException {

		if (eiHandler.getKeytype(this.getClass()) != EntityInfoHandler.KeyType.GENERATED) {
			Object primaryKey = this.getPK();
			Class<? extends EntityBaseBean> entityClass = this.getClass();
			if (primaryKey == null) {
				throw new ValidationException(entityClass.getSimpleName() + "[id:" + primaryKey + "] was null.");
			}
			if (manager.find(entityClass, primaryKey) != null) {
				throw new ObjectAlreadyExistsException(entityClass.getSimpleName() + "[id:" + primaryKey
						+ "] already present.");
			}
		}

	}

	public void preparePersistTop(String modId, EntityManager manager) throws NoSuchObjectFoundException {
		preparePersist(modId, manager);
	}

	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException {
		this.modId = modId;
	}

	public abstract Object getPK();
}