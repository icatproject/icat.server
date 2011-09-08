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
 * Holds information about primary keys of an Entity In the case of a simple key it is a list with
 * the accessor method entry, otherwise the first entry is the accessor method of the embedded PK
 * object and subsequent entries are the accessor methods within the PK object. It also holds the
 * set of related entities.
 * */
// Note that this does not use a Singleton Bean as there is no need for the extra complexity and
// that the instance is created statically as we know it will be needed.
public class EntityInfoHandler {

	protected final static Logger logger = Logger.getLogger(EntityInfoHandler.class);

	public static Class<? extends EntityBaseBean> getClass(String tableName) throws BadParameterException {
		// TODO avoid constant
		String name = "uk.icat3.entity." + tableName;
		try {
			Class<?> klass = Class.forName(name);
			if (EntityBaseBean.class.isAssignableFrom(klass)) {
				@SuppressWarnings("unchecked")
				Class<? extends EntityBaseBean> eklass = (Class<? extends EntityBaseBean>) klass;
				return eklass;
			} else {
				throw new BadParameterException(name + " is not an EntityBaseBean");
			}
		} catch (ClassNotFoundException e) {
			throw new BadParameterException(name + " is not known to the class loader");
		}
	}

	public class Relationship {

		private Class<? extends EntityBaseBean> bean;
		private Field field;
		private boolean collection;

		public Relationship(Class<? extends EntityBaseBean> bean, Field field, boolean collection) {
			this.bean = bean;
			this.field = field;
			this.collection = collection;
		}

		public String toString() {
			return this.bean.getSimpleName() + " by " + this.field.getName() + (collection ? " many" : " one");
		}

		public Class<? extends EntityBaseBean> getBean() {
			return bean;
		}

		public Field getField() {
			return field;
		}

	}

	public static EntityInfoHandler instance = new EntityInfoHandler();

	private HashMap<Class<? extends EntityBaseBean>, EntityInfo> map = new HashMap<Class<? extends EntityBaseBean>, EntityInfo>();

	private KeyType keyType;

	public enum KeyType {SIMPLE, GENERATED, COMPOUND};

	private EntityInfoHandler() {
	};

	public static synchronized EntityInfoHandler getInstance() {
		return instance;
	}

	private class EntityInfo {

	
		public EntityInfo(List<String> pks, Set<Relationship> rels, List<Field> notNullableFields,
				Map<Field, Method> getters, Map<Field, Integer> stringFields, Map<Field, Method> setters, KeyType keyType) {
			this.pks = pks;
			this.relatedEntities = rels;
			this.notNullableFields = notNullableFields;
			this.getters = getters;
			this.stringFields = stringFields;
			this.setters = setters;
			this.keyType = keyType;
		}

		private List<String> pks;
		private Set<Relationship> relatedEntities;
		private List<Field> notNullableFields;
		private Map<Field, Method> getters;
		private Map<Field, Integer> stringFields;
		private Map<Field, Method> setters;
		private KeyType keyType;

	};

	public List<String> getKeysFor(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.pks;
		}
	}

	public Set<Relationship> getRelatedEntities(Class<? extends EntityBaseBean> objectClass)
			throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.relatedEntities;
		}
	}

	private EntityInfo buildEi(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		logger.debug("Building EntityInfo for " + objectClass);

		List<Field> fields = new ArrayList<Field>(Arrays.asList(objectClass.getDeclaredFields()));
		Iterator<Field> iter = fields.iterator();
		while (iter.hasNext()) {
			Field f = iter.next();
			if (f.getName().startsWith("_")) {
				iter.remove();
				logger.debug("Ignore injected field " + f);
			}
		}

		keyType = null;
		List<String> keys = new ArrayList<String>();
		int c = 0;
		for (Field field : fields) {
			if (field.getAnnotation(Id.class) != null) {
				String name = field.getName();
				name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
				keys.add(name);
				c++;
				if (field.getAnnotation(GeneratedValue.class) != null) {
					keyType = KeyType.GENERATED;
				} else {
					keyType = KeyType.SIMPLE;
				}
			} 
		}
		for (Field field : fields) {
			if (field.getAnnotation(EmbeddedId.class) != null) {
				String name = field.getName();
				name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
				keys.add(name);
				for (Field ifield : field.getType().getDeclaredFields()) {
					if ((ifield.getModifiers() & Modifier.STATIC) == 0) {
						name = ifield.getName();
						name = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
						keys.add(name);
					}
				}
				c++;
				keyType = KeyType.COMPOUND;
			}
		}
		if (c != 1) {
			throw new IcatInternalException("Unable to determine key for " + objectClass.getName());
		}

		Set<Relationship> rels = new HashSet<Relationship>();
		for (Field field : fields) {
			if (field.getGenericType() instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) field.getGenericType();
				Type[] args = pt.getActualTypeArguments();
				if (args.length == 1) {
					Type argt = pt.getActualTypeArguments()[0];
					if (argt instanceof Class<?>) {
						Class<?> argc = (Class<?>) argt;
						if (EntityBaseBean.class.isAssignableFrom(argc)) {
							@SuppressWarnings("unchecked")
							Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) argc;
							rels.add(new Relationship(argc2, field, true));
						}
					}
				}
			} else if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
				@SuppressWarnings("unchecked")
				Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) field.getType();
				rels.add(new Relationship(argc2, field, false));
			}

		}

		List<Field> notNullableFields = new ArrayList<Field>();
		Map<Field, Method> getters = new HashMap<Field, Method>();
		Map<Field, Method> setters = new HashMap<Field, Method>();
		Map<Field, Integer> stringFields = new HashMap<Field, Integer>();
		for (Field field : fields) {

			if (field.getDeclaredAnnotations().length != 0) {

				Boolean nullable = null;
				int length = 255;
				boolean settable = true;

				for (Annotation note : field.getDeclaredAnnotations()) {
					Class<? extends Annotation> aType = note.annotationType();

					if (aType.equals(GeneratedValue.class)) {
						nullable = true;
					} else if (aType.equals(Column.class)) {
						Column column = (Column) note;
						if (nullable == null) {
							nullable = column.nullable();
						}
						length = column.length();

					} else if (aType.equals(JoinColumn.class)) {
						JoinColumn column = (JoinColumn) note;
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

				String name = field.getName();
				String prop = Character.toUpperCase(name.charAt(0)) + name.substring(1);
				Class<?>[] types = new Class[] {};
				Method method;
				try {
					method = objectClass.getMethod("get" + prop, types);
				} catch (NoSuchMethodException e) {
					try {
						method = objectClass.getMethod("is" + prop, types);
					} catch (Exception e1) {
						throw new IcatInternalException("" + e);
					}
				}
				getters.put(field, method);

				if (settable) {
					for (Method m : objectClass.getDeclaredMethods()) {
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
		}

		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("Key: ");
			boolean first = true;
			for (String key : keys) {
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
			for (Field f : notNullableFields) {
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
			for (Entry<Field, Integer> f : stringFields.entrySet()) {
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
			for (Entry<Field, Method> f : getters.entrySet()) {
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
			for (Entry<Field, Method> f : setters.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(f.getKey().getName() + " -> " + f.getValue().getName());
			}
			logger.debug(sb);

		}

		return new EntityInfo(keys, rels, notNullableFields, getters, stringFields, setters, keyType);
	}

	public List<Field> getNotNullableFields(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.notNullableFields;
		}
	}

	public Map<Field, Method> getGetters(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.getters;
		}
	}

	public Map<Field, Integer> getStringFields(Class<? extends EntityBaseBean> objectClass)
			throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.stringFields;
		}
	}

	public Map<Field, Method> getSetters(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.setters;
		}
	}

	public KeyType getKeytype(Class<? extends EntityBaseBean> objectClass) throws IcatInternalException {
		EntityInfo ei = null;
		synchronized (map) {
			ei = map.get(objectClass);
			if (ei == null) {
				ei = buildEi(objectClass);
				map.put(objectClass, ei);
			}
			return ei.keyType;
		}
	}
}
