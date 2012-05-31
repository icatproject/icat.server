package org.icatproject.core.manager;

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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.Comment;
import org.icatproject.core.entity.EntityBaseBean;

/**
 * Holds information about primary keys of an Entity In the case of a simple key it is a list with
 * the accessor method entry, otherwise the first entry is the accessor method of the embedded PK
 * object and subsequent entries are the accessor methods within the PK object. It also holds the
 * set of related entities.
 * */
// Note that this does not use a Singleton Bean as there is no need for the
// extra complexity and
// that the instance is created statically as we know it will be needed.
public class EntityInfoHandler {

	private class PrivateEntityInfo {

		private String classComment;
		private List<List<Field>> constraintFields;
		private Map<Field, String> fieldComments;
		private final Map<Field, Method> getters;
		private Set<Relationship> includesToFollow;
		private final List<Field> notNullableFields;
		private Set<Relationship> ones;
		private final Field pks;
		private final Set<Relationship> relatedEntities;
		private final Map<Field, Method> setters;
		private final Map<Field, Integer> stringFields;

		public PrivateEntityInfo(Field pks, Set<Relationship> rels, List<Field> notNullableFields,
				Map<Field, Method> getters, Map<Field, Integer> stringFields,
				Map<Field, Method> setters, List<List<Field>> constraintFields,
				String classComment, Map<Field, String> fieldComments, Set<Relationship> ones,
				Set<Relationship> includesToFollow) {
			this.pks = pks;
			this.relatedEntities = rels;
			this.notNullableFields = notNullableFields;
			this.getters = getters;
			this.stringFields = stringFields;
			this.setters = setters;
			this.constraintFields = constraintFields;
			this.classComment = classComment;
			this.fieldComments = fieldComments;
			this.ones = ones;
			this.includesToFollow = includesToFollow;
		}

	}

	public class Relationship {

		private final Class<? extends EntityBaseBean> bean;

		private final boolean cascaded;
		private final boolean collection;

		private final Field field;

		public Relationship(Class<? extends EntityBaseBean> bean, Field field, boolean collection,
				boolean cascaded) {
			this.bean = bean;
			this.field = field;
			this.collection = collection;
			this.cascaded = cascaded;
		}

		public Class<? extends EntityBaseBean> getBean() {
			return this.bean;
		}

		public Field getField() {
			return this.field;
		}

		public boolean isCascaded() {
			return cascaded;
		}

		public boolean isCollection() {
			return this.collection;
		}

		@Override
		public String toString() {
			return this.bean.getSimpleName() + " by " + this.field.getName()
					+ (this.collection ? " many" : " one") + (this.cascaded ? " cascaded" : "");
		}

	}

	public static EntityInfoHandler instance = new EntityInfoHandler();

	protected final static Logger logger = Logger.getLogger(EntityInfoHandler.class);

	public static Class<? extends EntityBaseBean> getClass(String tableName) throws IcatException {
		final String name = Constants.ENTITY_PREFIX + tableName;
		try {
			final Class<?> klass = Class.forName(name);
			if (EntityBaseBean.class.isAssignableFrom(klass)) {
				@SuppressWarnings("unchecked")
				final Class<? extends EntityBaseBean> eklass = (Class<? extends EntityBaseBean>) klass;
				return eklass;
			} else {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, name
						+ " is not an EntityBaseBean");
			}
		} catch (final ClassNotFoundException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, name
					+ " is not known to the class loader");
		}
	}

	public static synchronized EntityInfoHandler getInstance() {
		return instance;
	};

	private final HashMap<Class<? extends EntityBaseBean>, PrivateEntityInfo> map = new HashMap<Class<? extends EntityBaseBean>, PrivateEntityInfo>();;

	private EntityInfoHandler() {
	};

	private PrivateEntityInfo buildEi(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		logger.debug("Building PrivateEntityInfo for " + objectClass);
		List<Field> fields = new ArrayList<Field>();
		Class<?> cobj = objectClass;
		while (cobj != null) {
			fields.addAll(getFields(cobj));
			cobj = cobj.getSuperclass();
		}

		Field key = null;
		for (final Field field : fields) {
			if (field.getAnnotation(Id.class) != null) {
				key = field;
			}
		}

		if (key == null) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"Unable to determine key for " + objectClass.getSimpleName());
		}

		final Set<Relationship> rels = new HashSet<Relationship>();
		for (final Field field : fields) {
			if (field.getGenericType() instanceof ParameterizedType) {
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				boolean all;
				if (oneToMany != null) {
					all = Arrays.asList(oneToMany.cascade()).contains(CascadeType.ALL);
					if (!all && oneToMany.cascade().length != 0) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"Cascade must be all or nothing " + objectClass.getSimpleName()
										+ "." + field.getName());
					}
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"Looks like a one to many relationship but not marked as such "
									+ objectClass.getSimpleName() + "." + field.getName());
				}
				final ParameterizedType pt = (ParameterizedType) field.getGenericType();
				final Type[] args = pt.getActualTypeArguments();
				if (args.length == 1) {
					final Type argt = pt.getActualTypeArguments()[0];
					if (argt instanceof Class<?>) {
						final Class<?> argc = (Class<?>) argt;
						if (EntityBaseBean.class.isAssignableFrom(argc)) {
							@SuppressWarnings("unchecked")
							final Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) argc;
							rels.add(new Relationship(argc2, field, true, all));
						}
					}
				}
			} else if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
				ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
				boolean all;
				if (manyToOne != null) {
					all = Arrays.asList(manyToOne.cascade()).contains(CascadeType.ALL);
					if (!all && manyToOne.cascade().length != 0) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"Cascade must be all or nothing " + objectClass.getSimpleName()
										+ "." + field.getName());
					}
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"Looks like a many to one relationship but not marked as such "
									+ objectClass.getSimpleName() + "." + field.getName());
				}
				@SuppressWarnings("unchecked")
				final Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) field
						.getType();
				rels.add(new Relationship(argc2, field, false, all));
			}

		}

		final Set<Relationship> ones = new HashSet<Relationship>();
		for (Relationship rel : rels) {
			if (!rel.collection) {
				ones.add(rel);
			}
		}

		final Set<Relationship> includesToFollow = new HashSet<Relationship>();
		for (Relationship rel : rels) {
			if (!rel.collection || rel.cascaded) {
				includesToFollow.add(rel);
			}
		}

		final List<Field> notNullableFields = new ArrayList<Field>();
		final Map<Field, Method> getters = new HashMap<Field, Method>();
		final Map<Field, Method> setters = new HashMap<Field, Method>();
		final Map<Field, Integer> stringFields = new HashMap<Field, Integer>();
		final Map<String, Field> dbCols = new HashMap<String, Field>();
		final List<List<Field>> constraintFields = new ArrayList<List<Field>>();
		final Map<Field, String> comments = new HashMap<Field, String>();
		for (final Field field : fields) {
			if (Arrays.asList("modTime", "createTime", "createId", "modId").contains(
					field.getName())) {
				continue;
			}

			Class<?> objc = field.getDeclaringClass();

			Boolean nullable = null;
			int length = 255;
			boolean settable = true;

			Comment comment = field.getAnnotation(Comment.class);
			if (comment != null) {
				comments.put(field, comment.value());
			}
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
					if (column.name() != null) {
						dbCols.put(column.name(), field);
					}

				} else if (aType.equals(JoinColumn.class)) {
					final JoinColumn column = (JoinColumn) note;
					if (nullable == null) {
						nullable = column.nullable();
					}
					if (column.name() != null) {
						dbCols.put(column.name(), field);
					}
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
				method = objc.getMethod("get" + prop, types);
			} catch (final NoSuchMethodException e) {
				try {
					method = objc.getMethod("is" + prop, types);
				} catch (final Exception e1) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
				}
			}
			getters.put(field, method);

			if (settable) {
				for (final Method m : objc.getDeclaredMethods()) {
					if (m.getName().equals("set" + prop)) {
						if (setters.put(field, m) != null) {
							throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "set"
									+ prop + " is ambiguous");
						}
					}
				}
				if (setters.get(field) == null) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "set" + prop
							+ " not found for " + objc.getSimpleName());
				}
			}

			if (getters.get(field).getReturnType().equals(String.class)) {
				stringFields.put(field, length);
			}

		}

		Table tableAnnot = objectClass.getAnnotation(Table.class);
		if (tableAnnot != null) {
			for (UniqueConstraint constraint : Arrays.asList(tableAnnot.uniqueConstraints())) {
				List<Field> cf = new ArrayList<Field>();
				constraintFields.add(cf);
				for (String colNam : Arrays.asList(constraint.columnNames())) {

					Field col = dbCols.get(colNam);
					if (col == null) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Column "
								+ colNam + " mentioned in UniqueConstraint of "
								+ objectClass.getSimpleName() + " table is not present in entity");
					}
					cf.add(col);
				}
			}
		}

		if (logger.isDebugEnabled()) {

			StringBuilder sb = new StringBuilder("Not null fields: ");
			boolean first = true;
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

			for (List<Field> cf : constraintFields) {
				sb = new StringBuilder("Constraint: ");

				first = true;
				for (final Field f : cf) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(f.getName());
				}
				logger.debug(sb);
			}

		}

		Comment comment = objectClass.getAnnotation(Comment.class);
		String commentString = comment == null ? null : comment.value();

		return new PrivateEntityInfo(key, rels, notNullableFields, getters, stringFields, setters,
				constraintFields, commentString, comments, ones, includesToFollow);
	}

	public String getClassComment(Class<? extends EntityBaseBean> objectClass) throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.classComment;
		}
	}

	public List<List<Field>> getConstraintFields(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.constraintFields;
		}
	}

	@SuppressWarnings("unchecked")
	public EntityInfo getEntityInfo(String beanName) throws IcatException {
		Class<? extends EntityBaseBean> beanClass;
		try {
			beanClass = (Class<? extends EntityBaseBean>) Class.forName(Constants.ENTITY_PREFIX
					+ beanName);
		} catch (ClassNotFoundException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, beanName
					+ " is not an ICAT entity");
		}
		EntityInfo entityInfo = new EntityInfo();
		entityInfo.setClassComment(getClassComment(beanClass));
		for (List<Field> constraint : getConstraintFields(beanClass)) {
			Constraint c = new Constraint();
			for (Field f : constraint) {
				c.getFieldNames().add(f.getName());
			}
			entityInfo.getConstraints().add(c);
		}

		Map<Field, Method> getters = getGetters(beanClass);
		Map<Field, String> fieldComments = getFieldComments(beanClass);
		List<Field> nnf = getNotNullableFields(beanClass);
		Set<Relationship> re = getRelatedEntities(beanClass);
		Map<Field, Integer> sf = getStringFields(beanClass);
		Map<Field, EntityField> eiMap = new HashMap<Field, EntityField>();
		for (Field field : getters.keySet()) {
			EntityField ef = new EntityField();
			ef.setName(field.getName());
			ef.setType(field.getType().getSimpleName());
			ef.setNotNullable(nnf.contains(field));
			ef.setStringLength(sf.get(field));
			ef.setComment(fieldComments.get(field));
			ef.setRelType(EntityField.RelType.ATTRIBUTE);
			entityInfo.getFields().add(ef);
			eiMap.put(field, ef);
		}
		for (Relationship rel : re) {
			EntityField ef = eiMap.get(rel.getField());
			ef.setType(rel.getBean().getSimpleName());
			ef.setCascaded(rel.isCascaded());
			if (rel.isCollection()) {
				ef.setRelType(EntityField.RelType.MANY);
			} else {
				ef.setRelType(EntityField.RelType.ONE);
			}
		}
		return entityInfo;

	}

	public Map<Field, String> getFieldComments(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.fieldComments;
		}
	}

	private List<Field> getFields(Class<?> cobj) {
		List<Field> fields = new ArrayList<Field>(Arrays.asList(cobj.getDeclaredFields()));
		final Iterator<Field> iter = fields.iterator();
		while (iter.hasNext()) {
			final Field f = iter.next();
			int modifier = f.getModifiers();
			if (f.getName().startsWith("_")) {
				iter.remove();
				logger.debug("Ignore injected field " + f);
			} else if (Modifier.isStatic(modifier)) {
				iter.remove();
				logger.debug("Ignore static field " + f);
			} else if (Modifier.isTransient(modifier)) {
				iter.remove();
				logger.debug("Ignore transient field " + f);
			} else if (f.isAnnotationPresent(XmlTransient.class)) {
				iter.remove();
				logger.debug("Ignore XmlTransient field " + f);
			}
		}
		return fields;
	}

	public Map<Field, Method> getGetters(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.getters;
		}
	}

	public Set<Relationship> getIncludesToFollow(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.includesToFollow;
		}
	}

	public Field getKeyFor(Class<? extends EntityBaseBean> objectClass) throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.pks;
		}
	}

	public List<Field> getNotNullableFields(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.notNullableFields;
		}
	}

	public Set<Relationship> getOnes(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.ones;
		}
	}

	public Set<Relationship> getRelatedEntities(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.relatedEntities;
		}
	}

	public Map<Field, Method> getSetters(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
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
			throws IcatException {
		PrivateEntityInfo ei = null;
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
