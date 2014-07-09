package org.icatproject.core.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.icatproject.core.entity.Application;
import org.icatproject.core.entity.Comment;
import org.icatproject.core.entity.DataCollection;
import org.icatproject.core.entity.DataCollectionDatafile;
import org.icatproject.core.entity.DataCollectionDataset;
import org.icatproject.core.entity.DataCollectionParameter;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.FacilityCycle;
import org.icatproject.core.entity.Grouping;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.InstrumentScientist;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationInstrument;
import org.icatproject.core.entity.InvestigationParameter;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Job;
import org.icatproject.core.entity.Keyword;
import org.icatproject.core.entity.Log;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.PermissibleStringValue;
import org.icatproject.core.entity.PublicStep;
import org.icatproject.core.entity.Publication;
import org.icatproject.core.entity.RelatedDatafile;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleParameter;
import org.icatproject.core.entity.SampleType;
import org.icatproject.core.entity.Shift;
import org.icatproject.core.entity.Study;
import org.icatproject.core.entity.StudyInvestigation;
import org.icatproject.core.entity.User;
import org.icatproject.core.entity.UserGroup;

// Note that this does not use a Singleton Bean as there is no need for the
// extra complexity and
// that the instance is created statically as we know it will be needed.
public class EntityInfoHandler {

	private class PrivateEntityInfo {

		private Set<Field> attributes;
		private String classComment;
		private List<Field> constraintFields;
		private Constructor<? extends EntityBaseBean> constructor;
		public String exportHeader;
		public String exportHeaderAll;
		public String exportNull;
		private Map<Field, String> fieldComments;
		private Map<String, Field> fieldsByName;
		private final Map<Field, Method> getters;
		private final List<Field> notNullableFields;
		private Set<Relationship> ones;
		private final Set<Relationship> relatedEntities;
		private Map<Field, Method> setters;
		private final Map<Field, Integer> stringFields;
		private final Map<Field, Method> updaters;
		private List<Field> fields;

		public PrivateEntityInfo(Set<Relationship> rels, List<Field> notNullableFields,
				Map<Field, Method> getters, Map<Field, Integer> stringFields,
				Map<Field, Method> setters, Map<Field, Method> updaters,
				List<Field> constraintFields, String classComment,
				Map<Field, String> fieldComments, Set<Relationship> ones, Set<Field> attributes,
				Constructor<? extends EntityBaseBean> constructor, Map<String, Field> fieldByName,
				String exportHeader, String exportNull, List<Field> fields, String exportHeaderAll) {
			this.relatedEntities = rels;
			this.notNullableFields = notNullableFields;
			this.getters = getters;
			this.stringFields = stringFields;
			this.setters = setters;
			this.updaters = updaters;
			this.constraintFields = constraintFields;
			this.classComment = classComment;
			this.fieldComments = fieldComments;
			this.ones = ones;
			this.attributes = attributes;
			this.constructor = constructor;
			this.fieldsByName = fieldByName;
			this.exportHeader = exportHeader;
			this.exportNull = exportNull;
			this.fields = fields;
			this.exportHeaderAll = exportHeaderAll;
		}

	}

	public class Relationship {

		private final boolean collection;

		private final Class<? extends EntityBaseBean> destinationBean;

		private final Field field;

		private Method inverseSetter;

		private Class<? extends EntityBaseBean> originBean;

		public Relationship(Class<? extends EntityBaseBean> originBean, Field field,
				Class<? extends EntityBaseBean> destinationBean, boolean collection,
				boolean cascaded, Method inverseSetter) {
			if (collection != cascaded) {
				throw new RuntimeException(
						"Collection and Cascaded must match for this code to work "
								+ destinationBean + " " + field);
			}
			this.originBean = originBean;
			this.destinationBean = destinationBean;
			this.field = field;
			this.collection = collection;
			this.inverseSetter = inverseSetter;
		}

		public Class<? extends EntityBaseBean> getDestinationBean() {
			return destinationBean;
		}

		public Field getField() {
			return field;
		}

		public Method getInverseSetter() {
			return inverseSetter;
		}

		public Class<? extends EntityBaseBean> getOriginBean() {
			return originBean;
		}

		public boolean isCollection() {
			return collection;
		}

		@Override
		public String toString() {
			return "From " + originBean.getSimpleName() + " to " + destinationBean.getSimpleName()
					+ " by " + this.field.getName() + (collection ? " many" : " one")
					+ (inverseSetter == null ? "" : " " + this.inverseSetter.getName());
		}

	}

	private static List<String> alphabeticEntityNames;
	private static List<Class<? extends EntityBaseBean>> entities = Arrays.asList(User.class,
			Grouping.class, UserGroup.class, Rule.class, PublicStep.class, Facility.class,
			DatafileFormat.class, Application.class, Instrument.class, InvestigationType.class,
			DatasetType.class, ParameterType.class, Investigation.class, Dataset.class,
			Datafile.class, SampleType.class, FacilityCycle.class, DataCollection.class,
			DataCollectionDatafile.class, DataCollectionDataset.class,
			DataCollectionParameter.class, DatafileParameter.class, DatasetParameter.class,
			InvestigationParameter.class, Job.class, Keyword.class, Log.class,
			PermissibleStringValue.class, Publication.class, RelatedDatafile.class, Sample.class,
			SampleParameter.class, Shift.class, Study.class, InvestigationUser.class,
			StudyInvestigation.class, InvestigationInstrument.class, InstrumentScientist.class);
	private static Set<String> entityNames = new HashSet<>();

	private static String[] systemAttributes = { "createId", "createTime", "modId", "modTime" };

	private static List<String> exportEntityNames = new ArrayList<>();

	public static EntityInfoHandler instance = new EntityInfoHandler();

	protected final static Logger logger = Logger.getLogger(EntityInfoHandler.class);

	static {
		for (Class<? extends EntityBaseBean> entity : entities) {
			entityNames.add(entity.getSimpleName());
			exportEntityNames.add(entity.getSimpleName());
		}
		alphabeticEntityNames = new ArrayList<>(entityNames);
		Collections.sort(alphabeticEntityNames);
	}

	public static Set<String> getAlphabeticEntityNames() {
		return entityNames;
	}

	public static Class<EntityBaseBean> getClass(String tableName) throws IcatException {
		try {
			final Class<?> klass = Class.forName(Constants.ENTITY_PREFIX + tableName);
			if (EntityBaseBean.class.isAssignableFrom(klass)) {
				@SuppressWarnings("unchecked")
				final Class<EntityBaseBean> eklass = (Class<EntityBaseBean>) klass;
				return eklass;
			} else {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, tableName
						+ " is not an EntityBaseBean");
			}
		} catch (final ClassNotFoundException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, tableName
					+ " is not an EntityBaseBean");
		}
	}

	public static List<String> getEntityNamesList() {
		return alphabeticEntityNames;
	}

	public static List<String> getExportEntityNames() {
		return exportEntityNames;
	};

	public static synchronized EntityInfoHandler getInstance() {
		return instance;
	};

	private final HashMap<Class<? extends EntityBaseBean>, PrivateEntityInfo> map = new HashMap<Class<? extends EntityBaseBean>, PrivateEntityInfo>();

	private final static Comparator<? super Field> fieldComparator = new Comparator<Field>() {
		@Override
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	private EntityInfoHandler() {
	}

	private PrivateEntityInfo buildEi(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		logger.debug("Building PrivateEntityInfo for " + objectClass);
		List<Field> fields = new ArrayList<Field>();
		Class<?> cobj = objectClass;
		while (cobj != null) {
			fields.addAll(getNormalFields(cobj));
			cobj = cobj.getSuperclass();
		}

		Collections.sort(fields, fieldComparator);
		Map<String, Field> fieldsByName = new HashMap<>();

		Set<Field> attributes = new HashSet<Field>();

		Set<Relationship> rels = new HashSet<Relationship>();

		for (final Field field : fields) {
			fieldsByName.put(field.getName(), field);

			if (field.getGenericType() instanceof ParameterizedType) {
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				boolean all;
				String mappedBy;
				if (oneToMany != null) {
					all = Arrays.asList(oneToMany.cascade()).contains(CascadeType.ALL);
					if (!all && oneToMany.cascade().length != 0) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"Cascade must be all or nothing " + objectClass.getSimpleName()
										+ "." + field.getName());
					}
					mappedBy = oneToMany.mappedBy();
					if (mappedBy == null) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"MappedBy must be set for " + objectClass.getSimpleName() + "."
										+ field.getName());
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
							try {
								String name = "set" + Character.toUpperCase(mappedBy.charAt(0))
										+ mappedBy.substring(1);
								Method[] ms = argc2.getMethods();
								boolean inv = false;
								for (Method m : ms) {
									if (m.getName().equals(name)) {
										rels.add(new Relationship(objectClass, field, argc2, true,
												all, m));
										inv = true;
										break;
									}
								}
								if (!inv) {
									throw new IcatException(
											IcatException.IcatExceptionType.INTERNAL,
											"Inverse relationship not found "
													+ objectClass.getSimpleName() + "."
													+ field.getName());
								}
							} catch (SecurityException e) {
								throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
										e.getMessage());
							}
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
				rels.add(new Relationship(objectClass, field, argc2, false, all, null));
			} else if (!Arrays.asList("modTime", "createTime", "createId", "modId", "id").contains(
					field.getName())) {
				attributes.add(field);
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
			includesToFollow.add(rel);
		}

		final List<Field> notNullableFields = new ArrayList<Field>();
		final Map<Field, Method> getters = new HashMap<Field, Method>();
		final Map<Field, Method> setters = new HashMap<Field, Method>();
		final Map<Field, Method> updaters = new HashMap<Field, Method>();
		final Map<Field, Integer> stringFields = new HashMap<Field, Integer>();
		final Map<String, Field> dbCols = new HashMap<String, Field>();
		final List<Field> constraintFields = new ArrayList<Field>();
		final Map<Field, String> comments = new HashMap<Field, String>();

		for (final Field field : fields) {
			if (Arrays.asList("modTime", "createTime", "createId", "modId", "beanManager")
					.contains(field.getName())) {
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

			for (final Method m : objc.getDeclaredMethods()) {
				if (m.getName().equals("set" + prop)) {
					setters.put(field, m);
					if (settable) {
						if (updaters.put(field, m) != null) {
							throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "set"
									+ prop + " is ambiguous");
						}
					}
				}
			}
			if (settable && updaters.get(field) == null) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "set" + prop
						+ " not found for " + objc.getSimpleName());
			}

			if (getters.get(field).getReturnType().equals(String.class)) {
				stringFields.put(field, length);
			}

		}

		Set<Field> nnf = new HashSet<Field>(notNullableFields);

		Table tableAnnot = objectClass.getAnnotation(Table.class);
		if (tableAnnot != null) {
			int ncons = tableAnnot.uniqueConstraints().length;
			if (ncons == 1) {
				UniqueConstraint constraint = tableAnnot.uniqueConstraints()[0];
				for (String colNam : Arrays.asList(constraint.columnNames())) {
					Field col = dbCols.get(colNam);
					if (col == null) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Column "
								+ colNam + " mentioned in UniqueConstraint of "
								+ objectClass.getSimpleName() + " table is not present in entity");
					}
					if (!nnf.contains(col)) {
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Column "
								+ colNam + " mentioned in UniqueConstraint of "
								+ objectClass.getSimpleName()
								+ " table must be annotated as 'nullable = false'");
					}
					constraintFields.add(col);
				}
			} else if (ncons > 1) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Problem with "
						+ objectClass.getSimpleName()
						+ ": at most one constraint may be defined for an ICAT entity");
			}
		}

		Comment comment = objectClass.getAnnotation(Comment.class);
		String commentString = comment == null ? null : comment.value();

		Constructor<? extends EntityBaseBean> constructor = null;
		try {
			constructor = objectClass.getConstructor();
		} catch (Exception e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " "
					+ e.getMessage());
		}

		StringBuilder exportNull = new StringBuilder();
		StringBuilder exportHeader = new StringBuilder(objectClass.getSimpleName() + "(");
		StringBuilder exportHeaderAll = new StringBuilder(objectClass.getSimpleName() + "(");
		String sep = "";
		String sepAll = "";
		int n = 0;
		int nAll = 0;
		Set<Field> cons = new HashSet<>(constraintFields);
		if (cons.isEmpty()) {
			exportHeader.append("?:0");
			exportHeaderAll.append("?:0");
			sep = ",";
			sepAll = ",";
			n = 1;
			nAll = 1;
		}

		for (String s : systemAttributes) {
			exportHeaderAll.append(sepAll);
			sepAll = ",";
			exportHeaderAll.append(s + ":" + nAll++);
		}

		for (final Field field : fields) {
			if (Arrays.asList("id", "modTime", "createTime", "createId", "modId", "beanManager")
					.contains(field.getName()) || field.getAnnotation(OneToMany.class) != null) {
				continue;
			}

			exportHeader.append(sep);
			sep = ",";
			exportHeaderAll.append(sepAll);
			sepAll = ",";
			exportHeader.append(field.getName());
			exportHeaderAll.append(field.getName());
			if (field.getAnnotation(ManyToOne.class) == null) {
				exportHeader.append(":" + n++);
				exportHeaderAll.append(":" + nAll++);
				if (cons.contains(field)) {
					if (exportNull.length() == 0) {
						exportNull.append("NULL");
					} else {
						exportNull.append(",NA");
					}
				}
			} else {
				exportHeader.append('(');
				exportHeaderAll.append('(');
				n = setRelHeader(n, field, exportHeader, exportNull, cons.contains(field));
				nAll = setRelHeader(nAll, field, exportHeaderAll, null, cons.contains(field));
				exportHeader.append(')');
				exportHeaderAll.append(')');
			}

		}
		exportHeader.append(')');
		exportHeaderAll.append(')');

		Iterator<Field> iter = fields.iterator();
		while (iter.hasNext()) {
			if (Arrays.asList("id", "modTime", "createTime", "createId", "modId", "beanManager")
					.contains(iter.next().getName())) {
				iter.remove();
			}
		}

		return new PrivateEntityInfo(rels, notNullableFields, getters, stringFields, setters,
				updaters, constraintFields, commentString, comments, ones, attributes, constructor,
				fieldsByName, exportHeader.toString(), exportNull.toString(), fields,
				exportHeaderAll.toString());
	}

	public Set<Field> getAttributes(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.attributes;
		}
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

	public List<Field> getConstraintFields(Class<? extends EntityBaseBean> objectClass)
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

	public Constructor<? extends EntityBaseBean> getConstructor(
			Class<? extends EntityBaseBean> objectClass) throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.constructor;
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
		List<Field> constraint = getConstraintFields(beanClass);
		if (!constraint.isEmpty()) {
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
			ef.setType(rel.getDestinationBean().getSimpleName());
			if (rel.isCollection()) {
				ef.setRelType(EntityField.RelType.MANY);
			} else {
				ef.setRelType(EntityField.RelType.ONE);
			}
		}
		return entityInfo;

	}

	public String getExportHeader(Class<? extends EntityBaseBean> objectClass) throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.exportHeader;
		}
	}

	public String getExportHeaderAll(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.exportHeaderAll;
		}
	}

	public String getExportNull(Class<? extends EntityBaseBean> objectClass) throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.exportNull;
		}
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

	private List<Field> getNormalFields(Class<?> cobj) {
		List<Field> fields = new ArrayList<Field>(Arrays.asList(cobj.getDeclaredFields()));
		final Iterator<Field> iter = fields.iterator();
		while (iter.hasNext()) {
			final Field f = iter.next();
			int modifier = f.getModifiers();
			if (f.getName().startsWith("_")) {
				iter.remove();
			} else if (Modifier.isStatic(modifier)) {
				iter.remove();
			} else if (Modifier.isTransient(modifier)) {
				iter.remove();
			} else if (f.isAnnotationPresent(XmlTransient.class)) {
				iter.remove();
			}
		}
		return fields;
	}

	public Map<String, Field> getFieldsByName(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.fieldsByName;
		}
	}

	/** map from field to its getter - includes the id */
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

	public Map<Field, Method> getSettersForUpdate(Class<? extends EntityBaseBean> objectClass)
			throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.updaters;
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

	/* Returns all user settable fields (not id, createId, modId, createTime nor modTime */
	public List<Field> getFields(Class<? extends EntityBaseBean> objectClass) throws IcatException {
		PrivateEntityInfo ei = null;
		synchronized (this.map) {
			ei = this.map.get(objectClass);
			if (ei == null) {
				ei = this.buildEi(objectClass);
				this.map.put(objectClass, ei);
			}
			return ei.fields;
		}
	}

	private int setRelHeader(int n, Field field, StringBuilder exportHeader,
			StringBuilder exportNull, boolean con) {

		Set<String> conColumns = new HashSet<>();
		Table tableAnnot = field.getType().getAnnotation(Table.class);
		if (tableAnnot != null) {

			List<UniqueConstraint> constraints = Arrays.asList(tableAnnot.uniqueConstraints());
			if (!constraints.isEmpty()) {
				UniqueConstraint constraint = constraints.get(0);
				conColumns = new HashSet<>(Arrays.asList(constraint.columnNames()));
			}
		}

		if (conColumns.isEmpty()) {
			exportHeader.append("?:" + n++);
		}

		List<Field> fields = new ArrayList<Field>();
		Class<?> cobj = field.getType();
		while (cobj != null) {
			fields.addAll(getNormalFields(cobj));
			cobj = cobj.getSuperclass();
		}
		Collections.sort(fields, fieldComparator);

		String sep = "";
		for (Field f : fields) {
			String colName = null;
			Column col = f.getAnnotation(Column.class);
			if (col != null) {
				colName = col.name();
			} else {
				JoinColumn jcol = f.getAnnotation(JoinColumn.class);
				if (jcol != null) {
					colName = jcol.name();
				}
			}
			if (colName != null) {
				if (conColumns.contains(colName)) {
					if (EntityBaseBean.class.isAssignableFrom(f.getType())) {
						exportHeader.append(sep + f.getName() + '(');
						n = setRelHeader(n, f, exportHeader, exportNull, con);
						exportHeader.append(')');
					} else {
						exportHeader.append(sep + f.getName() + ":" + n++);
						if (con && exportNull != null) {
							if (exportNull.length() == 0) {
								exportNull.append("NULL");
							} else {
								exportNull.append(",NA");
							}
						}
					}
					sep = ",";
				}
			}

		}

		return n;
	}

}
