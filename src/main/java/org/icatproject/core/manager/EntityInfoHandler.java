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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.xml.bind.annotation.XmlTransient;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.Affiliation;
import org.icatproject.core.entity.Application;
import org.icatproject.core.entity.Comment;
import org.icatproject.core.entity.DataCollection;
import org.icatproject.core.entity.DataCollectionDatafile;
import org.icatproject.core.entity.DataCollectionDataset;
import org.icatproject.core.entity.DataCollectionInvestigation;
import org.icatproject.core.entity.DataCollectionParameter;
import org.icatproject.core.entity.DataPublication;
import org.icatproject.core.entity.DataPublicationDate;
import org.icatproject.core.entity.DataPublicationFunding;
import org.icatproject.core.entity.DataPublicationType;
import org.icatproject.core.entity.DataPublicationUser;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetTechnique;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.FacilityCycle;
import org.icatproject.core.entity.FundingReference;
import org.icatproject.core.entity.Grouping;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.DatasetInstrument;
import org.icatproject.core.entity.InstrumentScientist;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationFacilityCycle;
import org.icatproject.core.entity.InvestigationFunding;
import org.icatproject.core.entity.InvestigationGroup;
import org.icatproject.core.entity.InvestigationInstrument;
import org.icatproject.core.entity.InvestigationParameter;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Job;
import org.icatproject.core.entity.Keyword;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.PermissibleStringValue;
import org.icatproject.core.entity.PublicStep;
import org.icatproject.core.entity.Publication;
import org.icatproject.core.entity.RelatedDatafile;
import org.icatproject.core.entity.RelatedItem;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleParameter;
import org.icatproject.core.entity.SampleType;
import org.icatproject.core.entity.Shift;
import org.icatproject.core.entity.Study;
import org.icatproject.core.entity.StudyInvestigation;
import org.icatproject.core.entity.Technique;
import org.icatproject.core.entity.User;
import org.icatproject.core.entity.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Note that this does not use a Singleton Bean as there is no need for the
// extra complexity and
// that the instance is created statically as we know it will be needed.
public class EntityInfoHandler {

	private static class PrivateEntityInfo {

		final Set<Field> attributes;
		final String classComment;
		final List<Field> constraintFields;
		final Constructor<? extends EntityBaseBean> constructor;
		final String exportHeader;
		final String exportHeaderAll;
		final String exportNull;
		final Map<Field, String> fieldComments;
		final Map<String, Field> fieldsByName;
		final Map<Field, Method> getters;
		final List<Field> notNullableFields;
		final Set<Relationship> ones;
		final Set<Relationship> relatedEntities;
		final Map<Field, Method> setters;
		final Map<Field, Integer> stringFields;
		final Map<Field, Method> updaters;
		final List<Field> fields;
		final Map<String, Method> gettersFromName;
		final Map<String, Relationship> relationshipsByName;
		final Set<Field> relInKey;
		final boolean hasLuceneDoc;

		public PrivateEntityInfo(Set<Relationship> rels, List<Field> notNullableFields, Map<Field, Method> getters,
				Map<String, Method> gettersFromName, Map<Field, Integer> stringFields, Map<Field, Method> setters,
				Map<Field, Method> updaters, List<Field> constraintFields, String classComment,
				Map<Field, String> fieldComments, Set<Relationship> ones, Set<Field> attributes,
				Constructor<? extends EntityBaseBean> constructor, Map<String, Field> fieldByName, String exportHeader,
				String exportNull, List<Field> fields, String exportHeaderAll,
				Map<String, Relationship> relationshipsByName, Set<Field> relInKey, boolean hasLuceneDoc) {

			// Use copyOf to create unmodifiable collections
			this.relatedEntities = Set.copyOf(rels);
			this.notNullableFields = List.copyOf(notNullableFields);
			this.getters = Map.copyOf(getters);
			this.gettersFromName = Map.copyOf(gettersFromName);
			this.stringFields = Map.copyOf(stringFields);
			this.setters = Map.copyOf(setters);
			this.updaters = Map.copyOf(updaters);
			this.constraintFields = List.copyOf(constraintFields);
			this.classComment = classComment;
			this.fieldComments = Map.copyOf(fieldComments);
			this.ones = Set.copyOf(ones);
			this.attributes = Set.copyOf(attributes);
			this.constructor = constructor;
			this.fieldsByName = Map.copyOf(fieldByName);
			this.exportHeader = exportHeader;
			this.exportNull = exportNull;
			this.fields = List.copyOf(fields);
			this.exportHeaderAll = exportHeaderAll;
			this.relationshipsByName = Map.copyOf(relationshipsByName);
			this.relInKey = Set.copyOf(relInKey);
			this.hasLuceneDoc = hasLuceneDoc;
		}
	}

	public static class Relationship {

		private final boolean collection;

		private final Class<? extends EntityBaseBean> destinationBean;

		private final Field field;

		private final Method inverseSetter;

		private final Class<? extends EntityBaseBean> originBean;

		public Relationship(Class<? extends EntityBaseBean> originBean, Field field,
				Class<? extends EntityBaseBean> destinationBean, boolean collection, boolean cascaded,
				Method inverseSetter) {
			if (collection != cascaded) {
				throw new RuntimeException(
						"Collection and Cascaded must match for this code to work " + destinationBean + " " + field);
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
			return "From " + originBean.getSimpleName() + " to " + destinationBean.getSimpleName() + " by "
					+ this.field.getName() + (collection ? " many" : " one")
					+ (inverseSetter == null ? "" : " " + this.inverseSetter.getName());
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(EntityInfoHandler.class);

	private static final String[] SYSTEM_ATTRIBUTES = { "createId", "createTime", "modId", "modTime" };

	// All entities in export order
	private static final List<Class<? extends EntityBaseBean>> ENTITIES = List.of(User.class, Grouping.class,
			UserGroup.class, Rule.class, PublicStep.class, Facility.class, DatafileFormat.class, Application.class,
			Instrument.class, InvestigationType.class, DatasetType.class, ParameterType.class, SampleType.class,
			Investigation.class, Sample.class, Dataset.class, Datafile.class, FacilityCycle.class, DataCollection.class,
			DataCollectionDatafile.class, DataCollectionDataset.class, DataCollectionInvestigation.class, 
			DataCollectionParameter.class, DatafileParameter.class, DatasetParameter.class, InvestigationParameter.class, 
			Job.class, Keyword.class, Technique.class, DatasetTechnique.class, PermissibleStringValue.class, 
			Publication.class, RelatedDatafile.class, SampleParameter.class, Shift.class, Study.class, 
			DataPublication.class, DataPublicationDate.class, DataPublicationUser.class, DataPublicationType.class, 
			Affiliation.class, RelatedItem.class, FundingReference.class, DataPublicationFunding.class,
			InvestigationFunding.class, InvestigationUser.class, InvestigationGroup.class, StudyInvestigation.class, 
			InvestigationInstrument.class, InstrumentScientist.class, DatasetInstrument.class,
			InvestigationFacilityCycle.class);

	// All entity names in export order
	private static final List<String> EXPORT_ENTITY_NAMES =
		ENTITIES.stream().map((entity) -> entity.getSimpleName()).collect(Collectors.toUnmodifiableList());

	// All entity names in alphabetical order
	private static final List<String> ENTITY_NAMES =
		ENTITIES.stream().map((entity) -> entity.getSimpleName()).sorted().collect(Collectors.toUnmodifiableList());

	// Map of entity name -> entity class
	private static final Map<String, Class<? extends EntityBaseBean>> ENTITY_NAME_MAP =
		ENTITIES.stream().collect(Collectors.toUnmodifiableMap((entity) -> entity.getSimpleName(), (entity) -> entity));

	// Map of entity class -> PrivateEntityInfo
	private static final Map<Class<? extends EntityBaseBean>, PrivateEntityInfo> PRIVATE_ENTITY_INFO_MAP =
		ENTITIES.stream().collect(Collectors.toUnmodifiableMap((entity) -> entity, (entity) -> buildEi(entity)));

	private static EntityInfoHandler instance = new EntityInfoHandler();

	public static Class<? extends EntityBaseBean> getClass(String tableName) throws IcatException {
		Class<? extends EntityBaseBean> entityClass = ENTITY_NAME_MAP.get(tableName);

		if (entityClass == null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, tableName + " is not an ICAT entity");
		}

		return entityClass;
	}

	public static List<String> getEntityNamesList() {
		return ENTITY_NAMES;
	};

	public static List<String> getExportEntityNames() {
		return EXPORT_ENTITY_NAMES;
	};

	public static EntityInfoHandler getInstance() {
		return instance;
	}

	private EntityInfoHandler() {
	}

	private static PrivateEntityInfo buildEi(Class<? extends EntityBaseBean> objectClass) {
		logger.debug("Building PrivateEntityInfo for " + objectClass);
		List<Field> fields = new ArrayList<Field>();
		Class<?> cobj = objectClass;
		while (cobj != null) {
			fields.addAll(getNormalFields(cobj));
			cobj = cobj.getSuperclass();
		}

		fields.sort(Comparator.comparing(Field::getName));
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
						throw new IllegalStateException("Cascade must be all or nothing " + objectClass.getSimpleName() + "." + field.getName());
					}
					mappedBy = oneToMany.mappedBy();
					if (mappedBy == null) {
						throw new IllegalStateException("MappedBy must be set for " + objectClass.getSimpleName() + "." + field.getName());
					}
				} else {
					throw new IllegalStateException("Looks like a one to many relationship but not marked as such " + objectClass.getSimpleName() + "." + field.getName());
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

							String name = "set" + Character.toUpperCase(mappedBy.charAt(0)) + mappedBy.substring(1);
							Method[] ms = argc2.getMethods();
							boolean inv = false;
							for (Method m : ms) {
								if (m.getName().equals(name)) {
									rels.add(new Relationship(objectClass, field, argc2, true, all, m));
									inv = true;
									break;
								}
							}
							if (!inv) {
								throw new IllegalStateException("Inverse relationship not found " + objectClass.getSimpleName() + "." + field.getName());
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
						throw new IllegalStateException("Cascade must be all or nothing " + objectClass.getSimpleName() + "." + field.getName());
					}
				} else {
					throw new IllegalStateException("Looks like a many to one relationship but not marked as such " + objectClass.getSimpleName() + "." + field.getName());
				}
				@SuppressWarnings("unchecked")
				final Class<? extends EntityBaseBean> argc2 = (Class<? extends EntityBaseBean>) field.getType();
				rels.add(new Relationship(objectClass, field, argc2, false, all, null));
			} else if (!Arrays.asList("modTime", "createTime", "createId", "modId", "id").contains(field.getName())) {
				attributes.add(field);
			}

		}

		final Set<Relationship> includesToFollow = new HashSet<Relationship>();
		final Set<Relationship> ones = new HashSet<Relationship>();
		final Map<String, Relationship> relationshipsByName = new HashMap<>();

		for (Relationship rel : rels) {
			includesToFollow.add(rel);
			if (!rel.collection) {
				ones.add(rel);
			}
			relationshipsByName.put(rel.getField().getName(), rel);
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

			Class<?> objc = field.getDeclaringClass();
			final String name = field.getName();
			final String prop = Character.toUpperCase(name.charAt(0)) + name.substring(1);

			Method method;
			try {
				method = objc.getMethod("get" + prop);
			} catch (NoSuchMethodException e1) {
				try {
					method = objc.getMethod("is" + prop);
				} catch (NoSuchMethodException e2) {
					throw new IllegalStateException("No method get" + prop + "() or is" + prop+ "() found in class " + objc.getName());
				}
			}
			getters.put(field, method);
		}

		for (final Field field : fields) {

			if (Arrays.asList("modTime", "createTime", "createId", "modId", "beanManager").contains(field.getName())) {
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

			for (final Method m : objc.getDeclaredMethods()) {
				if (m.getName().equals("set" + prop)) {
					setters.put(field, m);
					if (settable) {
						if (updaters.put(field, m) != null) {
							throw new IllegalStateException("set" + prop + " is ambiguous");
						}
					}
				}
			}
			if (settable && updaters.get(field) == null) {
				throw new IllegalStateException("set" + prop + " not found for " + objc.getSimpleName());
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
						throw new IllegalStateException("Column " + colNam + " mentioned in UniqueConstraint of " + objectClass.getSimpleName()
										+ " table is not present in entity");
					}
					if (!nnf.contains(col)) {
						throw new IllegalStateException("Column " + colNam + " mentioned in UniqueConstraint of " + objectClass.getSimpleName()
										+ " table must be annotated as 'nullable = false'");
					}
					constraintFields.add(col);
				}
			} else if (ncons > 1) {
				throw new IllegalStateException("Problem with " + objectClass.getSimpleName() + ": at most one constraint may be defined for an ICAT entity");
			}
		}

		Comment comment = objectClass.getAnnotation(Comment.class);
		String commentString = comment == null ? null : comment.value();

		Constructor<? extends EntityBaseBean> constructor = null;
		try {
			constructor = objectClass.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Zero-argument constructor not found for class " + objectClass.getName(), e);
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

		for (String s : SYSTEM_ATTRIBUTES) {
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

		Map<String, Method> gettersFromName = new HashMap<>();
		for (Entry<Field, Method> entry : getters.entrySet()) {
			gettersFromName.put(entry.getKey().getName(), entry.getValue());
		}

		Set<Field> relInKey = new HashSet<>();
		for (Relationship rel : ones) {
			if (constraintFields.contains(rel.field)) {
				relInKey.add(rel.field);
			}
		}

		boolean hasLuceneDoc = true;
		try {
			objectClass.getDeclaredMethod("getDoc", JsonGenerator.class);
		} catch (NoSuchMethodException e) {
			hasLuceneDoc = false;
		}

		return new PrivateEntityInfo(rels, notNullableFields, getters, gettersFromName, stringFields, setters, updaters,
				constraintFields, commentString, comments, ones, attributes, constructor, fieldsByName,
				exportHeader.toString(), exportNull.toString(), fields, exportHeaderAll.toString(), relationshipsByName,
				relInKey, hasLuceneDoc);
	}

	private static PrivateEntityInfo getPrivateEntityInfo(Class<? extends EntityBaseBean> objectClass) {
		PrivateEntityInfo ei = PRIVATE_ENTITY_INFO_MAP.get(objectClass);

		if (ei == null) {
			// Should never happen because the map contains all non-abstract classes that extend EntityBaseBean
			throw new IllegalStateException("Class not found in PRIVATE_ENTITY_INFO_MAP: " + objectClass.getName());
		}

		return ei;
	};

	/**
	 * Set of fields that are "simple attributes". This excludes relationships,
	 * id, createId, CreateTime, modId and modTime
	 */
	public Set<Field> getAttributes(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).attributes;
	}

	public String getClassComment(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).classComment;
	}

	public List<Field> getConstraintFields(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).constraintFields;
	}

	public Constructor<? extends EntityBaseBean> getConstructor(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).constructor;
	}

	@SuppressWarnings("unchecked")
	public EntityInfo getEntityInfo(String beanName) throws IcatException {
		Class<? extends EntityBaseBean> beanClass = getClass(beanName);

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

	public String getExportHeader(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).exportHeader;
	}

	public String getExportHeaderAll(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).exportHeaderAll;
	}

	public String getExportNull(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).exportNull;
	}

	public Map<Field, String> getFieldComments(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).fieldComments;
	}

	/**
	 * Returns all user settable fields (not id, createId, modId, createTime nor
	 * modTime
	 */
	public List<Field> getFields(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).fields;
	}

	public Map<String, Field> getFieldsByName(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).fieldsByName;
	}

	/**
	 * Map from field to getter for all fields
	 */
	public Map<Field, Method> getGetters(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).getters;
	}

	/**
	 * Map from field name to getter for all fields
	 */
	public Map<String, Method> getGettersFromName(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).gettersFromName;
	}

	private static List<Field> getNormalFields(Class<?> cobj) {
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

	public List<Field> getNotNullableFields(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).notNullableFields;
	}

	public Set<Relationship> getOnes(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).ones;
	}

	public Set<Relationship> getRelatedEntities(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).relatedEntities;
	}

	/**
	 * Map from field name to relationship
	 * 
	 * @throws IcatException
	 */
	public Map<String, Relationship> getRelationshipsByName(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).relationshipsByName;
	}

	public Set<Field> getRelInKey(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).relInKey;
	}

	/**
	 * Map from field to setter for all fields including id but not for
	 * createId, modId, createTime nor modTime
	 */
	public Map<Field, Method> getSetters(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).setters;
	}

	/**
	 * Returns the setters for those fields that are not one to many
	 * relationships and not id, createId, modId, createTime nor modTime
	 */
	public Map<Field, Method> getSettersForUpdate(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).updaters;
	}

	/**
	 * Returns all string fields except for createId and modId
	 */
	public Map<Field, Integer> getStringFields(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).stringFields;
	}

	/** Return true if getDoc() method exists else false */
	public boolean hasLuceneDoc(Class<? extends EntityBaseBean> objectClass) {
		return getPrivateEntityInfo(objectClass).hasLuceneDoc;
	}

	private static int setRelHeader(int n, Field field, StringBuilder exportHeader, StringBuilder exportNull, boolean con) {

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
		fields.sort(Comparator.comparing(Field::getName));

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
