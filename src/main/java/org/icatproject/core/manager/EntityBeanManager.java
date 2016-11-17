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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.ParameterValueType;
import org.icatproject.core.entity.Session;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.Lucene.LuceneSearchResult;
import org.icatproject.core.manager.Lucene.ParameterPOJO;
import org.icatproject.core.manager.LuceneSingleton.ScoredResult;
import org.icatproject.core.manager.PropertyHandler.CallType;
import org.icatproject.core.manager.PropertyHandler.Operation;
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

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

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
	LuceneSingleton luceneSingleton;

	Lucene lucene;

	private boolean log;

	Marker fatal = MarkerFactory.getMarker("FATAL");

	private Set<CallType> logRequests;

	private Map<String, NotificationRequest> notificationRequests;

	private boolean luceneActive;

	private int maxEntities;

	private long exportCacheSize;
	private Set<String> rootUserNames;

	private String key;

	private String buildKey(EntityBaseBean bean, Map<String, Map<Long, String>> exportCaches)
			throws IcatException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends EntityBaseBean> klass = bean.getClass();
		List<Field> constraintFields = eiHandler.getConstraintFields(klass);
		if (constraintFields.isEmpty()) {
			return '"' + bean.getId().toString() + '"';
		}
		List<Field> fields = eiHandler.getFields(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		Map<String, Field> fieldMap = eiHandler.getFieldsByName(klass);
		Set<Field> atts = eiHandler.getAttributes(klass);
		Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();
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
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		for (Field field : eiHandler.getRelInKey(klass)) {
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

	public CreateResponse create(String userId, EntityBaseBean bean, EntityManager manager,
			UserTransaction userTransaction, boolean allAttributes, String ip) throws IcatException {

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
				bean.preparePersist(userId, manager, gateKeeper, persistMode);
				logger.trace(bean + " prepared for persist.");
				manager.persist(bean);
				logger.trace(bean + " persisted.");
				manager.flush();
				logger.trace(bean + " flushed.");
				// Check authz now everything persisted
				gateKeeper.performAuthorisation(userId, bean, AccessType.CREATE, manager);
				NotificationMessage notification = new NotificationMessage(Operation.C, bean, manager,
						notificationRequests);

				long beanId = bean.getId();

				if (luceneActive) {
					bean.addToLucene(lucene);
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

				bean.preparePersist(userId, manager, gateKeeper, persistMode);
				isUnique(bean, manager);
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

	private boolean createAllowed(String userId, EntityBaseBean bean, EntityManager manager,
			UserTransaction userTransaction) throws IcatException {
		try {
			userTransaction.begin();
			try {
				try {
					bean.preparePersist(userId, manager, gateKeeper, PersistMode.IMPORT_OR_WS);
					logger.debug(bean + " prepared for persist (createAllowed).");
					manager.persist(bean);
					logger.debug(bean + " persisted (createAllowed).");
					manager.flush();
					logger.debug(bean + " flushed (createAllowed).");
				} catch (EntityExistsException e) {
					throw new IcatException(IcatException.IcatExceptionType.OBJECT_ALREADY_EXISTS, e.getMessage());
				} catch (Throwable e) {
					userTransaction.rollback();
					logger.debug("Transaction rolled back for creation of " + bean + " because of " + e.getClass() + " "
							+ e.getMessage());
					bean.preparePersist(userId, manager, gateKeeper, PersistMode.IMPORT_OR_WS);
					isUnique(bean, manager);
					isValid(bean);
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"Unexpected DB response " + e.getClass() + " " + e.getMessage());
				}
				try {
					gateKeeper.performAuthorisation(userId, bean, AccessType.CREATE, manager);
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

	public List<CreateResponse> createMany(String userId, List<EntityBaseBean> beans, EntityManager manager,
			UserTransaction userTransaction, String ip) throws IcatException {
		try {
			userTransaction.begin();
			List<CreateResponse> crs = new ArrayList<CreateResponse>();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				for (EntityBaseBean bean : beans) {
					bean.preparePersist(userId, manager, gateKeeper, PersistMode.IMPORT_OR_WS);
					logger.trace(bean + " prepared for persist.");
					manager.persist(bean);
					logger.trace(bean + " persisted.");
					manager.flush();
					logger.trace(bean + " flushed.");
					// Check authz now everything persisted
					gateKeeper.performAuthorisation(userId, bean, AccessType.CREATE, manager);
					NotificationMessage notification = new NotificationMessage(Operation.C, bean, manager,
							notificationRequests);
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

				if (luceneActive) {
					for (EntityBaseBean bean : beans) {
						bean.addToLucene(lucene);
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
					bean.preparePersist(userId, manager, gateKeeper, PersistMode.IMPORT_OR_WS);
					isUnique(bean, manager);
					isValid(bean);
				} catch (IcatException e1) {
					e1.setOffset(pos);
					throw e1;
				}
				/*
				 * Now look for duplicates within the list of objects provided
				 */
				Class<? extends EntityBaseBean> entityClass = bean.getClass();
				Map<Field, Method> getters = eiHandler.getGetters(entityClass);

				List<Field> constraint = eiHandler.getConstraintFields(entityClass);
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

	public void delete(String userId, List<EntityBaseBean> beans, EntityManager manager,
			UserTransaction userTransaction, String ip) throws IcatException {
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

				List<EntityBaseBean> allBeansToDelete = new ArrayList<>();
				for (EntityBaseBean bean : beans) {
					List<EntityBaseBean> beansToDelete = new ArrayList<>();
					EntityBaseBean beanManaged = find(bean, manager);
					beansToDelete.add(beanManaged);
					beansToDelete.addAll(getDependentBeans(beanManaged));

					if (firstBean == null) {
						firstBean = beanManaged;
					}
					for (EntityBaseBean b : beansToDelete) {
						gateKeeper.performAuthorisation(userId, b, AccessType.DELETE, manager);
					}
					manager.remove(beanManaged);
					manager.flush();
					logger.trace("Deleted bean " + bean + " flushed.");
					allBeansToDelete.addAll(beansToDelete);
					offset++;
				}

				userTransaction.commit();

				if (luceneActive) {
					for (EntityBaseBean bean : allBeansToDelete) {
						lucene.deleteDocument(bean);
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
	public Response export(final String userId, final boolean allAttributes, final EntityManager manager,
			UserTransaction userTransaction) {

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
						exportTable(s, null, output, exportCaches, allAttributes, manager, userId);
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
	public Response export(final String userId, String query, final boolean all, final EntityManager manager,
			UserTransaction userTransaction) throws IcatException {

		logger.info(userId + " exporting " + query);

		EntitySetResult esr = getEntitySet(userId, query, manager);
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
					((EntityBaseBean) beanManaged).collectIds(ids, one, 0, steps, gateKeeper, userId, manager);
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
									exportTable(s, table, output, exportCaches, all, manager, userId);
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
								eiHandler.getExportNull((Class<? extends EntityBaseBean>) field.getType()).getBytes());
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
			Map<String, Map<Long, String>> exportCaches, boolean allAttributes, EntityManager manager, String userId)
			throws IcatException, IOException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		logger.debug("Export " + (ids == null ? "complete" : "partial") + " " + beanName);
		Class<EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		output.write((linesep).getBytes());
		if (allAttributes) {
			output.write(eiHandler.getExportHeaderAll(klass).getBytes());
		} else {
			output.write(eiHandler.getExportHeader(klass).getBytes());
		}
		output.write((linesep).getBytes());
		List<Field> fields = eiHandler.getFields(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		Map<String, Field> fieldMap = eiHandler.getFieldsByName(klass);
		Set<Field> atts = eiHandler.getAttributes(klass);
		Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();
		boolean qcolumn = eiHandler.getConstraintFields(klass).isEmpty();
		boolean notRootUser = !rootUserNames.contains(userId);

		if (ids == null) {
			int start = 0;

			while (true) {
				/* Get beans in blocks. */
				List<EntityBaseBean> beans = manager
						.createQuery("SELECT e from " + beanName + " e ORDER BY e.id", EntityBaseBean.class)
						.setFirstResult(start).setMaxResults(500).getResultList();

				if (beans.size() == 0) {
					break;
				}
				for (EntityBaseBean bean : beans) {
					if (notRootUser) {
						try {
							gateKeeper.performAuthorisation(userId, bean, AccessType.READ, manager);
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
				EntityBaseBean bean = manager.find(klass, id);
				if (bean != null) {
					exportBean(bean, output, qcolumn, allAttributes, fields, updaters, exportCaches, getters, fieldMap,
							atts);
				}
			}
		}
	}

	private void filterReadAccess(List<ScoredEntityBaseBean> results, List<ScoredResult> allResults, int maxCount,
			String userId, EntityManager manager, Class<? extends EntityBaseBean> klass) throws IcatException {

		logger.debug("Got " + allResults.size() + " results from Lucene");
		for (ScoredResult sr : allResults) {
			long entityId = Long.parseLong(sr.getResult());
			EntityBaseBean beanManaged = manager.find(klass, entityId);
			if (beanManaged != null) {
				try {
					gateKeeper.performAuthorisation(userId, beanManaged, AccessType.READ, manager);
					results.add(new ScoredEntityBaseBean(entityId, sr.getScore()));
					if (results.size() > maxEntities) {
						throw new IcatException(IcatExceptionType.VALIDATION,
								"attempt to return more than " + maxEntities + " entitities");
					}
					if (results.size() == maxCount) {
						break;
					}
				} catch (IcatException e) {
					// Nothing to do
				}
			}
		}
	}

	private EntityBaseBean find(EntityBaseBean bean, EntityManager manager) throws IcatException {
		Long primaryKey = bean.getId();
		Class<? extends EntityBaseBean> entityClass = bean.getClass();
		if (primaryKey == null) {
			throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND,
					entityClass.getSimpleName() + " has null primary key.");
		}
		EntityBaseBean object = null;
		try {
			object = manager.find(entityClass, primaryKey);
		} catch (Throwable e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Unexpected DB response " + e);
		}

		if (object == null) {
			throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND,
					entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}
		return object;
	}

	public EntityBaseBean get(String userId, String query, long primaryKey, EntityManager manager, String ip)
			throws IcatException {

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

		EntityBaseBean beanManaged = manager.find(entityClass, primaryKey);
		if (beanManaged == null) {
			throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND,
					entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}

		gateKeeper.performAuthorisation(userId, beanManaged, AccessType.READ, manager);
		logger.debug("got " + entityClass.getSimpleName() + "[id:" + primaryKey + "]");

		IncludeClause include = getQuery.getInclude();
		boolean one = false;
		List<Step> steps = null;
		if (include != null) {
			one = include.isOne();
			steps = include.getSteps();
		}
		result = beanManaged.pruned(one, 0, steps, maxEntities, gateKeeper, userId, manager);
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
		logger.trace("Get dependent beans for {}", bean);
		Class<? extends EntityBaseBean> klass = bean.getClass();
		Set<Relationship> rs = eiHandler.getRelatedEntities(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
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
		return eiHandler.getEntityInfo(beanName);
	}

	@SuppressWarnings("unchecked")
	private EntitySetResult getEntitySet(String userId, String query, EntityManager manager) throws IcatException {

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
		String jpql = q.getJPQL(userId, manager);
		logger.info("Final search JPQL: " + jpql);

		/* Null query indicates that nothing accepted by authz */
		if (jpql == null) {
			return new EntitySetResult(q, q.getNoAuthzResult());
		}

		/* Create query - which may go wrong */
		javax.persistence.Query jpqlQuery;
		try {
			jpqlQuery = manager.createQuery(jpql);
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
				gateKeeper.checkJPQL(sb.toString());

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
					"attempt to process more than " + maxEntities + " entitities");
		}

		List<Object> result = null;

		result = (List<Object>) objects;
		// eclipselink returns BigDecimal for aggregate
		// functions on Long and Double for oracle
		if (result.size() == 1) {
			logger.debug("One result only - look for bad return types");
			Object obj = result.get(0);
			if (obj != null) {
				logger.debug("Type is " + obj.getClass());
				if (obj.getClass() == BigDecimal.class) {
					String typeQueryString = q.typeQuery();
					logger.debug("Type query: " + typeQueryString);
					Query typeQuery = manager.createQuery(typeQueryString).setMaxResults(1);
					Class<? extends Object> klass = typeQuery.getSingleResult().getClass();
					logger.debug("Class is " + klass);
					if (klass == Long.class) {
						result.set(0, ((BigDecimal) obj).longValue());
					} else if (klass == Double.class) {
						result.set(0, ((BigDecimal) obj).doubleValue());
					} else
						throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
								"Type " + klass + " neither Long nor Double");
				}
			}
		}

		logger.debug("Obtained " + result.size() + " results.");
		return new EntitySetResult(q, result);
	}

	public List<String> getProperties() {
		return propertyHandler.props();
	}

	public double getRemainingMinutes(String sessionId, EntityManager manager) throws IcatException {
		logger.debug("getRemainingMinutes for sessionId " + sessionId);
		Session session = getSession(sessionId, manager);
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

	private Session getSession(String sessionId, EntityManager manager) throws IcatException {
		Session session = null;
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session Id cannot be null or empty.");
		}
		session = (Session) manager.find(Session.class, sessionId);
		if (session == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Unable to find user by sessionid: " + sessionId);
		}
		return session;
	}

	public String getUserName(String sessionId, EntityManager manager) throws IcatException {
		try {
			Session session = getSession(sessionId, manager);
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
		String luceneHost = propertyHandler.getLuceneHost();
		if (luceneHost != null) {
			String jndi = "java:global/icat.server-" + Constants.API_VERSION
					+ "/LuceneSingleton!org.icatproject.core.manager.Lucene";

			Context ctx = null;
			try {
				ctx = new InitialContext();
				ctx.addToEnvironment("org.omg.CORBA.ORBInitialHost", luceneHost);
				ctx.addToEnvironment("org.omg.CORBA.ORBInitialPort", Integer.toString(propertyHandler.getLucenePort()));
				lucene = (Lucene) ctx.lookup(jndi);
				logger.debug("Found Lucene: " + lucene + " with jndi " + jndi);
			} catch (NamingException e) {
				String msg = e.getClass() + " reports " + e.getMessage() + " from " + jndi;
				logger.error(fatal, msg);
				throw new IllegalStateException(msg);
			}
		} else {
			lucene = luceneSingleton;
		}

		logRequests = propertyHandler.getLogSet();
		log = !logRequests.isEmpty();
		notificationRequests = propertyHandler.getNotificationRequests();
		luceneActive = lucene.getActive();
		maxEntities = propertyHandler.getMaxEntities();
		exportCacheSize = propertyHandler.getImportCacheSize();
		rootUserNames = propertyHandler.getRootUserNames();
		key = propertyHandler.getKey();
	}

	public boolean isAccessAllowed(String userId, EntityBaseBean bean, EntityManager manager,
			UserTransaction userTransaction, AccessType accessType) throws IcatException {
		if (accessType == AccessType.CREATE) {
			return createAllowed(userId, bean, manager, userTransaction);
		} else {
			try {
				gateKeeper.performAuthorisation(userId, bean, accessType, manager);
				return true;
			} catch (IcatException e) {
				if (e.getType() != IcatExceptionType.INSUFFICIENT_PRIVILEGES) {
					throw e;
				}
				return false;
			}
		}
	}

	public boolean isLoggedIn(String userName, EntityManager manager) {
		logger.debug("isLoggedIn for user " + userName);
		return manager.createNamedQuery(Session.ISLOGGEDIN, Long.class).setParameter("userName", userName)
				.getSingleResult() > 0;
	}

	private void isUnique(EntityBaseBean bean, EntityManager manager) throws IcatException {
		logger.trace("Check uniqueness of {}", bean);
		EntityBaseBean other = lookup(bean, manager);

		if (other != null) {
			Class<? extends EntityBaseBean> entityClass = bean.getClass();
			Map<Field, Method> getters = eiHandler.getGetters(entityClass);
			List<Field> constraint = eiHandler.getConstraintFields(entityClass);

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
		List<Field> notNullFields = eiHandler.getNotNullableFields(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);

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

		Map<Field, Integer> stringFields = eiHandler.getStringFields(klass);
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

	public String login(String userName, int lifetimeMinutes, EntityManager manager, UserTransaction userTransaction,
			String ip) throws IcatException {
		Session session = new Session(userName, lifetimeMinutes);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				manager.persist(session);
				manager.flush();
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

	public void logout(String sessionId, EntityManager manager, UserTransaction userTransaction, String ip)
			throws IcatException {
		logger.debug("logout for sessionId " + sessionId);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				Session session = getSession(sessionId, manager);
				manager.remove(session);
				manager.flush();
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

	public EntityBaseBean lookup(EntityBaseBean bean, EntityManager manager) throws IcatException {
		Class<? extends EntityBaseBean> entityClass = bean.getClass();

		Map<Field, Method> getters = eiHandler.getGetters(entityClass);
		List<Field> constraint = eiHandler.getConstraintFields(entityClass);
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
		TypedQuery<EntityBaseBean> query = manager.createQuery(queryString.toString() + ")", EntityBaseBean.class);
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

	public void luceneClear() throws IcatException {
		if (luceneActive) {
			lucene.clear();
		}
	}

	public void luceneCommit() throws IcatException {
		if (luceneActive) {
			lucene.commit();
		}
	}

	public List<ScoredEntityBaseBean> luceneDatafiles(String userName, String user, String text, String lower,
			String upper, List<ParameterPOJO> parms, int maxCount, EntityManager manager, String ip)
			throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		List<ScoredEntityBaseBean> results = new ArrayList<>();
		if (luceneActive) {
			LuceneSearchResult last = null;
			List<ScoredResult> allResults = Collections.emptyList();
			/*
			 * As results may be rejected and maxCount may be 1 ensure that we
			 * don't make a huge number of calls to Lucene
			 */
			int blockSize = Math.max(1000, maxCount);

			do {
				if (last == null) {
					last = lucene.datafiles(user, text, lower, upper, parms, blockSize);
				} else {
					last = lucene.datafilesAfter(user, text, lower, upper, parms, blockSize, last);
				}
				allResults = last.getResults();
				filterReadAccess(results, allResults, maxCount, userName, manager, Datafile.class);
			} while (results.size() != maxCount && allResults.size() == blockSize);
			/* failing lucene retrieval calls clean up before throwing */
			lucene.freeSearcher(last);
		}

		if (logRequests.contains("R")) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				if (results.size() > 0) {
					gen.write("entityId", results.get(0).getEntityBaseBeanId());
				}
				gen.writeEnd();
			}
			transmitter.processMessage("luceneDatafiles", ip, baos.toString(), startMillis);
		}
		logger.debug("Returning {} results", results.size());
		return results;
	}

	public List<ScoredEntityBaseBean> luceneDatasets(String userName, String user, String text, String lower,
			String upper, List<ParameterPOJO> parms, int maxCount, EntityManager manager, String ip)
			throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		List<ScoredEntityBaseBean> results = new ArrayList<>();
		if (luceneActive) {
			LuceneSearchResult last = null;
			List<ScoredResult> allResults = Collections.emptyList();
			/*
			 * As results may be rejected and maxCount may be 1 ensure that we
			 * don't make a huge number of calls to Lucene
			 */
			int blockSize = Math.max(1000, maxCount);

			do {
				if (last == null) {
					last = lucene.datasets(user, text, lower, upper, parms, blockSize);
				} else {
					last = lucene.datasetsAfter(user, text, lower, upper, parms, blockSize, last);
				}
				allResults = last.getResults();
				filterReadAccess(results, allResults, maxCount, userName, manager, Dataset.class);
			} while (results.size() != maxCount && allResults.size() == blockSize);
			/* failing lucene retrieval calls clean up before throwing */
			lucene.freeSearcher(last);
		}
		if (logRequests.contains("R")) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				if (results.size() > 0) {
					gen.write("entityId", results.get(0).getEntityBaseBeanId());
				}
				gen.writeEnd();
			}
			transmitter.processMessage("luceneDatasets", ip, baos.toString(), startMillis);
		}
		logger.debug("Returning {} results", results.size());
		return results;
	}

	public List<String> luceneGetPopulating() {
		if (luceneActive) {
			return lucene.getPopulating();
		} else {
			return Collections.emptyList();
		}
	}

	public List<ScoredEntityBaseBean> luceneInvestigations(String userName, String user, String text, String lower,
			String upper, List<ParameterPOJO> parms, List<String> samples, String userFullName, int maxCount,
			EntityManager manager, String ip) throws IcatException {

		long startMillis = log ? System.currentTimeMillis() : 0;
		List<ScoredEntityBaseBean> results = new ArrayList<>();
		if (luceneActive) {
			LuceneSearchResult last = null;
			List<ScoredResult> allResults = Collections.emptyList();
			/*
			 * As results may be rejected and maxCount may be 1 ensure that we
			 * don't make a huge number of calls to Lucene
			 */
			int blockSize = Math.max(1000, maxCount);

			do {
				if (last == null) {
					last = lucene.investigations(user, text, lower, upper, parms, samples, userFullName, blockSize);
				} else {
					last = lucene.investigationsAfter(user, text, lower, upper, parms, samples, userFullName, blockSize,
							last);
				}
				allResults = last.getResults();
				filterReadAccess(results, allResults, maxCount, userName, manager, Investigation.class);
			} while (results.size() != maxCount && allResults.size() == blockSize);
			/* failing lucene retrieval calls clean up before throwing */
			lucene.freeSearcher(last);
		}
		if (logRequests.contains("R")) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos).writeStartObject()) {
				gen.write("userName", userName);
				if (results.size() > 0) {
					gen.write("entityId", results.get(0).getEntityBaseBeanId());
				}
				gen.writeEnd();
			}
			transmitter.processMessage("luceneInvestigations", ip, baos.toString(), startMillis);
		}
		logger.debug("Returning {} results", results.size());
		return results;
	}

	public void lucenePopulate(String entityName, EntityManager manager) throws IcatException {
		if (luceneActive) {
			try {
				Class.forName(Constants.ENTITY_PREFIX + entityName);
			} catch (ClassNotFoundException e) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getMessage());
			}
			lucene.populate(entityName);
		}
	}

	// This code might be in EntityBaseBean however this would mean that it
	// would be processed by JPA which gets confused by it.
	private void merge(EntityBaseBean thisBean, Object fromBean, EntityManager manager) throws IcatException {
		Class<? extends EntityBaseBean> klass = thisBean.getClass();
		Map<Field, Method> setters = eiHandler.getSettersForUpdate(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);

		for (Entry<Field, Method> fieldAndMethod : setters.entrySet()) {
			Field field = fieldAndMethod.getKey();
			try {
				Method m = getters.get(field);
				Object value = m.invoke(fromBean, new Object[0]);
				if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
					logger.debug("Needs special processing as " + value + " is a bean");
					if (value != null) {
						Object pk = ((EntityBaseBean) value).getId();
						value = (EntityBaseBean) manager.find(field.getType(), pk);
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
			EntityManager manager, List<EntityBaseBean> creates, Map<EntityBaseBean, Boolean> localUpdates,
			boolean create, String userId) throws IcatException {
		Map<String, Field> fieldsByName = eiHandler.getFieldsByName(klass);
		Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();
		Map<Field, Method> setters = eiHandler.getSetters(klass);
		Map<String, Relationship> rels = eiHandler.getRelationshipsByName(klass);
		Map<String, Method> getters = eiHandler.getGettersFromName(klass);
		Set<Field> relInKey = eiHandler.getRelInKey(klass);

		boolean deleteAllowed = false;
		if (!create) {
			gateKeeper.performUpdateAuthorisation(userId, bean, contents, manager);

			/*
			 * See if delete is allowed - it may not be relevant but need to
			 * check now before modifications are made
			 */
			deleteAllowed = gateKeeper.isAccessAllowed(userId, bean, AccessType.DELETE, manager);
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
							arg = parseSubEntity((JsonObject) fValue, rels.get(fName), manager, creates, localUpdates,
									userId);
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
							EntityBaseBean arg = parseSubEntity((JsonObject) aValue, rels.get(fName), manager, creates,
									localUpdates, userId);
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

	private EntityBaseBean parseSubEntity(JsonObject contents, Relationship relationship, EntityManager manager,
			List<EntityBaseBean> creates, Map<EntityBaseBean, Boolean> localUpdates, String userId)
			throws IcatException {
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
			bean = find(bean, manager);
		}

		parseEntity(bean, contents, klass, manager, creates, localUpdates, create, userId);
		return bean;

	}

	public void refresh(String sessionId, int lifetimeMinutes, EntityManager manager, UserTransaction userTransaction,
			String ip) throws IcatException {
		logger.debug("logout for sessionId " + sessionId);
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				Session session = getSession(sessionId, manager);
				session.refresh(lifetimeMinutes);
				manager.flush();
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

	public List<?> search(String userId, String query, EntityManager manager, String ip) throws IcatException {

		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.info(userId + " searching for " + query);

		EntitySetResult esr = getEntitySet(userId, query, manager);
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
								"attempt to return more than " + maxEntities + " entitities");
					}
					clones.add(null);
				} else {
					EntityBaseBean eb = ((EntityBaseBean) beanManaged).pruned(one, 0, steps, maxEntities, gateKeeper,
							userId, manager);
					if ((descendantCount += eb.getDescendantCount(maxEntities)) > maxEntities) {
						throw new IcatException(IcatExceptionType.VALIDATION,
								"attempt to return more than " + maxEntities + " entitities");
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

	public NotificationMessage update(String userId, EntityBaseBean bean, EntityManager manager,
			UserTransaction userTransaction, boolean allAttributes, String ip) throws IcatException {
		try {
			userTransaction.begin();
			try {
				long startMillis = log ? System.currentTimeMillis() : 0;
				EntityBaseBean beanManaged = find(bean, manager);
				gateKeeper.performAuthorisation(userId, beanManaged, AccessType.UPDATE, manager);
				boolean identityChange = checkIdentityChange(beanManaged, bean);
				if (identityChange) {
					gateKeeper.performAuthorisation(userId, beanManaged, AccessType.DELETE, manager);
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
				merge(beanManaged, bean, manager);
				if (identityChange) {
					gateKeeper.performAuthorisation(userId, beanManaged, AccessType.CREATE, manager);
				}
				beanManaged.postMergeFixup(manager, gateKeeper);
				manager.flush();
				logger.trace("Updated bean " + bean + " flushed.");
				NotificationMessage notification = new NotificationMessage(Operation.U, bean, manager,
						notificationRequests);
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
				if (luceneActive) {
					beanManaged.updateInLucene(lucene);
				}
				return notification;
			} catch (IcatException e) {
				userTransaction.rollback();
				throw e;
			} catch (Throwable e) {
				userTransaction.rollback();
				updateCache();
				EntityBaseBean beanManaged = find(bean, manager);
				beanManaged.setModId(userId);
				merge(beanManaged, bean, manager);
				beanManaged.postMergeFixup(manager, gateKeeper);
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

	public List<Long> write(String userId, String json, EntityManager manager, UserTransaction userTransaction,
			String ip) throws IcatException {
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
						EntityBaseBean bean = writeOne((JsonObject) obj, manager, userId, creates, updates,
								userTransaction);
						if (bean != null) {
							beanIds.add(bean.getId());
						}
						offset++;
					}
				} else {
					EntityBaseBean bean = writeOne((JsonObject) top, manager, userId, creates, updates,
							userTransaction);
					if (bean != null) {
						beanIds.add(bean.getId());
					}
				}
				userTransaction.commit();

				/*
				 * Nothing should be able to go wrong now so log, update lucene
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

				if (luceneActive) {
					for (EntityBaseBean eb : creates) {
						lucene.addDocument(eb);
					}
					for (EntityBaseBean eb : updates) {
						lucene.updateDocument(eb);
					}
				}

				try {
					for (EntityBaseBean eb : creates) {
						notificationTransmitter.processMessage(
								new NotificationMessage(Operation.C, eb, manager, notificationRequests));
					}

					for (EntityBaseBean eb : updates) {
						notificationTransmitter.processMessage(
								new NotificationMessage(Operation.U, eb, manager, notificationRequests));
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

	private EntityBaseBean writeOne(JsonObject entity, EntityManager manager, String userId,
			List<EntityBaseBean> creates, List<EntityBaseBean> updates, UserTransaction userTransaction)
			throws IcatException {
		logger.debug("write one {}", entity);

		if (entity.size() != 1) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"Entity must have one keyword followed by its values in json " + entity);
		}

		Entry<String, JsonValue> entry = entity.entrySet().iterator().next();
		String beanName = entry.getKey();
		Class<EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
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
			bean = find(bean, manager);
		}
		List<EntityBaseBean> localCreates = new ArrayList<>();
		Map<EntityBaseBean, Boolean> localUpdates = new HashMap<>();
		parseEntity(bean, contents, klass, manager, localCreates, localUpdates, create, userId);

		for (EntityBaseBean b : localUpdates.keySet()) {
			b.setModId(userId);
			b.setModTime(new Date());
		}

		try {
			bean.preparePersist(userId, manager, gateKeeper, PersistMode.REST);
			if (create) {
				manager.persist(bean);
				logger.trace(bean + " persisted.");
			}
			for (EntityBaseBean b : localUpdates.keySet()) {
				b.postMergeFixup(manager, gateKeeper);
			}
			manager.flush();
			logger.trace(bean + " flushed.");
		} catch (Throwable e) {
			/*
			 * Clear transaction so can use database again then start new
			 * transaction to put things into expected state
			 */
			logger.debug("Problem shows up with persist/flush will rollback and check: {} {}", e.getClass(),
					e.getMessage());
			try {
				userTransaction.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						e1.getClass() + " " + e1.getMessage());
			}
			for (EntityBaseBean b : localCreates) {
				isValid(b);
				isUnique(b, manager);
			}
			for (EntityBaseBean b : localUpdates.keySet()) {
				isValid(b);
				isUnique(b, manager);
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
				Map<Field, Method> getters = eiHandler.getGetters(entityClass);
				List<Field> constraint = eiHandler.getConstraintFields(entityClass);
				if (!constraint.isEmpty()) {
					for (EntityBaseBean bean1 : beans) {
						for (EntityBaseBean bean2 : beans) {
							if (bean1.getId() == null && bean2.getId() != null) {
								boolean diff = false;
								for (Field f : constraint) {
									Object value1 = getValue(getters, f, bean1);
									Object value2 = getValue(getters, f, bean2);
									if (!value1.equals(value2)) {
										logger.debug("No problem with objects {} and {} as {} have values {} and {}",
												bean1, bean2, f.getName(), value1, value2);
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
			gateKeeper.performAuthorisation(userId, eb, AccessType.CREATE, manager);
			creates.add(eb);
		}

		for (Entry<EntityBaseBean, Boolean> beanEntry : localUpdates.entrySet()) {
			EntityBaseBean eb = beanEntry.getKey();
			if (beanEntry.getValue()) {
				// Identity has changed
				gateKeeper.performAuthorisation(userId, eb, AccessType.CREATE, manager);
			}
			updates.add(eb);
		}

		if (create) {
			return bean;
		} else {
			return null;
		}

	}

	public long cloneEntity(String userId, String beanName, long id, String keys, EntityManager manager,
			UserTransaction userTransaction, String ip) throws IcatException {
		long startMillis = log ? System.currentTimeMillis() : 0;
		logger.info("{} cloning {}/{}", userId, beanName, id);

		Class<EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		EntityBaseBean bean = manager.find(klass, id);
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
		Map<Field, Method> setters = eiHandler.getSettersForUpdate(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		List<Field> constraintFields = eiHandler.getConstraintFields(klass);
		Set<Relationship> rs = eiHandler.getRelatedEntities(klass);

		for (Entry<Field, Method> fieldAndMethod : setters.entrySet()) {
			Field field = fieldAndMethod.getKey();
			try {
				Method m = getters.get(field);
				Object value = m.invoke(bean);
				if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
					if (value != null) {
						Object pk = ((EntityBaseBean) value).getId();
						value = (EntityBaseBean) manager.find(field.getType(), pk);
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

		cloneOneToManys(bean, clone, klass, getters, setters, rs, manager, clonedTo, userId);
		clone.preparePersist(userId, manager, gateKeeper, PersistMode.CLONE);
		logger.trace(clone + " prepared for persist.");

		try {
			try {
				userTransaction.begin();
				manager.persist(clone);
				manager.flush();
				logger.trace(clone + " flushed.");

				// Check authz now everything flushed
				for (EntityBaseBean c : clonedTo.values()) {
					gateKeeper.performAuthorisation(userId, c, AccessType.CREATE, manager);
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
				bean.preparePersist(userId, manager, gateKeeper, PersistMode.CLONE);
				isUnique(clone, manager);
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
		 * Nothing should be able to go wrong now so log, update lucene and send
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

		if (luceneActive) {
			for (EntityBaseBean c : clonedTo.values()) {
				lucene.addDocument(c);
			}
		}

		for (EntityBaseBean c : clonedTo.values()) {
			try {
				notificationTransmitter
						.processMessage(new NotificationMessage(Operation.C, c, manager, notificationRequests));
			} catch (JMSException e) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
						"Operation completed but unable to send JMS message " + e.getMessage());
			}
		}
		return clone.getId();
	}

	private void cloneOneToManys(EntityBaseBean bean, EntityBaseBean clone, Class<? extends EntityBaseBean> klass,
			Map<Field, Method> getters, Map<Field, Method> setters, Set<Relationship> rs, EntityManager manager,
			Map<EntityBaseBean, EntityBaseBean> clonedTo, String userId) throws IcatException {

		gateKeeper.performAuthorisation(userId, bean, AccessType.READ, manager);

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

							Map<Field, Method> subSetters = eiHandler.getSettersForUpdate(subKlass);
							Map<Field, Method> subGetters = eiHandler.getGetters(subKlass);
							Set<Relationship> subRs = eiHandler.getRelatedEntities(subKlass);

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
							cloneOneToManys(c, subClone, subKlass, subGetters, subSetters, subRs, manager, clonedTo,
									userId);
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
