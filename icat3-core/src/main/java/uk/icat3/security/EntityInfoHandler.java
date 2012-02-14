package uk.icat3.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;

/**
 * Holds information about primary keys of an Entity In the case of a simple key
 * it is a list with the accessor method entry, otherwise the first entry is the
 * accessor method of the embedded PK object and subsequent entries are the
 * accessor methods within the PK object. It also holds the set of related
 * entities.
 * */
// Note that this does not use a Singleton Bean as there is no need for the
// extra complexity and
// that the instance is created statically as we know it will be needed.
public class EntityInfoHandler {

	private class EntityInfo {

		private final List<String> pks;

		private final Set<Relationship> relatedEntities;
		private final List<Field> notNullableFields;
		private final Map<Field, Method> getters;
		private final Map<Field, Integer> stringFields;
		private final Map<Field, Method> setters;
		private final KeyType keyType;

		public EntityInfo(List<String> pks, Set<Relationship> rels, List<Field> notNullableFields,
				Map<Field, Method> getters, Map<Field, Integer> stringFields, Map<Field, Method> setters,
				KeyType keyType) {
			this.pks = pks;
			this.relatedEntities = rels;
			this.notNullableFields = notNullableFields;
			this.getters = getters;
			this.stringFields = stringFields;
			this.setters = setters;
			this.keyType = keyType;
		}

	}

	public enum KeyType {
		SIMPLE, GENERATED, COMPOUND
	}

	public class Relationship {

		private final Class<? extends EntityBaseBean> bean;
		private final Field field;

		private final boolean collection;

		public Relationship(Class<? extends EntityBaseBean> bean, Field field, boolean collection) {
			this.bean = bean;
			this.field = field;
			this.collection = collection;
		}

		public Class<? extends EntityBaseBean> getBean() {
			return this.bean;
		}

		public Field getField() {
			return this.field;
		}

		public boolean isCollection() {
			return this.collection;
		}

		@Override
		public String toString() {
			return this.bean.getSimpleName() + " by " + this.field.getName() + (this.collection ? " many" : " one");
		}

	}

	protected final static Logger logger = Logger.getLogger(EntityInfoHandler.class);

	public static EntityInfoHandler instance = new EntityInfoHandler();

	public static Class<? extends EntityBaseBean> getClass(String tableName) throws BadParameterException {
		// TODO avoid constant
		final String name = "uk.icat3.entity." + tableName;
		try {
			final Class<?> klass = Class.forName(name);
			if (EntityBaseBean.class.isAssignableFrom(klass)) {
				@SuppressWarnings("unchecked")
				final Class<? extends EntityBaseBean> eklass = (Class<? extends EntityBaseBean>) klass;
				return eklass;
			} else {
				throw new BadParameterException(name + " is not an EntityBaseBean");
			}
		} catch (final ClassNotFoundException e) {
			throw new BadParameterException(name + " is not known to the class loader");
		}
	}

	public static synchronized EntityInfoHandler getInstance() {
		return instance;
	};

	private final HashMap<Class<? extends EntityBaseBean>, EntityInfo> map = new HashMap<Class<? extends EntityBaseBean>, EntityInfo>();;

	private KeyType keyType;

	private EntityInfoHandler() {
	};

	private EntityInfo buildEi(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		logger.debug("Building EntityInfo for " + objectClass);

		final List<Field> fields = new ArrayList<Field>(Arrays.asList(objectClass.getDeclaredFields()));
		final Iterator<Field> iter = fields.iterator();
		while (iter.hasNext()) {
			final Field f = iter.next();
			if (f.getName().startsWith("_")) {
				iter.remove();
				logger.debug("Ignore injected field " + f);
			}
			int modifier =  f.getModifiers();
			if (Modifier.isStatic(modifier)) {
				iter.remove();
				logger.debug("Ignore static field " + f);
			}
			if (Modifier.isTransient(modifier)) {
				iter.remove();
				logger.debug("Ignore transient field " + f);
			}
		}

		this.keyType = null;
		final List<String> keys = new ArrayList<String>();
		int c = 0;
		for (final Field field : fields) {
			if (field.getAnnotation(Id.class) != null) {
				String name = field.getName();
				name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
				keys.add(name);
				c++;
				if (field.getAnnotation(GeneratedValue.class) != null) {
					this.keyType = KeyType.GENERATED;
				} else {
					this.keyType = KeyType.SIMPLE;
				}
			}
		}
		for (final Field field : fields) {
			if (field.getAnnotation(EmbeddedId.class) != null) {
				String name = field.getName();
				name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
				keys.add(name);
				for (final Field ifield : field.getType().getDeclaredFields()) {
					if ((ifield.getModifiers() & Modifier.STATIC) == 0) {
						name = ifield.getName();
						name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
						keys.add(name);
					}
				}
				c++;
				this.keyType = KeyType.COMPOUND;
			}
		}
		if (c != 1) {
			throw new IcatInternalException("Unable to determine key for " + objectClass.getName());
		}

		final Set<Relationship> rels = new HashSet<Relationship>();
		for (final Field field : fields) {
			if (field.getGenericType() instanceof ParameterizedType) {
				final ParameterizedType pt = (ParameterizedType) field.getGenericType();
				final Type[] args = pt.getActualTypeArguments();
				if (args.length == 1) {
					final Type argt = pt.getActualTypeArguments()[0];
					if (argt instanceof Class<?>) {
						final Class<?> argc = (Class<?>) argt;
						if (EntityBaseBean.class.isAssignableFrom(argc)) {
							@SuppressWarnings("unchecked")
							final Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) argc;
							rels.add(new Relationship(argc2, field, true));
						}
					}
				}
			} else if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
				@SuppressWarnings("unchecked")
				final Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) field.getType();
				rels.add(new Relationship(argc2, field, false));
			}

		}

		final List<Field> notNullableFields = new ArrayList<Field>();
		final Map<Field, Method> getters = new HashMap<Field, Method>();
		final Map<Field, Method> setters = new HashMap<Field, Method>();
		final Map<Field, Integer> stringFields = new HashMap<Field, Integer>();
		for (final Field field : fields) {

			Boolean nullable = null;
			int length = 255;
			boolean settable = true;

			for (final Annotation note : field.getDeclaredAnnotations()) {
				final Class<? extends Annotation> aType = note.annotationType();

				if (aType.equals(GeneratedValue.class)) {
					nullable = true;
				} else if (aType.equals(Column.class)) {
					final Column column = (Column) note;
					if (nullable == null) {
						nullable = column.nullable();
					}
					length = column.length();

				} else if (aType.equals(JoinColumn.class)) {
					final JoinColumn column = (JoinColumn) note;
					if (nullable == null) {
						nullable = column.nullable();
					}
				} else if (aType.equals(EmbeddedId.class)) {
					nullable = true;
					settable = false;
				} else if (aType.equals(XmlTransient.class)) {
					settable = false;
				} else if (aType.equals(Id.class)) {
					settable = false;
				}
				if (field.getGenericType() instanceof ParameterizedType) {
					settable = false;
				}
			}

			if (nullable != null && !nullable) {
				notNullableFields.add(field);
			}

			final String name = field.getName();
			final String prop = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			final Class<?>[] types = new Class[] {};
			Method method;
			try {
				method = objectClass.getMethod("get" + prop, types);
			} catch (final NoSuchMethodException e) {
				try {
					method = objectClass.getMethod("is" + prop, types);
				} catch (final Exception e1) {
					throw new IcatInternalException("" + e);
				}
			}
			getters.put(field, method);

			if (settable) {
				for (final Method m : objectClass.getDeclaredMethods()) {
					if (m.getName().equals("set" + prop)) {
						if (setters.put(field, m) != null) {
							throw new IcatInternalException("set" + prop + " is ambiguous");
						}
					}
				}
				if (setters.get(field) == null) {
					throw new IcatInternalException("set" + prop + " not found for " + objectClass.getSimpleName());
				}
			}

			if (getters.get(field).getReturnType().equals(String.class)) {
				stringFields.put(field, length);
			}

		}

		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Key: ");
			boolean first = true;
			for (final String key : keys) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(key);
			}
			logger.debug(sb);

			sb = new StringBuilder("Not null fields: ");
			first = true;
			for (final Field f : notNullableFields) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(f.getName());
			}
			logger.debug(sb);

			sb = new StringBuilder("String fields: ");
			first = true;
			for (final Entry<Field, Integer> f : stringFields.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(f.getKey().getName() + " " + f.getValue());
			}
			logger.debug(sb);

			sb = new StringBuilder("Getters: ");
			first = true;
			for (final Entry<Field, Method> f : getters.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(f.getKey().getName() + " -> " + f.getValue().getName());
			}
			logger.debug(sb);

			sb = new StringBuilder("Setters: ");
			first = true;
			for (final Entry<Field, Method> f : setters.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(f.getKey().getName() + " -> " + f.getValue().getName());
			}
			logger.debug(sb);

		}

		return new EntityInfo(keys, rels, notNullableFields, getters, stringFields, setters, this.keyType);
	}

	public Map<Field, Method> getGetters(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.getters;
		}
	}

	public List<String> getKeysFor(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.pks;
		}
	}

	public KeyType getKeytype(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.keyType;
		}
	}

	public List<Field> getNotNullableFields(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.notNullableFields;
		}
	}

	public Set<Relationship> getRelatedEntities(Class<? extends EntityBaseBean> objectClass)
			throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.relatedEntities;
		}
	}

	public Map<Field, Method> getSetters(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.setters;
		}
	}

	public Map<Field, Integer> getStringFields(Class<? extends EntityBaseBean> objectClass)
			throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.stringFields;
		}
	}
}
