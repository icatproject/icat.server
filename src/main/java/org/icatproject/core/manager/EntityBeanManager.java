package org.icatproject.core.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.jms.JMSException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationInstrument;
import org.icatproject.core.entity.ParameterValueType;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.Session;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.PropertyHandler.CallType;
import org.icatproject.core.manager.PropertyHandler.Operation;
import org.icatproject.core.manager.search.FacetDimension;
import org.icatproject.core.manager.search.ScoredEntityBaseBean;
import org.icatproject.core.manager.search.SearchManager;
import org.icatproject.core.manager.search.SearchResult;
import org.icatproject.core.oldparser.OldGetQuery;
import org.icatproject.core.oldparser.OldInput;
import org.icatproject.core.oldparser.OldLexerException;
import org.icatproject.core.oldparser.OldParserException;
import org.icatproject.core.oldparser.OldSearchQuery;
import org.icatproject.core.oldparser.OldTokenizer;
import org.icatproject.core.parser.GetQuery;
import org.icatproject.core.parser.IncludeClause;
import org.icatproject.core.parser.IncludeClause.Step;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.LexerException;
import org.icatproject.core.parser.ParserException;
import org.icatproject.core.parser.SearchQuery;
import org.icatproject.core.parser.Token;
import org.icatproject.core.parser.Tokenizer;
import org.icatproject.core.utils.JpqlChecker;
import org.icatproject.utils.IcatSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EntityBeanManager {

	private class EntitySetResult {

		private SearchQuery searchQuery;
		private List<?> result;

		public EntitySetResult(SearchQuery searchQuery, List<?> result) {
			this.searchQuery = searchQuery;
			this.result = result;
		}

	}

	public enum PersistMode {
		CLONE, REST, IMPORTALL, IMPORT_OR_WS
	};

	private final static DateFormat df8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	private static final String linesep = System.getProperty("line.separator");

	private static DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	private static final Logger logger = LoggerFactory.getLogger(EntityBeanManager.class);

	private static final Pattern timestampPattern = Pattern.compile(":ts(\\d{14})");

	@EJB
	GateKeeper gateKeeper;

	@EJB
	PropertyHandler propertyHandler;

	@EJB
	NotificationTransmitter notificationTransmitter;

	@EJB
	Transmitter transmitter;

	@EJB
	SearchManager searchManager;

	@Resource
	UserTransaction userTransaction;

	@PersistenceContext(unitName = "icat")
	EntityManager entityManager;

	private boolean log;

	Marker fatal = MarkerFactory.getMarker("FATAL");

	private Set<CallType> logRequests;

	private Map<String, NotificationRequest> notificationRequests;

	private boolean searchActive;
	private long searchMaxSearchTimeMillis;

	private int maxEntities;
	private int searchSearchBlockSize;

	private long exportCacheSize;
	private Set<String> rootUserNames;

	private String key;

	private String buildKey(EntityBaseBean bean, Map<String, Map<Long, String>> exportCaches)
			throws IcatException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends EntityBaseBean> klass = bean.getClass();
		List<Field> constraintFields = EntityInfoHandler.getConstraintFields(klass);
		if (constraintFields.isEmpty()) {
			return '"' + bean.getId().toString() + '"';
		}
		List<Field> fields = EntityInfoHandler.getFields(klass);
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);
		Map<String, Field> fieldMap = EntityInfoHandler.getFieldsByName(klass);
		Set<Field> atts = EntityInfoHandler.getAttributes(klass);
		Set<Field> updaters = EntityInfoHandler.getSettersForUpdate(klass).keySet();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Field field : fields) {
			String name = field.getName();
			if (constraintFields.contains(field)) {
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				Object value = getters.get(fieldMap.get(name)).invoke(bean);
				if (atts.contains(field)) {
					sb.append(getRep(field, value));
				} else if (updaters.contains(field)) {
					long obId = ((EntityBaseBean) value).getId();
					String obType = value.getClass().getSimpleName();
					Map<Long, String> idCache = exportCaches.get(obType);
					String s = idCache.get(obId);
					if (s == null) {
						s = buildKey((EntityBaseBean) value, exportCaches);
						idCache.put(obId, s);
						logger.debug("Cached " + obType + " " + obId + " as " + s);
					}
					sb.append(s);
				}
			}
		}

		return sb.toString();
	}

	private boolean checkIdentityChange(EntityBaseBean thisBean, EntityBaseBean fromBean) throws IcatException {

		Class<? extends EntityBaseBean> klass = thisBean.getClass();
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);
		for (Field field : EntityInfoHandler.getRelInKey(klass)) {
			try {
				Method m = getters.get(field);
				EntityBaseBean newValue = (EntityBaseBean) m.invoke(fromBean, new Object[0]);
				if (newValue != null) {
					long newPk = newValue.getId();
					long oldPk = ((EntityBaseBean) m.invoke(thisBean)).getId();
					boolean idChange = newPk != oldPk;
					if (idChange) {
						logger.debug("Identity relationship field " + field.getName() + " of " + thisBean
								+ " is being changed from " + oldPk + " to " + newPk);
						return true;
					}
				} else {
					throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
							"Attempt to set field " + field.getName() + " of " + thisBean + " to null");
				}
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}
		}
		return false;
	}

	public CreateResponse create(String userId, EntityBaseBean bean, boolean allAttributes, String ip) throws IcatException {

		logger.info(userId + " creating " + bean.getClass().getSimpleName());
		try {
			userTransaction.begin();
			PersistMode persistMode;
			if (allAttributes) {
				persistMode = PersistMode.IMPORTALL;
			} else {
				persistMode = PersistMode.IMPORT_OR_WS;
			}
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				bean.preparePersist(userId, entityManager, persistMode);
				logger.trace(bean + " prepared for persist.");
				entityManager.persist(bean);
				logger.trace(bean + " persisted.");
				entityManager.flush();
				logger.trace(bean + " flushed.");
				// Check authz now everything persisted
				gateKeeper.performAuthorisation(userId, bean, AccessType.CREATE);
				NotificationMessage notification = new NotificationMessage(Operation.C, bean, notificationRequests);

				long beanId = bean.getId();

				if (searchActive) {
					bean.addToSearch(entityManager, searchManager);
				}
				userTransaction.commit();
				if (logRequests.contains(CallType.WRITE)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", userId);
						gen.write("entityName", bean.getClass().getSimpleName());
						gen.write("entityId", beanId);
						gen.writeEnd();
					}
					transmitter.processMessage("create", ip, baos.toString(), startMillis);
				}
				return new CreateResponse(beanId, notification);
			} catch (EntityExistsException e) {
				userTransaction.rollback();
				throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getMessage());
			} catch (IcatException e) {
				userTransaction.rollback();
				throw e;
			} catch (Throwable e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for creation of " + bean + " because of " + e.getClass() + " "
						+ e.getMessage());
				updateCache();

				bean.preparePersist(userId, entityManager, persistMode);
				isUnique(bean);
				isValid(bean);
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException" + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException" + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "NotSupportedException" + e.getMessage());
		}

	}

	private boolean createAllowed(String userId, EntityBaseBean bean) throws IcatException {
		try {
			userTransaction.begin();
			try {
				try {
					bean.preparePersist(userId, entityManager, PersistMode.IMPORT_OR_WS);
					logger.debug(bean + " prepared for persist (createAllowed).");
					entityManager.persist(bean);
					logger.debug(bean + " persisted (createAllowed).");
					entityManager.flush();
					logger.debug(bean + " flushed (createAllowed).");
				} catch (EntityExistsException e) {
					throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getMessage());
				} catch (Throwable e) {
					userTransaction.rollback();
					logger.debug("Transaction rolled back for creation of " + bean + " because of " + e.getClass() + " "
							+ e.getMessage());
					bean.preparePersist(userId, entityManager, PersistMode.IMPORT_OR_WS);
					isUnique(bean);
					isValid(bean);
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"Unexpected DB response " + e.getClass() + " " + e.getMessage());
				}
				try {
					gateKeeper.performAuthorisation(userId, bean, AccessType.CREATE);
					return true;
				} catch (IcatException e) {
					if (e.getType() != IcatExceptionType.INSUFFICIENT_PRIVILEGES) {
						throw e;
					}
					return false;
				} catch (Throwable e) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							e.getClass() + " " + e.getMessage());
				}
			} finally {
				if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
					userTransaction.rollback();
					logger.debug("Transaction rolled back (createAllowed)");
				}
			}
		} catch (IcatException e) {
			logger.debug(e.getClass() + " " + e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Transaction problem? " + e.getClass() + " " + e.getMessage());
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public List<CreateResponse> createMany(String userId, List<EntityBaseBean> beans, String ip) throws IcatException {
		try {
			userTransaction.begin();
			List<CreateResponse> crs = new ArrayList<CreateResponse>();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				for (EntityBaseBean bean : beans) {
					bean.preparePersist(userId, entityManager, PersistMode.IMPORT_OR_WS);
					logger.trace(bean + " prepared for persist.");
					entityManager.persist(bean);
					logger.trace(bean + " persisted.");
					entityManager.flush();
					logger.trace(bean + " flushed.");
					// Check authz now everything persisted
					gateKeeper.performAuthorisation(userId, bean, AccessType.CREATE);
					NotificationMessage notification = new NotificationMessage(Operation.C, bean, notificationRequests);
					CreateResponse cr = new CreateResponse(bean.getId(), notification);
					crs.add(cr);
				}
				userTransaction.commit();

				if (logRequests.contains(CallType.WRITE) && !crs.isEmpty()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", userId);
						gen.write("entityName", beans.get(0).getClass().getSimpleName());
						gen.write("entityId", crs.get(0).getPk());
						gen.writeEnd();
					}
					transmitter.processMessage("createMany", ip, baos.toString(), startMillis);
				}

				if (searchActive) {
					for (EntityBaseBean bean : beans) {
						bean.addToSearch(entityManager, searchManager);
					}
				}

				return crs;
			} catch (EntityExistsException e) {
				userTransaction.rollback();
				throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getMessage(),
						crs.size());
			} catch (IcatException e) {
				userTransaction.rollback();
				e.setOffset(crs.size());
				throw e;
			} catch (Throwable e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for creation because of " + e.getClass() + " " + e.getMessage());
				updateCache();
				int pos = crs.size();
				EntityBaseBean bean = beans.get(pos);
				try {
					bean.preparePersist(userId, entityManager, PersistMode.IMPORT_OR_WS);
					isUnique(bean);
					isValid(bean);
				} catch (IcatException e1) {
					e1.setOffset(pos);
					throw e1;
				}
				/*
				 * Now look for duplicates within the list of objects provided
				 */
				Class<? extends EntityBaseBean> entityClass = bean.getClass();
				Map<Field, Method> getters = EntityInfoHandler.getGetters(entityClass);

				List<Field> constraint = EntityInfoHandler.getConstraintFields(entityClass);
				if (!constraint.isEmpty()) {
					for (int i = 0; i < pos; i++) {
						boolean diff = false;
						for (Field f : constraint) {
							Object value = getValue(getters, f, bean);
							Object value2 = getValue(getters, f, beans.get(i));
							if (!value.equals(value2)) {
								logger.debug("No problem with object " + i + " as " + f.getName() + " has value "
										+ value2 + " and not " + value);
								diff = true;
								break;
							}
						}
						if (!diff) {
							StringBuilder erm = new StringBuilder();
							for (Field f : constraint) {
								if (erm.length() == 0) {
									erm.append(entityClass.getSimpleName() + " exists with ");
								} else {
									erm.append(", ");
								}
								erm.append(f.getName() + " = '" + getValue(getters, f, bean) + "'");
							}
							throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS,
									erm.toString(), pos);
						}
					}
				}
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage(), pos);

			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IllegalStateException" + e.getMessage(),
					-1);
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException" + e.getMessage(), -1);
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException" + e.getMessage(), -1);
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "NotSupportedException" + e.getMessage(),
					-1);
		}
	}

	public void delete(String userId, List<EntityBaseBean> beans, String ip) throws IcatException {
		if (beans == null) { // Wildlfy 10 receives null instead of empty list
			beans = Collections.emptyList();
		}
		logger.info("{} requests delete of {} entities", userId, beans.size());
		try {
			int offset = 0;
			userTransaction.begin();
			EntityBaseBean firstBean = null;
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;

				// A set is used because investigations have datasets - but also
				// have samples that have datasets. This is an easy way to deal
				// with this odd example.
				Set<EntityBaseBean> allBeansToDelete = new HashSet<>();
				for (EntityBaseBean bean : beans) {
					List<EntityBaseBean> beansToDelete = new ArrayList<>();
					EntityBaseBean beanManaged = find(bean);
					beansToDelete.add(beanManaged);
					beansToDelete.addAll(getDependentBeans(beanManaged));

					if (firstBean == null) {
						firstBean = beanManaged;
					}
					for (EntityBaseBean b : beansToDelete) {
						gateKeeper.performAuthorisation(userId, b, AccessType.DELETE);
					}
					entityManager.remove(beanManaged);
					entityManager.flush();
					logger.trace("Deleted bean " + bean + " flushed.");
					allBeansToDelete.addAll(beansToDelete);
					offset++;
				}

				userTransaction.commit();

				if (searchActive) {
					for (EntityBaseBean bean : allBeansToDelete) {
						searchManager.deleteDocument(bean);
					}
				}

				if (logRequests.contains(CallType.WRITE) && firstBean != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", userId);
						gen.write("entityName", firstBean.getClass().getSimpleName());
						gen.write("entityId", firstBean.getId());
						gen.writeEnd();
					}
					transmitter.processMessage("delete", ip, baos.toString(), startMillis);
				}
			} catch (IcatException e) {
				userTransaction.rollback();
				e.setOffset(offset);
				throw e;
			} catch (Throwable e) {
				logger.error("Problem in deleteMany", e);
				userTransaction.rollback();
				updateCache();
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage(), offset);
			}
		} catch (IllegalStateException | SecurityException | SystemException | NotSupportedException e) {
			logger.error("Problem in deleteMany", e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage(), -1);
		}
	}

	/**
	 * Export all data
	 */
	@SuppressWarnings("serial")
	public Response export(final String userId, final boolean allAttributes) {

		logger.info(userId + " exporting complete schema");

		final Map<String, Map<Long, String>> exportCaches = new HashMap<>();
		for (String s : EntityInfoHandler.getExportEntityNames()) {
			exportCaches.put(s, new LinkedHashMap<Long, String>() {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
					return size() > exportCacheSize;
				}
			});
		}

		StreamingOutput strOut = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException {
				logger.debug("Streaming of export file started");
				output.write(("# ICAT Export file written by " + userId + linesep).getBytes());
				output.write(("1.0" + linesep).getBytes());
				for (String s : EntityInfoHandler.getExportEntityNames()) {
					try {
						exportTable(s, null, output, exportCaches, allAttributes, userId);
					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						logger.error(baos.toString());
						throw new IOException(e.getClass() + " " + e.getMessage());
					}
				}
				output.close();
				logger.debug("Streaming of export file complete");
			}

		};
		return Response.ok().entity(strOut)
				.header("Content-Disposition", "attachment; filename=\"" + "icat.export" + "\"")
				.header("Accept-Ranges", "bytes").build();

	}

	/** export data described by the query */
	@SuppressWarnings("serial")
	public Response export(final String userId, String query, final boolean all) throws IcatException {

		logger.info(userId + " exporting " + query);

		EntitySetResult esr = getEntitySet(userId, query);
		List<?> result = esr.result;

		if (result.size() > 0) {
			if (result.get(0) instanceof EntityBaseBean) {
				IncludeClause include = esr.searchQuery.getIncludeClause();
				boolean one = false;
				List<Step> steps = null;
				if (include != null) {
					one = include.isOne();
					steps = include.getSteps();
				}
				final Map<String, Set<Long>> ids = new HashMap<>();
				for (String s : EntityInfoHandler.getExportEntityNames()) {
					ids.put(s, new HashSet<Long>());
				}
				for (Object beanManaged : result) {
					((EntityBaseBean) beanManaged).collectIds(ids, one, 0, steps, gateKeeper, userId);
				}
				result = null; // Give gc a chance

				final Map<String, Map<Long, String>> exportCaches = new HashMap<>();
				for (String s : EntityInfoHandler.getExportEntityNames()) {
					exportCaches.put(s, new LinkedHashMap<Long, String>() {
						@Override
						protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
							return size() > exportCacheSize;
						}
					});
				}

				StreamingOutput strOut = new StreamingOutput() {

					@Override
					public void write(OutputStream output) throws IOException {
						output.write(("# ICAT Export file written by " + userId + linesep).getBytes());
						output.write(("1.0" + linesep).getBytes());
						for (String s : EntityInfoHandler.getExportEntityNames()) {
							Set<Long> table = ids.get(s);
							if (!table.isEmpty()) {
								try {
									exportTable(s, table, output, exportCaches, all, userId);
								} catch (IOException e) {
									throw e;
								} catch (Exception e) {
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									e.printStackTrace(new PrintStream(baos));
									logger.error(baos.toString());
									throw new IOException(e.getClass() + " " + e.getMessage());
								}
							}
						}
						output.close();
					}

				};
				return Response.ok().entity(strOut)
						.header("Content-Disposition", "attachment; filename=\"" + "icat.export" + "\"")
						.header("Accept-Ranges", "bytes").build();

			}
		} else {
			// Do nothing?
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void exportBean(EntityBaseBean bean, OutputStream output, boolean qcolumn, boolean all, List<Field> fields,
			Set<Field> updaters, Map<String, Map<Long, String>> exportCaches, Map<Field, Method> getters,
			Map<String, Field> fieldMap, Set<Field> atts) throws IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IcatException {
		boolean first = true;
		if (qcolumn) {
			output.write(('"' + bean.getId().toString() + '"').getBytes());
			first = false;
		}
		if (all) {
			if (first) {
				first = false;
			} else {
				output.write(",".getBytes());
			}
			synchronized (df8601) {
				output.write(("\"" + bean.getCreateId() + "\",").getBytes());
				output.write((df8601.format(bean.getCreateTime()) + ",").getBytes());
				output.write(("\"" + bean.getModId() + "\",").getBytes());
				output.write((df8601.format(bean.getModTime())).getBytes());
			}
		}
		for (Field field : fields) {
			String name = field.getName();
			if (updaters.contains(field)) {
				if (first) {
					first = false;
				} else {
					output.write(",".getBytes());
				}
				Object value = getters.get(fieldMap.get(name)).invoke(bean);
				if (atts.contains(field)) {
					if (value == null) {
						output.write("NULL".getBytes());
					} else {
						output.write(getRep(field, value).getBytes());
					}
				} else {
					if (value == null) {
						output.write(
								EntityInfoHandler.getExportNull((Class<? extends EntityBaseBean>) field.getType()).getBytes());
					} else {
						long obId = ((EntityBaseBean) value).getId();
						String obType = value.getClass().getSimpleName();
						Map<Long, String> idCache = exportCaches.get(obType);
						String s = idCache.get(obId);
						if (s == null) {
							s = buildKey((EntityBaseBean) value, exportCaches);
							idCache.put(obId, s);
							logger.debug("Cached " + obType + " " + obId + " as " + s);
						}
						output.write(s.getBytes());
					}
				}
			}
		}
		output.write((linesep).getBytes());
	}

	/**
	 * Export part of a table as specified by the list of ids or the complete
	 * table
	 */
	private void exportTable(String beanName, Set<Long> ids, OutputStream output,
			Map<String, Map<Long, String>> exportCaches, boolean allAttributes, String userId) throws IcatException,
			IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		logger.debug("Export " + (ids == null ? "complete" : "partial") + " " + beanName);
		Class<? extends EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		output.write((linesep).getBytes());
		if (allAttributes) {
			output.write(EntityInfoHandler.getExportHeaderAll(klass).getBytes());
		} else {
			output.write(EntityInfoHandler.getExportHeader(klass).getBytes());
		}
		output.write((linesep).getBytes());
		List<Field> fields = EntityInfoHandler.getFields(klass);
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);
		Map<String, Field> fieldMap = EntityInfoHandler.getFieldsByName(klass);
		Set<Field> atts = EntityInfoHandler.getAttributes(klass);
		Set<Field> updaters = EntityInfoHandler.getSettersForUpdate(klass).keySet();
		boolean qcolumn = EntityInfoHandler.getConstraintFields(klass).isEmpty();
		boolean notRootUser = !rootUserNames.contains(userId);

		if (ids == null) {
			int start = 0;

			while (true) {
				/* Get beans in blocks. */
				List<EntityBaseBean> beans = entityManager
						.createQuery("SELECT e from " + beanName + " e ORDER BY e.id", EntityBaseBean.class)
						.setFirstResult(start).setMaxResults(500).getResultList();

				if (beans.size() == 0) {
					break;
				}
				for (EntityBaseBean bean : beans) {
					if (notRootUser) {
						try {
							gateKeeper.performAuthorisation(userId, bean, AccessType.READ);
						} catch (IcatException e) {
							if (e.getType() == IcatExceptionType.INSUFFICIENT_PRIVILEGES) {
								continue;
							}
						}
					}
					exportBean(bean, output, qcolumn, allAttributes, fields, updaters, exportCaches, getters, fieldMap,
							atts);
				}
				start = start + beans.size();
			}
		} else {
			for (Long id : ids) {
				EntityBaseBean bean = entityManager.find(klass, id);
				if (bean != null) {
					exportBean(bean, output, qcolumn, allAttributes, fields, updaters, exportCaches, getters, fieldMap,
							atts);
				}
			}
		}
	}

	/**
	 * Performs authorisation for READ access on the newResults. Instead of
	 * returning the entries which can be READ, they are added to the end of
	 * acceptedResults, ensuring it doesn't exceed maxCount or maxEntities.
	 * 
	 * @param acceptedResults List containing already authorised entities. Entries
	 *                        in newResults that pass authorisation will be added to
	 *                        acceptedResults.
	 * @param newResults      List containing new results to check READ access to.
	 *                        Entries in newResults that pass authorisation will be
	 *                        added to acceptedResults.
	 * @param maxCount        The maximum size of acceptedResults. Once reached, no
	 *                        more entries from newResults will be added.
	 * @param userId          The user attempting to read the newResults.
	 * @param klass           The Class of the EntityBaseBean that is being
	 *                        filtered.
	 * @throws IcatException If more entities than the configuration option
	 *                       maxEntities would be added to acceptedResults, then an
	 *                       IcatException is thrown instead.
	 */
	private ScoredEntityBaseBean filterReadAccess(List<ScoredEntityBaseBean> acceptedResults, List<ScoredEntityBaseBean> newResults,
			int maxCount, String userId, Class<? extends EntityBaseBean> klass) throws IcatException {

		logger.debug("Got " + newResults.size() + " results from search engine");
		Set<Long> allowedIds = gateKeeper.getReadableIds(userId, newResults, klass.getSimpleName());
		if (allowedIds == null) {
			// A null result means there are no restrictions on the readable ids, so add as
			// many newResults as we need to reach maxCount
			int needed = maxCount - acceptedResults.size();
			if (newResults.size() > needed) {
				acceptedResults.addAll(newResults.subList(0, needed));
				return newResults.get(needed - 1);
			} else {
				acceptedResults.addAll(newResults);
			}
			if (acceptedResults.size() > maxEntities) {
				throw new IcatException(IcatExceptionType.VALIDATION,
						"attempt to return more than " + maxEntities + " entities");
			}
		} else {
			// Otherwise, add results in order until we reach maxCount
			for (ScoredEntityBaseBean newResult : newResults) {
				if (allowedIds.contains(newResult.getId())) {
					acceptedResults.add(newResult);
					if (acceptedResults.size() > maxEntities) {
						throw new IcatException(IcatExceptionType.VALIDATION,
								"attempt to return more than " + maxEntities + " entities");
					}
					if (acceptedResults.size() == maxCount) {
						logger.debug("maxCount {} reached", maxCount);
						return newResult;
					}
				}
			}
		}
		return null;
	}

	private EntityBaseBean find(EntityBaseBean bean) throws IcatException {
		Long primaryKey = bean.getId();
		Class<? extends EntityBaseBean> entityClass = bean.getClass();
		if (primaryKey == null) {
			throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND,
					entityClass.getSimpleName() + " has null primary key.");
		}
		EntityBaseBean object = null;
		try {
			object = entityManager.find(entityClass, primaryKey);
		} catch (Throwable e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Unexpected DB response " + e);
		}

		if (object == null) {
			throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND,
					entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}
		return object;
	}

	public EntityBaseBean get(String userId, String query, long primaryKey, String ip) throws IcatException {

		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.debug(userId + " issues get for " + query);
		String[] words = query.trim().split("\\s+");
		if (words.length > 1 && words[1].toUpperCase().equals("INCLUDE")) {
			try {
				query = new OldGetQuery(new OldInput(OldTokenizer.getTokens(query))).getNewQuery();
				logger.debug("new style query: " + query);
			} catch (OldLexerException e) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
			} catch (OldParserException e) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
			}
		}
		GetQuery getQuery;
		try {
			getQuery = new GetQuery(new Input(Tokenizer.getTokens(query)), gateKeeper);
		} catch (LexerException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		} catch (ParserException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}

		Class<? extends EntityBaseBean> entityClass = getQuery.getBean();

		EntityBaseBean result;

		EntityBaseBean beanManaged = entityManager.find(entityClass, primaryKey);
		if (beanManaged == null) {
			throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND,
					entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}

		gateKeeper.performAuthorisation(userId, beanManaged, AccessType.READ);
		logger.debug("got " + entityClass.getSimpleName() + "[id:" + primaryKey + "]");

		IncludeClause include = getQuery.getInclude();
		boolean one = false;
		List<Step> steps = null;
		if (include != null) {
			one = include.isOne();
			steps = include.getSteps();
		}
		result = beanManaged.pruned(one, 0, steps, maxEntities, gateKeeper, userId);
		logger.debug("Obtained " + result.getDescendantCount(maxEntities) + " entities.");

		if (logRequests.contains(CallType.READ)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userId);
				gen.write("entityName", result.getClass().getSimpleName());
				gen.write("entityId", result.getId());
				gen.write("query", query);
				gen.writeEnd();
			}
			transmitter.processMessage("get", ip, baos.toString(), startMillis);
		}
		return result;
	}

	private List<EntityBaseBean> getDependentBeans(EntityBaseBean bean) throws IcatException {
		Class<? extends EntityBaseBean> klass = bean.getClass();
		Set<Relationship> rs = EntityInfoHandler.getRelatedEntities(klass);
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);
		List<EntityBaseBean> beans = new ArrayList<>();
		for (Relationship r : rs) {
			if (r.isCollection()) {
				Method m = getters.get(r.getField());
				try {
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> collection = (List<EntityBaseBean>) m.invoke(bean);
					for (EntityBaseBean b : collection) {
						beans.add(b);
						beans.addAll(getDependentBeans(b));
					}
				} catch (Exception e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				}
			}
		}
		return beans;
	}

	public EntityInfo getEntityInfo(String beanName) throws IcatException {
		return EntityInfoHandler.getEntityInfo(beanName);
	}

	@SuppressWarnings("unchecked")
	private EntitySetResult getEntitySet(String userId, String query) throws IcatException {

		if (query == null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, "query may not be null");
		}

		if (!query.toUpperCase().trim().startsWith("SELECT")) {

			/* Parse the query */
			try {
				OldSearchQuery oldSearchQuery = new OldSearchQuery(new OldInput(OldTokenizer.getTokens(query)));
				query = oldSearchQuery.getNewQuery();
			} catch (OldLexerException e) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
			} catch (OldParserException e) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
			}
			logger.debug("new style query: " + query);

		}
		/* New style query - parse it */
		List<Token> tokens = null;
		try {
			tokens = Tokenizer.getTokens(query);
		} catch (LexerException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}
		Input input = new Input(tokens);
		SearchQuery q;
		try {
			q = new SearchQuery(input, gateKeeper, userId);
		} catch (ParserException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}

		/* Get the JPQL which includes authz restrictions */
		String jpql = q.getJPQL(userId, entityManager);
		logger.info("Final search JPQL: " + jpql);

		/* Null query indicates that nothing accepted by authz */
		if (jpql == null) {
			return new EntitySetResult(q, q.getNoAuthzResult());
		}

		/* Create query - which may go wrong */
		Query jpqlQuery;
		try {
			jpqlQuery = entityManager.createQuery(jpql);
		} catch (IllegalArgumentException e) {
			/*
			 * Parse the original query but without trailing LIMIT and INCLUDE
			 * clauses
			 */
			try {
				input.reset();
				StringBuilder sb = new StringBuilder();
				Token token = null;
				token = input.consume();

				while (token != null && token.getType() != Token.Type.LIMIT && token.getType() != Token.Type.INCLUDE) {
					if (sb.length() != 0) {
						sb.append(" ");
					}
					sb.append(token.getValue());
					token = input.consume();
				}
				JpqlChecker.checkJPQL(sb.toString(), entityManager);

			} catch (ParserException e1) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Parsing error should not occur as already parsed " + e.getMessage());
			}
			throw new IcatException(IcatExceptionType.INTERNAL, "Derived JPQL reports " + e.getMessage());
		}

		/* add parameter values for any timestamps */
		Matcher m = timestampPattern.matcher(jpql);
		while (m.find()) {
			Date d = null;
			try {
				synchronized (df) { // Access to data formats must be
					// synchronized
					d = df.parse(m.group(1));
				}
			} catch (ParseException e) {
				// This cannot happen - honest
			}
			jpqlQuery.setParameter("ts" + m.group(1), d);
		}
		try {
			jpqlQuery.setParameter("user", userId);
		} catch (IllegalArgumentException e) {
			// Ignore
		}

		/*
		 * Set the range of results to return and ensure don't exceed
		 * maxEntities
		 */
		Integer offset = q.getOffset();
		if (offset != null) {
			jpqlQuery.setFirstResult(offset);
		}
		Integer number = q.getNumber();
		jpqlQuery.setMaxResults(maxEntities + 1);
		if (number != null && number <= maxEntities) {
			jpqlQuery.setMaxResults(number);
		}

		List<Object> objects = jpqlQuery.getResultList();
		if (objects.size() > maxEntities) {
			throw new IcatException(IcatExceptionType.VALIDATION,
					"attempt to process more than " + maxEntities + " entities");
		}

		List<Object> result = null;

		result = (List<Object>) objects;
		// eclipselink returns BigDecimal for aggregate
		// functions on Long and Double for oracle
		if (result.size() == 1) {
			logger.debug("One result only - look for bad return types");
			Object obj = result.get(0);
			if (obj != null) {
				if (obj.getClass() == BigDecimal.class) {
					String typeQueryString = q.typeQuery();
					logger.debug("Type query for BigDecimal: " + typeQueryString);
					Class<? extends Object> klass = null;
					try {
						Query typeQuery = entityManager.createQuery(typeQueryString).setMaxResults(1);
						klass = typeQuery.getSingleResult().getClass();
					} catch (Exception e) {
						throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
								"Unable to handle query " + q + " with Oracle DB");
					}
					if (klass == Long.class) {
						result.set(0, ((BigDecimal) obj).longValue());
					} else if (klass == Double.class) {
						result.set(0, ((BigDecimal) obj).doubleValue());
					} else
						throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
								"Unable to handle query " + q + " with Oracle DB");
				}
			}
		}

		logger.debug("Obtained " + result.size() + " results.");
		return new EntitySetResult(q, result);
	}

	public List<String> getProperties() {
		return propertyHandler.props();
	}

	public double getRemainingMinutes(String sessionId) throws IcatException {
		logger.debug("getRemainingMinutes for sessionId " + sessionId);
		Session session = getSession(sessionId);
		return session.getRemainingMinutes();
	}

	private String getRep(Field field, Object value) throws IcatException {
		String type = field.getType().getSimpleName();
		if (type.equals("String")) {
			return "\"" + ((String) value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "\\t")
					.replace("\r", "\\r").replace("\n", "\\n").replace("\f", "\\f").replace("\b", "\\b") + "\"";
		} else if (type.equals("Integer")) {
			return ((Integer) value).toString();
		} else if (type.equals("Double")) {
			return value.toString();
		} else if (type.equals("Long")) {
			return value.toString();
		} else if (type.equals("boolean")) {
			return ((Boolean) value).toString();
		} else if (type.equals("Date")) {
			synchronized (df8601) {
				return df8601.format((Date) value);
			}
		} else if (field.getType().isEnum()) {
			return value.toString();
		} else {
			throw new IcatException(IcatExceptionType.INTERNAL, "Don't know how to export field of type " + type);
		}
	}

	private Session getSession(String sessionId) throws IcatException {
		Session session = null;
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session Id cannot be null or empty.");
		}
		session = (Session) entityManager.find(Session.class, sessionId);
		if (session == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Unable to find user by sessionid: " + sessionId);
		}
		return session;
	}

	public String getUserName(String sessionId) throws IcatException {
		try {
			Session session = getSession(sessionId);
			String userName = session.getUserName();
			logger.debug("user: " + userName + " is associated with: " + sessionId);
			return userName;
		} catch (IcatException e) {
			logger.debug("sessionId " + sessionId + " is not associated with valid session " + e.getMessage());
			throw e;
		}
	}

	private Object getValue(Map<Field, Method> getters, Field f, EntityBaseBean bean) throws IcatException {
		Object value;
		try {
			value = getters.get(f).invoke(bean);
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
		if (value instanceof EntityBaseBean) {
			value = "id:" + ((EntityBaseBean) value).getId();
		}
		return value;
	}

	@PostConstruct
	void init() {
		logRequests = propertyHandler.getLogSet();
		log = !logRequests.isEmpty();
		notificationRequests = propertyHandler.getNotificationRequests();
		searchActive = searchManager.isActive();
		searchMaxSearchTimeMillis = propertyHandler.getSearchMaxSearchTimeMillis();
		maxEntities = propertyHandler.getMaxEntities();
		searchSearchBlockSize = propertyHandler.getSearchSearchBlockSize();
		exportCacheSize = propertyHandler.getImportCacheSize();
		rootUserNames = propertyHandler.getRootUserNames();
		key = propertyHandler.getKey();
	}

	public boolean isAccessAllowed(String userId, EntityBaseBean bean, AccessType accessType) throws IcatException {
		if (accessType == AccessType.CREATE) {
			return createAllowed(userId, bean);
		} else {
			try {
				gateKeeper.performAuthorisation(userId, bean, accessType);
				return true;
			} catch (IcatException e) {
				if (e.getType() != IcatExceptionType.INSUFFICIENT_PRIVILEGES) {
					throw e;
				}
				return false;
			}
		}
	}

	public boolean isLoggedIn(String userName) {
		logger.debug("isLoggedIn for user " + userName);
		return entityManager.createNamedQuery(Session.ISLOGGEDIN, Long.class).setParameter("userName", userName).getSingleResult() > 0;
	}

	private void isUnique(EntityBaseBean bean) throws IcatException {
		logger.trace("Check uniqueness of {}", bean);
		EntityBaseBean other = lookup(bean);

		if (other != null) {
			Class<? extends EntityBaseBean> entityClass = bean.getClass();
			Map<Field, Method> getters = EntityInfoHandler.getGetters(entityClass);
			List<Field> constraint = EntityInfoHandler.getConstraintFields(entityClass);

			StringBuilder erm = new StringBuilder();
			for (Field f : constraint) {
				Object value;
				try {
					value = getters.get(f).invoke(bean);
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							e.getClass() + " " + e.getMessage());
				}
				if (erm.length() == 0) {
					erm.append(entityClass.getSimpleName() + " exists with ");
				} else {
					erm.append(", ");
				}
				erm.append(f.getName() + " = '" + value + "'");
			}
			throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS, erm.toString());
		}
	}

	private void isValid(EntityBaseBean bean) throws IcatException {
		logger.trace("Checking validity of {}", bean);
		Class<? extends EntityBaseBean> klass = bean.getClass();
		List<Field> notNullFields = EntityInfoHandler.getNotNullableFields(klass);
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);

		for (Field field : notNullFields) {

			Object value;
			try {
				Method method = getters.get(field);
				value = method.invoke(bean, (Object[]) new Class[] {});
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}

			if (value == null) {
				throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
						klass.getSimpleName() + ": " + field.getName() + " cannot be null.");
			}
		}

		Map<Field, Integer> stringFields = EntityInfoHandler.getStringFields(klass);
		for (Entry<Field, Integer> entry : stringFields.entrySet()) {
			Field field = entry.getKey();
			Integer length = entry.getValue();
			Method method = getters.get(field);
			Object value;
			try {
				value = method.invoke(bean, (Object[]) new Class[] {});
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}
			if (value != null) {
				if (((String) value).length() > length) {
					throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
							klass.getSimpleName() + ": " + field.getName() + " cannot have length > " + length);
				}
			}
		}

	}

	public String login(String userName, int lifetimeMinutes, String ip) throws IcatException {
		Session session = new Session(userName, lifetimeMinutes);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				entityManager.persist(session);
				entityManager.flush();
				userTransaction.commit();
				String result = session.getId();
				logger.debug("Session " + result + " persisted.");
				if (logRequests.contains(CallType.SESSION)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", userName);
						gen.writeEnd();
					}
					transmitter.processMessage("login", ip, baos.toString(), startMillis);
				}
				return result;
			} catch (Throwable e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for login because of " + e.getClass() + " " + e.getMessage());
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"IllegalStateException " + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException " + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException " + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"NotSupportedException " + e.getMessage());
		}
	}

	public void logout(String sessionId, String ip) throws IcatException {
		logger.debug("logout for sessionId " + sessionId);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				Session session = getSession(sessionId);
				entityManager.remove(session);
				entityManager.flush();
				userTransaction.commit();
				logger.debug("Session {} removed.", session.getId());
				if (logRequests.contains(CallType.SESSION)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", session.getUserName());
						gen.writeEnd();
					}
					transmitter.processMessage("logout", ip, baos.toString(), startMillis);
				}
			} catch (IcatException e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for logout because of " + e.getClass() + " " + e.getMessage());
				if (e.getType() == IcatExceptionType.SESSION) {
					throw e;
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							e.getClass() + " " + e.getMessage());
				}
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException" + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException" + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "NotSupportedException" + e.getMessage());
		} catch (RuntimeException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public EntityBaseBean lookup(EntityBaseBean bean) throws IcatException {
		Class<? extends EntityBaseBean> entityClass = bean.getClass();

		Map<Field, Method> getters = EntityInfoHandler.getGetters(entityClass);
		List<Field> constraint = EntityInfoHandler.getConstraintFields(entityClass);
		if (constraint.isEmpty()) {
			return null;
		}
		StringBuilder queryString = new StringBuilder();
		for (Field f : constraint) {
			if (queryString.length() == 0) {
				queryString.append("SELECT o FROM " + entityClass.getSimpleName() + " o WHERE (");
			} else {
				queryString.append(") AND (");
			}
			String name = f.getName();
			queryString.append("o." + name + " = :" + name);
		}
		TypedQuery<EntityBaseBean> query = entityManager.createQuery(queryString.toString() + ")", EntityBaseBean.class);
		for (Field f : constraint) {
			Object value;
			try {
				value = getters.get(f).invoke(bean);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
			query = query.setParameter(f.getName(), value);
		}
		logger.debug("Looking up with " + queryString + ")");
		List<EntityBaseBean> results = query.getResultList();
		if (results.isEmpty()) {
			return null;
		} else if (results.size() != 1) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"lookup found more than one " + bean + "with same key");
		}
		return results.get(0);
	}

	public void searchClear() throws IcatException {
		if (searchActive) {
			searchManager.clear();
		}
	}

	public void searchCommit() throws IcatException {
		if (searchActive) {
			searchManager.commit();
		}
	}

	/**
	 * Performs a search on a single entity, and authorises the results before
	 * returning. Does not support sorting or searchAfter.
	 * 
	 * @param userName User performing the search, used for authorisation.
	 * @param jo       JsonObject containing the details of the query to be used.
	 * @param maxCount The maximum number of results to collect before returning. If
	 *                 a batch from the search engine has more than this many
	 *                 authorised results, then the excess results will be
	 *                 discarded.
	 * @param ip       Used for logging only.
	 * @param klass    Class of the entity to search.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public List<ScoredEntityBaseBean> freeTextSearch(String userName, JsonObject jo, int maxCount,
			String ip, Class<? extends EntityBaseBean> klass) throws IcatException {
		long startMillis = System.currentTimeMillis();
		List<ScoredEntityBaseBean> results = new ArrayList<>();
		if (searchActive) {
			searchDocuments(userName, jo, null, maxCount, maxCount, null, klass, startMillis, results, Arrays.asList("id"));
		}
		logSearch(userName, ip, startMillis, results, "freeTextSearch");
		return results;
	}

	/**
	 * Performs a search on a single entity, and authorises the results before
	 * returning.
	 * 
	 * @param userName    User performing the search, used for authorisation.
	 * @param jo          JsonObject containing the details of the query to be used.
	 * @param searchAfter JsonValue representation of the final result from a
	 *                    previous search.
	 * @param minCount    The minimum number of results to collect before returning.
	 *                    If a batch from the search engine has at least this many
	 *                    authorised results, no further batches will be requested.
	 * @param maxCount    The maximum number of results to collect before returning.
	 *                    If a batch from the search engine has more than this many
	 *                    authorised results, then the excess results will be
	 *                    discarded.
	 * @param sort        String of Json representing sort criteria.
	 * @param ip          Used for logging only.
	 * @param klass       Class of the entity to search.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public SearchResult freeTextSearchDocs(String userName, JsonObject jo, JsonValue searchAfter, int minCount,
			int maxCount, String sort, String ip, Class<? extends EntityBaseBean> klass) throws IcatException {
		long startMillis = System.currentTimeMillis();
		JsonValue lastSearchAfter = null;
		List<ScoredEntityBaseBean> results = new ArrayList<>();
		List<FacetDimension> dimensions = new ArrayList<>();
		if (searchActive) {
			List<String> fields = searchManager.getPublicSearchFields(gateKeeper, klass.getSimpleName());
			lastSearchAfter = searchDocuments(userName, jo, searchAfter, maxCount, minCount, sort, klass, startMillis, results, fields);

			if (jo.containsKey("facets")) {
				List<JsonObject> jsonFacets = jo.getJsonArray("facets").getValuesAs(JsonObject.class);
				for (JsonObject jsonFacet : jsonFacets) {
					String target = jsonFacet.getString("target");
					JsonObject facetQuery = buildFacetQuery(klass, target, results, jsonFacet);
					if (facetQuery != null) {
						dimensions.addAll(searchManager.facetSearch(target, facetQuery, results.size(), 10));
					}
				}
			}
		}
		logSearch(userName, ip, startMillis, results, "freeTextSearchDocs");
		return new SearchResult(lastSearchAfter, results, dimensions);
	}

	/**
	 * Performs logging dependent on the value of logRequests.
	 * 
	 * @param userName    User performing the search
	 * @param ip          Used for logging only
	 * @param startMillis The start time of the search in milliseconds
	 * @param results     List of authorised search results
	 * @param operation   Name of the calling function
	 */
	private void logSearch(String userName, String ip, long startMillis, List<ScoredEntityBaseBean> results,
			String operation) {
		if (logRequests.contains("R")) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				if (results.size() > 0) {
					gen.write("entityId", results.get(0).getId());
				}
				gen.writeEnd();
			}
			transmitter.processMessage(operation, ip, baos.toString(), startMillis);
		}
		logger.debug("Returning {} results", results.size());
	}

	/**
	 * Performs batches of searches, the results of which are authorised. Results
	 * are collected until they run out, minCount is reached, or too much time
	 * elapses.
	 * 
	 * @param userName    User performing the search, used for authorisation.
	 * @param jo          JsonObject containing the details of the query to be used.
	 * @param searchAfter JsonValue representation of the final result from a
	 *                    previous search.
	 * @param minCount    The minimum number of results to collect before returning.
	 *                    If a batch from the search engine has at least this many
	 *                    authorised results, no further batches will be requested.
	 * @param maxCount    The maximum number of results to collect before returning.
	 *                    If a batch from the search engine has more than this many
	 *                    authorised results, then the excess results will be
	 *                    discarded.
	 * @param sort        String of Json representing sort criteria.
	 * @param klass       Class of the entity to search.
	 * @param startMillis The start time of the search in milliseconds
	 * @param results     List of results from the search. Authorised results will
	 *                    be appended to this List.
	 * @param fields      Fields to include in the returned Documents.
	 * @return JsonValue representing the last result of the search, formatted to
	 *         allow future searches to "search after" this result. May be null.
	 * @throws IcatException If the search exceeds the maximum allowed time.
	 */
	private JsonValue searchDocuments(String userName, JsonObject jo, JsonValue searchAfter, int maxCount, int minCount,
			String sort, Class<? extends EntityBaseBean> klass, long startMillis, List<ScoredEntityBaseBean> results,
			List<String> fields) throws IcatException {
		JsonValue lastSearchAfter;
		try {
			do {
				SearchResult lastSearchResult = searchManager.freeTextSearch(jo, searchAfter, searchSearchBlockSize,
						sort, fields);
				List<ScoredEntityBaseBean> allResults = lastSearchResult.getResults();
				ScoredEntityBaseBean lastBean = filterReadAccess(results, allResults, maxCount, userName, klass);
				if (lastBean == null) {
					// Haven't stopped early, so use the Lucene provided searchAfter document
					lastSearchAfter = lastSearchResult.getSearchAfter();
					if (lastSearchAfter == null) {
						return null; // If searchAfter is null, we ran out of results so stop here
					}
					searchAfter = lastSearchAfter;
				} else {
					// Have stopped early by reaching the limit, so build a searchAfter document
					return searchManager.buildSearchAfter(lastBean, sort);
				}
				if (System.currentTimeMillis() - startMillis > searchMaxSearchTimeMillis) {
					long maxTimeSeconds = searchMaxSearchTimeMillis / 1000;
					String msg = "Search cancelled for exceeding " + maxTimeSeconds + " seconds";
					logger.warn(msg + ": user={} query={}", userName, jo);
					throw new IcatException(IcatExceptionType.INTERNAL, msg);
				}
			} while (results.size() < minCount);
		} catch (IcatException e) {
			String message = e.getMessage();
			if (message != null && message.startsWith("Search cancelled for exceeding")) {
				logger.warn(message + ": user={} query={}", userName, jo);
			}
			throw e;
		}
		return lastSearchAfter;
	}

	/**
	 * Perform faceting on entities of klass using the criteria contained in jo.
	 * 
	 * @param jo    JsonObject containing "facets" key with a value of a JsonArray
	 *              of JsonObjects.
	 * @param klass Class of the entity to facet.
	 * @return SearchResult with only the dimensions set.
	 * @throws IcatException
	 */
	public SearchResult facetDocs(JsonObject jo, Class<? extends EntityBaseBean> klass) throws IcatException {
		List<FacetDimension> dimensions = new ArrayList<>();
		if (searchActive && jo.containsKey("facets")) {
			List<JsonObject> jsonFacets = jo.getJsonArray("facets").getValuesAs(JsonObject.class);
			for (JsonObject jsonFacet : jsonFacets) {
				String target = jsonFacet.getString("target");
				JsonObject filterObject = jo.getJsonObject("filter");
				JsonObject facetQuery = buildFacetQuery(klass, target, filterObject, jsonFacet);
				if (facetQuery != null) {
					dimensions.addAll(searchManager.facetSearch(target, facetQuery, 1000, 10));
				}
			}
		}
		return new SearchResult(dimensions);
	}

	/**
	 * Formats Json for requesting faceting. Performs the logic needed to ensure
	 * that we do not facet on a field that should not be visible.
	 * 
	 * @param klass        Class of the entity to facet.
	 * @param target       The entity which directly posses the dimensions of
	 *                     interest. Note this may be different than the klass, for
	 *                     example if klass is Investigation then target might be
	 *                     InvestigationParameter.
	 * @param filterObject JsonObject to be used as the query.
	 * @param jsonFacet    JsonObject containing the dimensions to facet.
	 * @return JsonObject with the format
	 *         <code>{"query": `filterObject`, "dimensions": [...]}</code>
	 * @throws IcatException
	 */
	private JsonObject buildFacetQuery(Class<? extends EntityBaseBean> klass, String target, JsonObject filterObject,
			JsonObject jsonFacet) throws IcatException {
		if (target.equals(klass.getSimpleName())) {
			return SearchManager.buildFacetQuery(filterObject, jsonFacet);
		} else {
			Relationship relationship;
			if (target.equals("SampleParameter")) {
				Relationship sampleRelationship;
				if (klass.getSimpleName().equals("Investigation")) {
					sampleRelationship = EntityInfoHandler.getRelationshipsByName(klass).get("samples");
				} else {
					if (klass.getSimpleName().equals("Datafile")) {
						Relationship datasetRelationship = EntityInfoHandler.getRelationshipsByName(klass).get("dataset");
						if (!gateKeeper.allowed(datasetRelationship)) {
							return null;
						}
					}
					sampleRelationship = EntityInfoHandler.getRelationshipsByName(Dataset.class).get("sample");
				}
				Relationship parameterRelationship = EntityInfoHandler.getRelationshipsByName(Sample.class).get("parameters");
				if (!gateKeeper.allowed(sampleRelationship) || !gateKeeper.allowed(parameterRelationship)) {
					return null;
				}
				return SearchManager.buildFacetQuery(filterObject, jsonFacet);
			} else if (target.contains("Parameter")) {
				relationship = EntityInfoHandler.getRelationshipsByName(klass).get("parameters");
			} else if (target.contains("DatasetTechnique")) {
				relationship = EntityInfoHandler.getRelationshipsByName(klass).get("datasetTechniques");
			} else {
				relationship = EntityInfoHandler.getRelationshipsByName(klass).get(target.toLowerCase() + "s");
			}

			if (gateKeeper.allowed(relationship)) {
				return SearchManager.buildFacetQuery(filterObject, jsonFacet);
			} else {
				logger.debug("Cannot collect facets for {} as Relationship with parent {} is not allowed",
						target, klass.getSimpleName());
				return null;
			}
		}
	}

	/**
	 * Formats Json for requesting faceting. Performs the logic needed to ensure
	 * that we do not facet on a field that should not be visible.
	 * 
	 * @param klass     Class of the entity to facet.
	 * @param target    The entity which directly posses the dimensions of interest.
	 *                  Note this may be different than the klass, for example if
	 *                  klass is Investigation then target might be
	 *                  InvestigationParameter.
	 * @param results   List of results from a previous search, containing entity
	 *                  ids.
	 * @param jsonFacet JsonObject containing the dimensions to facet.
	 * @return <code>{"query": {`idField`: [...]}, "dimensions": [...]}</code>
	 * @throws IcatException
	 */
	private JsonObject buildFacetQuery(Class<? extends EntityBaseBean> klass, String target,
			List<ScoredEntityBaseBean> results, JsonObject jsonFacet) throws IcatException {
		String parentName = klass.getSimpleName();
		if (target.equals(parentName)) {
			return SearchManager.buildFacetQuery(results, "id", jsonFacet);
		} else {
			Relationship relationship;
			if (target.equals("SampleParameter")) {
				Relationship sampleRelationship;
				if (parentName.equals("Investigation")) {
					sampleRelationship = EntityInfoHandler.getRelationshipsByName(klass).get("samples");
				} else {
					if (parentName.equals("Datafile")) {
						Relationship datasetRelationship = EntityInfoHandler.getRelationshipsByName(klass).get("dataset");
						if (!gateKeeper.allowed(datasetRelationship)) {
							logger.debug("Cannot collect facets for {} as Relationship with parent {} is not allowed", target,
								parentName);
							return null;
						}
					}
					sampleRelationship = EntityInfoHandler.getRelationshipsByName(Dataset.class).get("sample");
				}
				Relationship parameterRelationship = EntityInfoHandler.getRelationshipsByName(Sample.class).get("parameters");
				if (!gateKeeper.allowed(sampleRelationship) || !gateKeeper.allowed(parameterRelationship)) {
					logger.debug("Cannot collect facets for {} as Relationship with parent {} is not allowed", target,
						parentName);
					return null;
				}
				return SearchManager.buildFacetQuery(results, "sample.id", "sample.id", jsonFacet);
			} else if (target.equals("InvestigationInstrument")) {
				List<Relationship> relationships = new ArrayList<>();
				String resultIdField = "id";
				if (parentName.equals("Datafile")) {
					resultIdField = "investigation.id";
					relationships.add(EntityInfoHandler.getRelationshipsByName(Datafile.class).get("dataset"));
					relationships.add(EntityInfoHandler.getRelationshipsByName(Dataset.class).get("investigation"));
				} else if (parentName.equals("Dataset")) {
					resultIdField = "investigation.id";
					relationships.add(EntityInfoHandler.getRelationshipsByName(Dataset.class).get("investigation"));
				}
				relationships.add(EntityInfoHandler.getRelationshipsByName(Investigation.class).get("investigationInstruments"));
				relationships.add(EntityInfoHandler.getRelationshipsByName(InvestigationInstrument.class).get("instrument"));
				for (Relationship r : relationships) {
					if (!gateKeeper.allowed(r)) {
						logger.debug("Cannot collect facets for {} as Relationship with parent {} is not allowed", target,
							parentName);
						return null;
					}
				}
				return SearchManager.buildFacetQuery(results, resultIdField, "investigation.id", jsonFacet);
			} else if (target.contains("Parameter")) {
				relationship = EntityInfoHandler.getRelationshipsByName(klass).get("parameters");
			} else {
				relationship = EntityInfoHandler.getRelationshipsByName(klass).get(target.toLowerCase() + "s");
			}

			if (gateKeeper.allowed(relationship)) {
				if (target.equals("Sample") && parentName.equals("Investigation")) {
					// As samples can be one to many on Investigations or one to one on Datasets, they do not follow
					// usual naming conventions in the document mapping
					return SearchManager.buildFacetQuery(results, "sample.investigation.id", jsonFacet);
				}
				return SearchManager.buildFacetQuery(results, parentName.toLowerCase() + ".id", jsonFacet);
			} else {
				logger.debug("Cannot collect facets for {} as Relationship with parent {} is not allowed",
						target, parentName);
				return null;
			}
		}
	}

	public List<String> searchGetPopulating() {
		if (searchActive) {
			return searchManager.getPopulating();
		} else {
			return Collections.emptyList();
		}
	}

	public void searchPopulate(String entityName, Long minId, Long maxId, boolean delete) throws IcatException {
		if (searchActive) {
			// Throws IcatException if entityName is not an ICAT entity
			EntityInfoHandler.getClass(entityName);

			searchManager.populate(entityName, minId, maxId, delete);
		}
	}

	// This code might be in EntityBaseBean however this would mean that it
	// would be processed by JPA which gets confused by it.
	private void merge(EntityBaseBean thisBean, Object fromBean) throws IcatException {
		Class<? extends EntityBaseBean> klass = thisBean.getClass();
		Map<Field, Method> setters = EntityInfoHandler.getSettersForUpdate(klass);
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);

		for (Entry<Field, Method> fieldAndMethod : setters.entrySet()) {
			Field field = fieldAndMethod.getKey();
			try {
				Method m = getters.get(field);
				Object value = m.invoke(fromBean, new Object[0]);
				if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
					logger.debug("Needs special processing as " + value + " is a bean");
					if (value != null) {
						Object pk = ((EntityBaseBean) value).getId();
						value = (EntityBaseBean) entityManager.find(field.getType(), pk);
						fieldAndMethod.getValue().invoke(thisBean, value);
					} else {
						fieldAndMethod.getValue().invoke(thisBean, (EntityBaseBean) null);
					}
				} else {
					fieldAndMethod.getValue().invoke(thisBean, value);
				}
				logger.trace("Updated " + klass.getSimpleName() + "." + field.getName() + " to " + value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}
		}
	}

	private void parseEntity(EntityBaseBean bean, JsonObject contents, Class<? extends EntityBaseBean> klass,
			List<EntityBaseBean> creates, Map<EntityBaseBean, Boolean> localUpdates, boolean create, String userId)
			throws IcatException {
		Map<String, Field> fieldsByName = EntityInfoHandler.getFieldsByName(klass);
		Set<Field> updaters = EntityInfoHandler.getSettersForUpdate(klass).keySet();
		Map<Field, Method> setters = EntityInfoHandler.getSetters(klass);
		Map<String, Relationship> rels = EntityInfoHandler.getRelationshipsByName(klass);
		Map<String, Method> getters = EntityInfoHandler.getGettersFromName(klass);
		Set<Field> relInKey = EntityInfoHandler.getRelInKey(klass);

		boolean deleteAllowed = false;
		if (!create) {
			gateKeeper.performUpdateAuthorisation(userId, bean, contents);

			/*
			 * See if delete is allowed - it may not be relevant but need to
			 * check now before modifications are made
			 */
			deleteAllowed = gateKeeper.isAccessAllowed(userId, bean, AccessType.DELETE);
		}

		boolean changedIdentity = false;

		for (Entry<String, JsonValue> fentry : contents.entrySet()) {
			String fName = fentry.getKey();
			if (!fName.equals("id")) {
				JsonValue fValue = fentry.getValue();
				logger.trace("Setting {}.{} to {}", klass.getSimpleName(), fName, fValue);
				Field field = fieldsByName.get(fName);
				if (field == null) {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Field " + fName + " not found in " + klass.getSimpleName());
				} else if (updaters.contains(field)) {
					String type = field.getType().getSimpleName();
					Object arg = null;
					if (fValue.getValueType() == ValueType.NULL) {
						// Do nothing
					} else if (type.equals("String")) {
						arg = ((JsonString) fValue).getString();
					} else if (type.equals("Integer")) {
						arg = ((JsonNumber) fValue).intValueExact();
					} else if (type.equals("Double")) {
						arg = ((JsonNumber) fValue).doubleValue();
					} else if (type.equals("Long")) {
						arg = ((JsonNumber) fValue).longValueExact();
					} else if (type.equals("boolean")) {
						if (fValue.getValueType() == ValueType.TRUE) {
							arg = true;
						} else if (fValue.getValueType() == ValueType.FALSE) {
							arg = false;
						} else {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									"Field " + fName + " must be true or false in " + klass.getSimpleName());
						}
					} else if (type.equals("ParameterValueType")) {
						try {
							arg = ParameterValueType.valueOf(((JsonString) fValue).getString().toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Field " + fName + " must be in "
									+ Arrays.asList(ParameterValueType.values()) + " for " + klass.getSimpleName());
						}
					} else if (type.equals("Date")) {
						synchronized (df8601) {
							try {
								arg = df8601.parse(((JsonString) fValue).getString());
							} catch (ParseException | ClassCastException e) {
								throw new IcatException(IcatExceptionType.BAD_PARAMETER,
										"Badly formatted date " + fValue);
							}
						}
					} else {
						try {
							arg = parseSubEntity((JsonObject) fValue, rels.get(fName), creates, localUpdates, userId);
							/* This may be an illegal update */
							if (relInKey.contains(field)) {
								if (bean.getId() != ((EntityBaseBean) arg).getId()) {
									logger.debug("Identity relationship field " + field.getName() + " of " + bean
											+ " is being changed from " + ((EntityBaseBean) arg).getId() + " to "
											+ bean.getId());
									changedIdentity = true;
								}
							}
						} catch (ClassCastException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									"Badly formatted relationship object " + fValue);
						}
					}
					try {
						setters.get(field).invoke(bean, arg);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new IcatException(IcatExceptionType.INTERNAL,
								"failed to set field " + fName + " of " + klass.getSimpleName());
					}
				} else {
					logger.debug("Need to process field {} with rel {} to {}", fName, rels.get(fName), fValue);
					try {
						@SuppressWarnings("unchecked")
						List<EntityBaseBean> beans = (List<EntityBaseBean>) getters.get(fName).invoke(bean);
						for (JsonValue aValue : (JsonArray) fValue) {
							EntityBaseBean arg = parseSubEntity((JsonObject) aValue, rels.get(fName), creates, localUpdates, userId);
							beans.add(arg);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

					}
				}
			}

		}

		if (create) {
			creates.add(bean);
		} else {
			if (changedIdentity && !deleteAllowed) {
				throw new IcatException(IcatException.IcatExceptionType.INSUFFICIENT_PRIVILEGES,
						"DELETE access implied by UPDATE to this " + klass.getSimpleName() + " is not allowed.");
			}
			// Will need to check after commit that CREATE is permitted if the
			// identity has changed
			localUpdates.put(bean, changedIdentity);
		}

	}

	private EntityBaseBean parseSubEntity(JsonObject contents, Relationship relationship, List<EntityBaseBean> creates,
			Map<EntityBaseBean, Boolean> localUpdates, String userId) throws IcatException {
		logger.debug("Parse entity {} from relationship {}", contents, relationship);
		Class<? extends EntityBaseBean> klass = relationship.getDestinationBean();

		EntityBaseBean bean = null;
		try {
			bean = klass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, "failed to instantiate " + klass.getSimpleName());
		}

		boolean create = !contents.containsKey("id");
		if (create) {
			if (!relationship.isCollection()) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"One to many related objects should not have the id value set: " + contents);
			}
		} else {
			if (relationship.isCollection()) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"Many to one related objects should have the id value set: " + contents);
			}
			bean.setId(contents.getJsonNumber("id").longValueExact());
			bean = find(bean);
		}

		parseEntity(bean, contents, klass, creates, localUpdates, create, userId);
		return bean;

	}

	public void refresh(String sessionId, int lifetimeMinutes, String ip) throws IcatException {
		logger.debug("logout for sessionId " + sessionId);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				Session session = getSession(sessionId);
				session.refresh(lifetimeMinutes);
				entityManager.flush();
				userTransaction.commit();
				logger.debug("Session {} refreshed.", session.getId());
				if (logRequests.contains(CallType.SESSION)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", session.getUserName());
						gen.writeEnd();
					}
					transmitter.processMessage("refresh", ip, baos.toString(), startMillis);
				}
			} catch (IcatException e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for logout because of " + e.getClass() + " " + e.getMessage());
				if (e.getType() == IcatExceptionType.SESSION) {
					throw e;
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							e.getClass() + " " + e.getMessage());
				}
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IllegalStateException" + e.getMessage());
		} catch (SecurityException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SecurityException" + e.getMessage());
		} catch (SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "SystemException" + e.getMessage());
		} catch (NotSupportedException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "NotSupportedException" + e.getMessage());
		}
	}

	public List<?> search(String userId, String query, String ip) throws IcatException {

		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.info(userId + " searching for " + query);

		EntitySetResult esr = getEntitySet(userId, query);
		List<?> result = esr.result;

		if (result.size() > 0 && (result.get(0) == null || result.get(0) instanceof EntityBaseBean)) {
			IncludeClause include = esr.searchQuery.getIncludeClause();
			boolean one = false;
			List<Step> steps = null;
			if (include != null) {
				one = include.isOne();
				steps = include.getSteps();
			}
			List<Object> clones = new ArrayList<Object>();
			long descendantCount = 0;
			for (Object beanManaged : result) {
				if (beanManaged == null) {
					if ((descendantCount += 1) > maxEntities) {
						throw new IcatException(IcatExceptionType.VALIDATION,
								"attempt to return more than " + maxEntities + " entities");
					}
					clones.add(null);
				} else {
					EntityBaseBean eb = ((EntityBaseBean) beanManaged).pruned(one, 0, steps, maxEntities, gateKeeper, userId);
					if ((descendantCount += eb.getDescendantCount(maxEntities)) > maxEntities) {
						throw new IcatException(IcatExceptionType.VALIDATION,
								"attempt to return more than " + maxEntities + " entities");
					}
					clones.add(eb);
				}
			}
			logger.debug("Obtained " + descendantCount + " entities.");

			if (logRequests.contains(CallType.READ)) {
				EntityBaseBean bean = (EntityBaseBean) clones.get(0);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
					gen.write("userName", userId);
					if (bean != null) {
						gen.write("entityName", bean.getClass().getSimpleName());
						gen.write("entityId", bean.getId());
					}
					gen.write("query", query);
					gen.writeEnd();
				}
				transmitter.processMessage("search", ip, baos.toString(), startMillis);
			}
			logger.debug("Clones {}", clones);
			return clones;
		} else {
			if (logRequests.contains(CallType.READ)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
					gen.write("userName", userId);
					gen.write("query", query);
					gen.writeEnd();
				}
				transmitter.processMessage("search", ip, baos.toString(), startMillis);
			}
			logger.debug("Result {}", result);
			return result;
		}
	}

	public NotificationMessage update(String userId, EntityBaseBean bean, boolean allAttributes, String ip) throws IcatException {
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				EntityBaseBean beanManaged = find(bean);
				gateKeeper.performAuthorisation(userId, beanManaged, AccessType.UPDATE);
				boolean identityChange = checkIdentityChange(beanManaged, bean);
				if (identityChange) {
					gateKeeper.performAuthorisation(userId, beanManaged, AccessType.DELETE);
				}

				if (allAttributes) {
					if (bean.getCreateId() != null) {
						beanManaged.setCreateId(bean.getCreateId());
					} else {
						beanManaged.setCreateId(userId);
					}
					if (bean.getModId() != null) {
						beanManaged.setModId(bean.getModId());
					} else {
						beanManaged.setModId(userId);
					}
					Date now = null;
					if (bean.getCreateTime() != null) {
						beanManaged.setCreateTime(bean.getCreateTime());
					} else {
						now = new Date();
						beanManaged.setCreateTime(now);
					}
					if (bean.getModTime() != null) {
						beanManaged.setModTime(bean.getModTime());
					} else {
						if (now == null) {
							now = new Date();
						}
						beanManaged.setModTime(now);
					}
				} else {
					beanManaged.setModId(userId);
					beanManaged.setModTime(new Date());
				}
				merge(beanManaged, bean);
				if (identityChange) {
					gateKeeper.performAuthorisation(userId, beanManaged, AccessType.CREATE);
				}
				beanManaged.postMergeFixup(entityManager);
				entityManager.flush();
				logger.trace("Updated bean " + bean + " flushed.");
				NotificationMessage notification = new NotificationMessage(Operation.U, bean, notificationRequests);
				userTransaction.commit();
				if (logRequests.contains(CallType.WRITE)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
						gen.write("userName", userId);
						gen.write("entityName", bean.getClass().getSimpleName());
						gen.write("entityId", bean.getId());
						gen.writeEnd();
					}
					transmitter.processMessage("update", ip, baos.toString(), startMillis);
				}
				if (searchActive) {
					searchManager.updateDocument(entityManager, beanManaged);
				}
				return notification;
			} catch (IcatException e) {
				userTransaction.rollback();
				throw e;
			} catch (Throwable e) {
				userTransaction.rollback();
				updateCache();
				EntityBaseBean beanManaged = find(bean);
				beanManaged.setModId(userId);
				merge(beanManaged, bean);
				beanManaged.postMergeFixup(entityManager);
				isValid(beanManaged);
				logger.error("Internal error", e);
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException | SecurityException | SystemException | NotSupportedException e) {
			logger.error("Internal error", e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private void updateCache() throws IcatException {
		try {
			gateKeeper.updateCache();
		} catch (JMSException e) {
			logger.error("Internal error", e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public List<Long> write(String userId, String json, String ip) throws IcatException {
		logger.info("write called with {}", json);

		if (json == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "entities is not set");
		}

		List<Long> beanIds = new ArrayList<>();
		List<EntityBaseBean> creates = new ArrayList<>();
		List<EntityBaseBean> updates = new ArrayList<>();

		try {
			int offset = 0;
			userTransaction.begin();
			try (JsonReader reader = Json.createReader(new ByteArrayInputStream(json.getBytes()))) {
				long startMillis = log ? System.currentTimeMillis() : 0;
				JsonStructure top = reader.read();

				if (top.getValueType() == ValueType.ARRAY) {

					for (JsonValue obj : (JsonArray) top) {
						EntityBaseBean bean = writeOne((JsonObject) obj, userId, creates, updates);
						if (bean != null) {
							beanIds.add(bean.getId());
						}
						offset++;
					}
				} else {
					EntityBaseBean bean = writeOne((JsonObject) top, userId, creates, updates);
					if (bean != null) {
						beanIds.add(bean.getId());
					}
				}
				userTransaction.commit();

				/*
				 * Nothing should be able to go wrong now so log, update
				 * and send notification messages
				 */
				if (logRequests.contains(CallType.WRITE)) {

					EntityBaseBean bean = null;
					if (!creates.isEmpty()) {
						bean = creates.get(0);
					} else if (!updates.isEmpty()) {
						bean = updates.get(0);
					}

					if (bean != null) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();

						try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
							gen.write("userName", userId);
							gen.write("entityName", bean.getClass().getSimpleName());
							gen.write("entityId", bean.getId());
							gen.writeEnd();
						}
						transmitter.processMessage("write", ip, baos.toString(), startMillis);

					}
				}

				if (searchActive) {
					for (EntityBaseBean eb : creates) {
						searchManager.addDocument(entityManager, eb);
					}
					for (EntityBaseBean eb : updates) {
						searchManager.updateDocument(entityManager, eb);
					}
				}

				try {
					for (EntityBaseBean eb : creates) {
						notificationTransmitter.processMessage(new NotificationMessage(Operation.C, eb, notificationRequests));
					}

					for (EntityBaseBean eb : updates) {
						notificationTransmitter.processMessage(new NotificationMessage(Operation.U, eb, notificationRequests));
					}
				} catch (JMSException e) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"Operation completed but unable to send JMS message " + e.getMessage());
				}

				return beanIds;
			} catch (JsonException e) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getMessage() + " in json " + json, offset);
			} catch (IcatException e) {
				e.setOffset(offset);
				throw e;
			}
		} catch (IllegalStateException | SecurityException | SystemException | NotSupportedException | RollbackException
				| HeuristicMixedException | HeuristicRollbackException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		} catch (IcatException e) {
			try {
				userTransaction.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				// Ignore it
			}
			throw e;
		}
	}

	private EntityBaseBean writeOne(JsonObject entity, String userId, List<EntityBaseBean> creates,
			List<EntityBaseBean> updates) throws IcatException {
		logger.debug("write one {}", entity);

		if (entity.size() != 1) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"Entity must have one keyword followed by its values in json " + entity);
		}

		Entry<String, JsonValue> entry = entity.entrySet().iterator().next();
		String beanName = entry.getKey();
		Class<? extends EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		JsonValue value = entry.getValue();
		if (value.getValueType() != ValueType.OBJECT) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"Unexpected array found in JSON " + value.toString());
		}
		JsonObject contents = (JsonObject) value;

		EntityBaseBean bean = null;
		try {
			bean = klass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, "failed to instantiate " + beanName);
		}
		boolean create = !contents.containsKey("id");
		if (!create) {
			try {
				bean.setId(contents.getJsonNumber("id").longValueExact());
			} catch (ClassCastException e) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"Badly formatted id: " + contents.getString("id"));
			}
			bean = find(bean);
		}
		List<EntityBaseBean> localCreates = new ArrayList<>();
		Map<EntityBaseBean, Boolean> localUpdates = new HashMap<>();
		parseEntity(bean, contents, klass, localCreates, localUpdates, create, userId);

		for (EntityBaseBean b : localUpdates.keySet()) {
			b.setModId(userId);
			b.setModTime(new Date());
		}

		try {
			bean.preparePersist(userId, entityManager, PersistMode.REST);
			if (create) {
				entityManager.persist(bean);
				logger.trace(bean + " persisted.");
			}
			for (EntityBaseBean b : localUpdates.keySet()) {
				b.postMergeFixup(entityManager);
			}
			entityManager.flush();
			logger.trace(bean + " flushed.");
		} catch (Throwable e) {
			/*
			 * Clear transaction so can use database again.
			 */
			logger.debug("Problem shows up with persist/flush will rollback and check: {} {}", e.getClass(),
					e.getMessage());
			try {
				userTransaction.rollback();
				bean.setId(null);
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						e1.getClass() + " " + e1.getMessage());
			}
			for (EntityBaseBean b : localCreates) {
				isValid(b);
				isUnique(b);
			}
			for (EntityBaseBean b : localUpdates.keySet()) {
				isValid(b);
				isUnique(b);
			}

			/*
			 * Now look for duplicates within the list of objects provided
			 */
			List<EntityBaseBean> comp = (new ArrayList<>(creates));
			comp.addAll(localCreates);

			logger.debug("Looking for duplicates in data by {}", comp);
			Map<Class<? extends EntityBaseBean>, List<EntityBaseBean>> beansByClass = new HashMap<>();
			for (EntityBaseBean aBean : comp) {
				List<EntityBaseBean> beans = beansByClass.get(aBean.getClass());
				if (beans == null) {
					beans = new ArrayList<>();
					beansByClass.put(aBean.getClass(), beans);
				}
				beans.add(aBean);
			}
			logger.debug("Have {} types to consider", beansByClass.keySet());

			for (Entry<Class<? extends EntityBaseBean>, List<EntityBaseBean>> pair : beansByClass.entrySet()) {
				Class<? extends EntityBaseBean> entityClass = pair.getKey();
				List<EntityBaseBean> beans = pair.getValue();
				Map<Field, Method> getters = EntityInfoHandler.getGetters(entityClass);
				List<Field> constraint = EntityInfoHandler.getConstraintFields(entityClass);
				if (!constraint.isEmpty()) {
					for (EntityBaseBean bean1 : beans) {
						for (EntityBaseBean bean2 : beans) {
							if (bean1 != bean2) {
								boolean diff = false;
								for (Field f : constraint) {
									Object value1 = getValue(getters, f, bean1);
									Object value2 = getValue(getters, f, bean2);
									if (!value1.equals(value2)) {
										diff = true;
										break;
									}
								}
								if (!diff) {
									StringBuilder erm = new StringBuilder();
									for (Field f : constraint) {
										if (erm.length() == 0) {
											erm.append(entityClass.getSimpleName() + " exists with ");
										} else {
											erm.append(", ");
										}
										erm.append(f.getName() + " = '" + getValue(getters, f, bean1) + "'");
									}
									throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS,
											erm.toString());
								}
							}
						}
					}
				}
			}

			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"Unexpected DB response " + e.getClass() + " " + e.getMessage());

		}

		// Check authz now everything persisted and update creates and
		// updates
		for (EntityBaseBean eb : localCreates) {
			gateKeeper.performAuthorisation(userId, eb, AccessType.CREATE);
			creates.add(eb);
		}

		for (Entry<EntityBaseBean, Boolean> beanEntry : localUpdates.entrySet()) {
			EntityBaseBean eb = beanEntry.getKey();
			if (beanEntry.getValue()) {
				// Identity has changed
				gateKeeper.performAuthorisation(userId, eb, AccessType.CREATE);
			}
			updates.add(eb);
		}

		if (create) {
			return bean;
		} else {
			return null;
		}

	}

	public long cloneEntity(String userId, String beanName, long id, String keys, String ip) throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.info("{} cloning {}/{}", userId, beanName, id);

		Class<? extends EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		EntityBaseBean bean = entityManager.find(klass, id);
		if (bean == null) {
			throw new IcatException(IcatExceptionType.NO_SUCH_OBJECT_FOUND, beanName + ":" + id);
		}
		EntityBaseBean clone = null;
		try {
			clone = klass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, "failed to instantiate " + beanName);
		}
		Map<EntityBaseBean, EntityBaseBean> clonedTo = new HashMap<>();
		clonedTo.put(bean, clone);
		Map<Field, Method> setters = EntityInfoHandler.getSettersForUpdate(klass);
		Map<Field, Method> getters = EntityInfoHandler.getGetters(klass);
		List<Field> constraintFields = EntityInfoHandler.getConstraintFields(klass);
		Set<Relationship> rs = EntityInfoHandler.getRelatedEntities(klass);

		for (Entry<Field, Method> fieldAndMethod : setters.entrySet()) {
			Field field = fieldAndMethod.getKey();
			try {
				Method m = getters.get(field);
				Object value = m.invoke(bean);
				if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
					if (value != null) {
						Object pk = ((EntityBaseBean) value).getId();
						value = (EntityBaseBean) entityManager.find(field.getType(), pk);
						fieldAndMethod.getValue().invoke(clone, value);
					}
				} else {
					fieldAndMethod.getValue().invoke(clone, value);
				}
			} catch (Exception e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
			}
		}

		try (JsonReader reader = Json.createReader(new ByteArrayInputStream(keys.getBytes()))) {
			JsonStructure obj = reader.read();

			if (obj.getValueType() == ValueType.OBJECT) {

				for (Entry<String, JsonValue> field : ((JsonObject) obj).entrySet()) {
					String fieldName = field.getKey();
					Field f = null;
					for (Field con : constraintFields) {
						if (con.getName().equals(fieldName)) {
							f = con;
							break;
						}
					}
					if (f == null) {
						throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
								fieldName + " is not a constraint field of " + beanName);
					}
					if (EntityBaseBean.class.isAssignableFrom(f.getType())) {
						throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
								fieldName + " can't override a many to one relationship field " + beanName);
					}
					Method setter = setters.get(f);
					logger.debug("Setting {} to {} by {}", fieldName, field.getValue(), setter);
					setter.invoke(clone, ((JsonString) field.getValue()).getString());
				}
			} else {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
						"Keys " + keys + " do not represent a json object");
			}
		} catch (Exception e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
		}

		cloneOneToManys(bean, clone, klass, getters, setters, rs, clonedTo, userId);
		clone.preparePersist(userId, entityManager, PersistMode.CLONE);
		logger.trace(clone + " prepared for persist.");

		try {
			try {
				userTransaction.begin();
				entityManager.persist(clone);
				entityManager.flush();
				logger.trace(clone + " flushed.");

				// Check authz now everything flushed
				for (EntityBaseBean c : clonedTo.values()) {
					gateKeeper.performAuthorisation(userId, c, AccessType.CREATE);
				}

				// Update any Datafile.location values if key provided
				if (key != null) {
					for (EntityBaseBean c : clonedTo.values()) {
						if (c.getClass().getSimpleName().equals("Datafile")) {
							Datafile df = (Datafile) c;
							String location = df.getLocation();
							if (location != null) {
								int i = location.lastIndexOf(' ');
								if (i >= 0) {
									location = location.substring(0, i);
									df.setLocation(location + " " + IcatSecurity.digest(df.getId(), location, key));
								}
							}
						}
					}
				}

				userTransaction.commit();

			} catch (EntityExistsException e) {
				userTransaction.rollback();
				throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getMessage());
			} catch (Throwable e) {
				userTransaction.rollback();
				logger.trace("Transaction rolled back for creation of " + clone + " because of " + e.getClass() + " "
						+ e.getMessage());
				updateCache();
				bean.preparePersist(userId, entityManager, PersistMode.CLONE);
				isUnique(clone);
				isValid(clone);
				logger.error("Database unhappy", e);
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Unexpected DB response " + e.getClass() + " " + e.getMessage());
			}
		} catch (IllegalStateException | SecurityException | SystemException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					e.getClass().getSimpleName() + e.getMessage());
		}

		/*
		 * Nothing should be able to go wrong now so log, update and send
		 * notification messages
		 */
		if (logRequests.contains(CallType.WRITE)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userId);
				gen.write("entityName", clone.getClass().getSimpleName());
				gen.write("entityId", clone.getId());
				gen.writeEnd();
			}
			transmitter.processMessage("write", ip, baos.toString(), startMillis);
		}

		if (searchActive) {
			for (EntityBaseBean c : clonedTo.values()) {
				searchManager.addDocument(entityManager, c);
			}
		}

		for (EntityBaseBean c : clonedTo.values()) {
			try {
				notificationTransmitter.processMessage(new NotificationMessage(Operation.C, c, notificationRequests));
			} catch (JMSException e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Operation completed but unable to send JMS message " + e.getMessage());
			}
		}
		return clone.getId();
	}

	private void cloneOneToManys(EntityBaseBean bean, EntityBaseBean clone, Class<? extends EntityBaseBean> klass,
			Map<Field, Method> getters, Map<Field, Method> setters, Set<Relationship> rs,
			Map<EntityBaseBean, EntityBaseBean> clonedTo, String userId) throws IcatException {

		gateKeeper.performAuthorisation(userId, bean, AccessType.READ);

		for (Relationship r : rs) {
			if (r.isCollection()) {
				Method m = getters.get(r.getField());
				Method back = r.getInverseSetter();
				try {
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> collection = (List<EntityBaseBean>) m.invoke(bean);
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> clonedCollection = (List<EntityBaseBean>) m.invoke(clone);

					for (EntityBaseBean c : collection) {
						Class<? extends EntityBaseBean> subKlass = c.getClass();
						EntityBaseBean subClone = clonedTo.get(c);

						if (subClone == null) {
							try {
								subClone = subKlass.newInstance();
							} catch (InstantiationException | IllegalAccessException e) {
								throw new IcatException(IcatExceptionType.INTERNAL,
										"failed to instantiate " + subKlass.getSimpleName());
							}

							clonedCollection.add(subClone);
							clonedTo.put(c, subClone);

							Map<Field, Method> subSetters = EntityInfoHandler.getSettersForUpdate(subKlass);
							Map<Field, Method> subGetters = EntityInfoHandler.getGetters(subKlass);
							Set<Relationship> subRs = EntityInfoHandler.getRelatedEntities(subKlass);

							for (Entry<Field, Method> fieldAndMethod : subSetters.entrySet()) {
								Field field = fieldAndMethod.getKey();
								try {
									Method subM = subGetters.get(field);
									Object value = subM.invoke(c);
									if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
										Method setRel = fieldAndMethod.getValue();
										if (setRel.equals(back)) {
											value = clone;
											logger.trace("Setting back ref {} {} {}", subClone, back, clone);
										}
										if (value != null) {
											setRel.invoke(subClone, value);
										}
									} else {
										fieldAndMethod.getValue().invoke(subClone, value);
									}
								} catch (Exception e) {
									throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "" + e);
								}
							}
							cloneOneToManys(c, subClone, subKlass, subGetters, subSetters, subRs, clonedTo, userId);
						} else {
							logger.trace("Setting back ref for existing clone {} {} {}", subClone, back, clone);
							clonedCollection.add(subClone);
							back.invoke(subClone, clone);
						}
					}
				} catch (Exception e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
				}
			}
		}

	}

}
