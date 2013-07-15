package org.icatproject.core.entity;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.manager.BeanManager;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.LuceneSingleton;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class EntityBaseBean implements Serializable {

	private static final EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	private static final Logger logger = Logger.getLogger(EntityBaseBean.class);

	@Column(name = "CREATE_ID", nullable = false)
	@XmlElement
	protected String createId;

	@Column(name = "CREATE_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement
	protected Date createTime;

	@Id
	@GeneratedValue
	protected Long id;

	protected transient Set<Class<? extends EntityBaseBean>> includes;

	@Column(name = "MOD_ID", nullable = false)
	protected String modId;

	@Column(name = "MOD_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement
	protected Date modTime;

	public void addIncludes(Set<Class<? extends EntityBaseBean>> includes, boolean followCascades)
			throws IcatException {
		BeanManager.addIncludes(this, includes, followCascades);
	}

	public void addOne() throws IcatException {
		BeanManager.addOne(this);
	}

	/*
	 * If this method is overridden it should be called as well by super.addToClone()
	 */
	void addToClone(EntityBaseBean clone) {
		clone.createId = createId;
		clone.createTime = createTime;
		clone.id = id;
		clone.modId = modId;
		clone.modTime = modTime;
	}

	/*
	 * If this method is overridden it should be called as well by super.canDelete()
	 */
	public void canDelete(EntityManager manager) throws IcatException {
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
	 * Gets the createTime of this entity.
	 * 
	 * @return the modTime
	 */
	public Date getCreateTime() {
		return this.createTime;
	}

	public Long getId() {
		return id;
	}

	public Set<Class<? extends EntityBaseBean>> getIncludes() {
		if (includes == null) {
			includes = new HashSet<Class<? extends EntityBaseBean>>();
		}
		return includes;
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
	 * Gets the modTime of this entity.
	 * 
	 * @return the modTime
	 */
	public Date getModTime() {
		return this.modTime;
	}

	final public void isUnique(EntityManager manager) throws IcatException {

		Class<? extends EntityBaseBean> entityClass = this.getClass();

		Map<Field, Method> getters = eiHandler.getGetters(entityClass);
		for (List<Field> constraint : eiHandler.getConstraintFields(entityClass)) {
			StringBuilder queryString = new StringBuilder();
			for (Field f : constraint) {
				if (queryString.length() == 0) {
					queryString.append("SELECT COUNT(o) FROM " + entityClass.getSimpleName()
							+ " o WHERE (");
				} else {
					queryString.append(") AND (");
				}
				String name = f.getName();
				queryString.append("o." + name + " = :" + name + " OR o." + name + " IS NULL");
			}
			Query query = manager.createQuery(queryString.toString() + ")");
			for (Field f : constraint) {
				Object value;
				try {
					value = getters.get(f).invoke(this);
				} catch (IllegalArgumentException e) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"IllegalArgumentException " + e.getMessage());
				} catch (IllegalAccessException e) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"IllegalAccessException " + e.getMessage());
				} catch (InvocationTargetException e) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"InvocationTargetException " + e.getMessage());
				}
				query = query.setParameter(f.getName(), value);
			}
			logger.debug("Checking uniqueness with " + query);
			long count = (Long) query.getSingleResult();
			if (count != 0) {
				StringBuilder erm = new StringBuilder();
				for (Field f : constraint) {
					Object value;
					try {
						value = getters.get(f).invoke(this);
					} catch (IllegalArgumentException e) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"IllegalArgumentException " + e.getMessage());
					} catch (IllegalAccessException e) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"IllegalAccessException " + e.getMessage());
					} catch (InvocationTargetException e) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"InvocationTargetException " + e.getMessage());
					}
					if (erm.length() == 0) {
						erm.append(entityClass.getSimpleName() + " exists with ");
					} else {
						erm.append(", ");
					}
					erm.append(f.getName() + " = '" + value + "'");
				}
				throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS,
						erm.toString());
			}
		}
	}

	private void isValid() throws IcatException {

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
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}

			if (value == null) {
				throw new IcatException(IcatException.IcatExceptionType.VALIDATION, this.getClass()
						.getSimpleName() + ": " + field.getName() + " cannot be null.");
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
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}
			if (value != null) {
				if (((String) value).length() > length) {
					throw new IcatException(IcatException.IcatExceptionType.VALIDATION, getClass()
							.getSimpleName()
							+ ": "
							+ field.getName()
							+ " cannot have length > "
							+ length);
				}
			}
		}

	}

	/*
	 * If this method is overridden it should normally be called as well by super.isValid()
	 */
	public void isValid(EntityManager manager) throws IcatException {
		isValid(manager, true);
	}

	public void isValid(EntityManager manager, boolean deepValidation) throws IcatException {
		isValid();
	}

	final public void merge(Object from, EntityManager manager) throws IcatException {
		BeanManager.merge(this, from, manager);
		this.postMergeFixup(manager);
	}

	/*
	 * If this method is overridden it should normally be called as well by super.postMergeFixup()
	 */
	public void postMergeFixup(EntityManager manager) throws IcatException {
		// Do nothing by default
	}

	/*
	 * If this method is overridden it should be called as well by super.preparePersist()
	 */
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		this.id = null;
		this.modId = modId;
		Class<? extends EntityBaseBean> klass = this.getClass();
		Set<Relationship> rs = eiHandler.getRelatedEntities(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		for (Relationship r : rs) {
			if (r.isCascaded()) {
				Method m = getters.get(r.getField());
				try {
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> collection = (List<EntityBaseBean>) m.invoke(this);
					if (!collection.isEmpty()) {
						Method rev = r.getInverseSetter();
						for (EntityBaseBean bean : collection) {
							bean.preparePersist(modId, manager);
							rev.invoke(bean, this);
						}
					}
				} catch (Exception e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " "
							+ e.getMessage());
				}
			}
		}
	}

	/**
	 * Automatically updates deleted, modTime, createTime and modId when entity is created
	 */
	@PrePersist
	public void prePersist() {
		if (modId == null) {
			throw new RuntimeException("modId not set on " + this);
		}
		createId = modId;
		createTime = modTime = new Date();
	}

	/**
	 * Automatically updates modTime when entity is persisted or merged
	 */
	@PreUpdate
	public void preUpdate() {
		modTime = new Date();
	}

	public EntityBaseBean pruned() throws IcatException {
		Class<? extends EntityBaseBean> klass = this.getClass();
		try {
			Constructor<? extends EntityBaseBean> con = klass.getConstructor();
			EntityBaseBean clone = con.newInstance();
			clone.id = this.id;
			clone.createTime = this.createTime;
			clone.createId = this.createId;
			clone.modTime = this.modTime;
			clone.modId = this.modId;
			Set<Relationship> rs = eiHandler.getRelatedEntities(klass);
			Set<Field> atts = eiHandler.getAttributes(klass);
			Map<Field, Method> getters = eiHandler.getGetters(klass);
			Map<Field, Method> setters = eiHandler.getSettersForUpdate(klass);
			for (Field att : atts) {
				Object value = getters.get(att).invoke(this);
				if (value != null) {
					setters.get(att).invoke(clone, new Object[] { value });
				}
			}
			for (Relationship r : rs) {
				if (this.includes != null && this.includes.contains(r.getBean())) {
					if (r.isCollection()) {
						Field att = r.getField();
						@SuppressWarnings("unchecked")
						List<EntityBaseBean> values = (List<EntityBaseBean>) getters.get(att)
								.invoke(this);
						@SuppressWarnings("unchecked")
						List<EntityBaseBean> cloneList = (List<EntityBaseBean>) getters.get(att)
								.invoke(clone);
						for (EntityBaseBean value : values) {
							value = value.pruned();
							cloneList.add(value);
						}
					} else {
						Field att = r.getField();
						EntityBaseBean value = (EntityBaseBean) getters.get(att).invoke(this);
						if (value != null) {
							value = value.pruned();
							setters.get(att).invoke(clone, new Object[] { value });
						}
					}
				}
			}
			return clone;
		} catch (Exception e) {
			reportUnexpected(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
		}

	}

	public void setId(Long id) {
		this.id = id;
	}

	private void reportUnexpected(Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(baos);
		e.printStackTrace(s);
		s.close();
		logger.error("Internal exception: " + baos);
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

	public void addToLucene(LuceneSingleton lucene) throws IcatException {
		lucene.addDocument(this);
		Class<? extends EntityBaseBean> klass = this.getClass();
		Set<Relationship> rs = eiHandler.getRelatedEntities(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		for (Relationship r : rs) {
			if (r.isCascaded()) {
				Method m = getters.get(r.getField());
				try {
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> collection = (List<EntityBaseBean>) m.invoke(this);
					if (!collection.isEmpty()) {
						for (EntityBaseBean bean : collection) {
							bean.addToLucene(lucene);
						}
					}
				} catch (Exception e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}

		}

	}

	public void removeFromLucene(LuceneSingleton lucene) throws IcatException {
		lucene.deleteDocument(this);
		Class<? extends EntityBaseBean> klass = this.getClass();
		Set<Relationship> rs = eiHandler.getRelatedEntities(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		for (Relationship r : rs) {
			if (r.isCascaded()) {
				Method m = getters.get(r.getField());
				try {
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> collection = (List<EntityBaseBean>) m.invoke(this);
					if (!collection.isEmpty()) {
						for (EntityBaseBean bean : collection) {
							bean.removeFromLucene(lucene);
						}
					}
				} catch (Exception e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}

		}

	}

	public void updateInLucene(LuceneSingleton lucene) throws IcatException {
		lucene.updateDocument(this);
		Class<? extends EntityBaseBean> klass = this.getClass();
		Set<Relationship> rs = eiHandler.getRelatedEntities(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		for (Relationship r : rs) {
			if (r.isCascaded()) {
				Method m = getters.get(r.getField());
				try {
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> collection = (List<EntityBaseBean>) m.invoke(this);
					if (!collection.isEmpty()) {
						for (EntityBaseBean bean : collection) {
							bean.updateInLucene(lucene);
						}
					}
				} catch (Exception e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}

		}

	}

}
