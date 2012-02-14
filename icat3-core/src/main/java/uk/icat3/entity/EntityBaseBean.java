package uk.icat3.entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.BeanManager;
import uk.icat3.security.EntityInfoHandler;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityBaseBean implements Serializable {

	private static final Logger logger = Logger.getLogger(EntityBaseBean.class);

	private static final EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

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

	protected transient Set<Class<? extends EntityBaseBean>> includes = new HashSet<Class<? extends EntityBaseBean>>();

	public Set<Class<? extends EntityBaseBean>> getIncludes() {
		return includes;
	}

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
	 * Automatically updates deleted, modTime, createTime and modId when entity
	 * is created
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
				logger.trace("Getter: " + method);
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
				if (((String) value).length() > length) {
					throw new ValidationException(getClass().getSimpleName() + ": " + field.getName()
							+ " cannot have length > " + length);
				}
			}
		}

	}

	/*
	 * If this method is overridden it should normally be called as well by
	 * super.isValid()
	 */
	public void isValid(EntityManager manager) throws ValidationException, IcatInternalException {
		isValid(manager, true);
	}

	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		isValid();
	}

	/*
	 * If this method is overridden it should normally be called as well by
	 * super.merge()
	 */
	public void merge(Object from, EntityManager manager) throws ValidationException, IcatInternalException,
			NoSuchObjectFoundException, BadParameterException {
		BeanManager.merge(this, from, manager);
		this.postMergeFixup(manager);
	}

	/*
	 * If this method is overridden it should normally be called as well by
	 * super.postMergeFixup()
	 */
	public void postMergeFixup(EntityManager manager) throws NoSuchObjectFoundException, BadParameterException,
			IcatInternalException {
		// Do nothing by default
	}

	/*
	 * If this method is overridden it should normally be called as well by
	 * super.isUnique()
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

	/*
	 * If this method is overridden it should normally be called as well by
	 * super.preparePersist()
	 */
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		this.modId = modId;
	}

	public abstract Object getPK();

	public void addIncludes(Set<Class<? extends EntityBaseBean>> requestedIncludes) throws IcatInternalException {
		BeanManager.addIncludes(this, requestedIncludes);

	}

	/*
	 * If this method is overridden it should normally be called as well by
	 * super.canDelete()
	 */
	public void canDelete(EntityManager manager) throws ValidationException {
	}
}
