package org.icatproject.core.manager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.ParameterValueType;
import org.icatproject.core.entity.Session;
import org.icatproject.core.manager.importParser.Attribute;
import org.icatproject.core.manager.importParser.Input;
import org.icatproject.core.manager.importParser.ParserException;
import org.icatproject.core.manager.importParser.Table;
import org.icatproject.core.manager.importParser.TableField;
import org.icatproject.core.manager.importParser.Token;
import org.icatproject.core.manager.importParser.Token.Type;
import org.icatproject.core.manager.importParser.Tokenizer;
import org.icatproject.core.parser.LexerException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class Porter {

	public enum ExportType {
		/** query field must be set with what to export */
		QUERY,

		/** query field must not be set - dump everything */
		DUMP
	}

	public enum Attributes {
		/** Include createId etc */
		ALL,

		/** Only export attributes which may normally be set by the user */
		USER
	}

	public enum DuplicateAction {
		/** Throw an expection */
		THROW,

		/** Don't check just go to the next row */
		IGNORE,

		/** Check that new data matches the old */
		CHECK,

		/** Replace old data with new */
		OVERWRITE
	}

	@EJB
	EntityBeanManager beanManager;

	@EJB
	PropertyHandler propertyHandler;

	private Set<String> rootUserNames;

	private static final Logger logger = Logger.getLogger(Porter.class);
	private final static Pattern tsRegExp = Pattern
			.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(\\.\\d+)?(.*?)");

	private final static DateFormat dfZoned = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	private final static DateFormat dfNoZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private final static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	@PostConstruct
	void init() {
		importCacheSize = propertyHandler.getImportCacheSize();
		rootUserNames = propertyHandler.getRootUserNames();
	}

	private long importCacheSize;

	public static Date getDate(String fullString) throws IcatException {

		Matcher m = tsRegExp.matcher(fullString);
		if (m.matches()) {
			Date date;
			synchronized (dfZoned) {
				try {
					if (m.group(3).isEmpty()) {
						date = dfNoZone.parse(m.group(1));
					} else {
						date = dfZoned.parse(m.group(1) + m.group(3));
					}

				} catch (ParseException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, "Unable to parse "
							+ fullString);
				}
			}
			if (m.group(2) != null && m.group(2).length() > 1) {
				int millis = Integer.parseInt((m.group(2) + "00").substring(1, 4));
				date = new Date(date.getTime() + millis);
			}
			return date;
		} else {
			throw new IcatException(IcatExceptionType.INTERNAL, "Unable to parse " + fullString);
		}

	}

	public void importData(String jsonString, InputStream body, EntityManager manager,
			UserTransaction userTransaction) throws IcatException {

		@SuppressWarnings("serial")
		LinkedHashMap<String, EntityBaseBean> jpqlCache = new LinkedHashMap<String, EntityBaseBean>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, EntityBaseBean> eldest) {
				return size() > importCacheSize;
			}
		};

		@SuppressWarnings("serial")
		LinkedHashMap<Long, EntityBaseBean> idCache = new LinkedHashMap<Long, EntityBaseBean>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, EntityBaseBean> eldest) {
				return size() > importCacheSize;
			}
		};

		logger.debug("JSON " + jsonString);
		if (jsonString == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "json must not be null");
		}
		String sessionId = null;
		DuplicateAction duplicateAction = DuplicateAction.THROW;
		Attributes attributes = Attributes.USER;
		try (JsonParser parser = Json.createParser(new ByteArrayInputStream(jsonString.getBytes()))) {
			String key = null;
			while (parser.hasNext()) {
				JsonParser.Event event = parser.next();
				if (event == Event.KEY_NAME) {
					key = parser.getString();
				} else if (event == Event.VALUE_STRING || event == Event.VALUE_NUMBER) {
					if (key.equals("sessionId")) {
						sessionId = parser.getString();
					} else if (key.equals("duplicate")) {
						try {
							duplicateAction = DuplicateAction.valueOf(parser.getString()
									.toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									parser.getString() + " is not a valid value for 'duplicate'");
						}
					} else if (key.equals("attributes")) {
						try {
							attributes = Attributes.valueOf(parser.getString().toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									parser.getString() + " is not a valid value for 'attributes'");
						}
					} else {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, key
								+ " is not an expected key in the json");
					}
				}
			}
		}

		String userId = getUserName(sessionId, manager);
		boolean allAttributes = attributes == Attributes.ALL;
		if (allAttributes && !rootUserNames.contains(userId)) {
			throw new IcatException(IcatExceptionType.INSUFFICIENT_PRIVILEGES,
					"Only root users may import with Attributes.ALL");
		}

		Map<String, Long> ids = new HashMap<>();
		int linum = 0;

		try (BufferedReader data = new BufferedReader(new InputStreamReader(body))) {

			// Get the version
			String version = null;
			String line;

			while ((line = data.readLine()) != null) {
				linum++;
				line = line.trim();
				if (!line.startsWith("#") && !line.isEmpty()) {
					version = line;
					logger.debug("File version: " + version);
					break;
				}
			}
			if (version == null) {
				throw new IcatException(IcatExceptionType.VALIDATION,
						"No version of file encountered");
			}
			if (!version.equals("1.0")) {
				throw new IcatException(IcatExceptionType.VALIDATION, "Version of file must be 1.0");
			}

			Table table = null;
			while ((line = data.readLine()) != null) {
				linum++;
				line = line.trim();
				if (line.startsWith("#")) {
					// do nothing
				} else if (line.isEmpty()) {
					if (table != null) {
						logger.debug("Ending import of " + table.getName());
					}
					table = null;
				} else if (table == null) {
					table = processTableHeader(line);
				} else {
					processTuple(table, line, userId, jpqlCache, ids, idCache, manager,
							userTransaction, duplicateAction, attributes, allAttributes);
				}
			}
			if (table != null) {
				logger.debug("Ending import of " + table.getName());
			}

		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.VALIDATION, e.getClass() + " "
					+ e.getMessage(), linum);
		} catch (IcatException e) {
			throw new IcatException(e.getType(), e.getMessage() + " at line " + linum, linum);
		} catch (LexerException | ParserException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage()
					+ " at line " + linum, linum);
		} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass()
					.getSimpleName() + " " + e.getMessage() + " at line " + linum, linum);
		}
	}

	private Session getSession(String sessionId, EntityManager manager) throws IcatException {
		Session session = null;
		if (sessionId == null || sessionId.equals("")) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Session Id cannot be null or empty.");
		}
		session = manager.find(Session.class, sessionId);
		if (session == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Unable to find user by sessionid: " + sessionId);
		}
		return session;
	}

	private String getUserName(String sessionId, EntityManager manager) throws IcatException {
		try {
			Session session = getSession(sessionId, manager);
			String userName = session.getUserName();
			logger.debug("user: " + userName + " is associated with: " + sessionId);
			return userName;
		} catch (IcatException e) {
			logger.debug("sessionId " + sessionId + " is not associated with valid session "
					+ e.getMessage());
			throw e;
		}
	}

	private void processTuple(Table table, String line, String userId,
			Map<String, EntityBaseBean> cache, Map<String, Long> ids,
			LinkedHashMap<Long, EntityBaseBean> idCache, EntityManager manager,
			UserTransaction userTransaction, DuplicateAction duplicateAction,
			Attributes attributes, boolean allAttributes) throws IcatException, LexerException,
			ParserException, IllegalArgumentException, InvocationTargetException,
			IllegalAccessException {
		logger.debug("Requested add " + line + " to " + table.getName());
		Input input = new Input(Tokenizer.getTokens(line));
		List<TableField> tableFields = table.getTableFields();
		EntityBaseBean bean = table.createEntity();
		Token token;
		List<Token> tokens = new ArrayList<>();
		while ((token = input.consume()) != null) {
			tokens.add(token);
			if (input.peek(0) != null) {
				input.consume(Token.Type.COMMA);
			}
		}
		String save = null;
		for (TableField tableField : tableFields) {
			Field f = tableField.getField();
			Method setter = tableField.getSetter();
			Integer offset = tableField.getOffset();
			if (offset != null) {
				token = tokens.get(offset);
				if (token == null) {
					throw new ParserException("Data column " + offset + " does not exist");
				}
				Type tType = token.getType();
				if (f != null) {
					String fType = f.getType().getSimpleName();

					if (tType == Token.Type.NULL) {
						// do nothing
					} else if (fType.equals("String")) {
						if (tType == Token.Type.STRING) {
							setter.invoke(bean, token.getValue());
						} else {
							throw new ParserException("Expected a String value for column "
									+ offset);
						}
					} else if (fType.equals("Integer")) {
						if (tType == Token.Type.INTEGER) {
							setter.invoke(bean, Integer.parseInt(token.getValue()));
						} else {
							throw new ParserException("Expected an integer value for column "
									+ offset);
						}
					} else if (fType.equals("boolean")) {
						if (tType == Token.Type.BOOLEAN) {
							setter.invoke(bean, Boolean.parseBoolean(token.getValue()));
						} else {
							throw new ParserException("Expected an boolean value for column "
									+ offset);
						}
					} else if (fType.equals("Date")) {
						if (tType == Token.Type.TIMESTAMP) {
							setter.invoke(bean, getDate(token.getValue()));
						} else {
							throw new ParserException("Expected an date value for column " + offset);
						}
					} else if (fType.equals("Double")) {
						if (tType == Token.Type.REAL || tType == Token.Type.INTEGER) {
							setter.invoke(bean, Double.parseDouble((token.getValue())));
						} else {
							throw new ParserException(
									"Expected an real or integer value for column " + offset);
						}
					} else if (fType.equals("Long")) {
						if (tType == Token.Type.INTEGER) {
							setter.invoke(bean, Long.parseLong((token.getValue())));
						} else {
							throw new ParserException("Expected an integer value for column "
									+ offset);
						}
					} else if (fType.equals("ParameterValueType")) {
						if (tType == Token.Type.NAME) {
							setter.invoke(bean,
									ParameterValueType.valueOf(token.getValue().toUpperCase()));
						} else {
							throw new ParserException("Expected a " + ParameterValueType.values()
									+ " value for column " + offset);
						}
					} else if (tableField.isQmark()) {
						Long id = ids.get(fType + "." + token.getValue());
						if (id == null) {
							throw new ParserException("? field '" + token.getValue()
									+ "' not defined yet");
						}
						EntityBaseBean eb = idCache.get(id);
						if (eb == null) {
							eb = (EntityBaseBean) manager.find(f.getType(), id);
							if (eb == null) {
								throw new ParserException("? field '" + token.getValue()
										+ "' => id " + id + " no longer exists");
							}
							idCache.put(id, eb);
						}
						setter.invoke(bean, eb);
					} else {
						throw new IcatException(IcatExceptionType.INTERNAL,
								"Don't know how to process " + fType);
					}
				} else {
					if (tType == Token.Type.STRING) {
						save = token.getValue();
					} else {
						throw new ParserException("Expected a String value for column " + offset);
					}
				}
			} else { // Not all types are considered here - only those used in "keys"
				String jpql = tableField.getJPQL();
				StringBuilder sb = new StringBuilder(jpql);
				boolean nullRef = false;
				for (Attribute attribute : tableField.getAttributes()) {
					int n = attribute.getFieldNum();
					token = tokens.get(n);
					if (token == null) {
						throw new ParserException("Data column " + n + " does not exist");
					}
					if (token.getType() == Token.Type.NULL) {
						nullRef = true;
						break;
					}
					sb.append(" " + n + " " + token.getValue());
				}
				if (!nullRef) {
					String key = sb.toString();
					EntityBaseBean eb = cache.get(key);
					if (eb == null) {
						logger.debug("'" + key + "' not found in import cache");
						TypedQuery<EntityBaseBean> query = manager.createQuery(jpql,
								EntityBaseBean.class);
						for (Attribute attribute : tableField.getAttributes()) {
							int n = attribute.getFieldNum();
							token = tokens.get(n);
							Type tType = token.getType();
							String fType = attribute.getType();

							if (fType.equals("String")) {
								if (tType == Token.Type.STRING) {
									query.setParameter("p" + n, token.getValue());
									logger.debug("Setting " + n + " to " + token.getValue());
								} else {
									throw new ParserException("Expected a String value for column "
											+ n);
								}
							} else if (fType.equals("Integer")) {
								if (tType == Token.Type.INTEGER) {
									query.setParameter("p" + n, Integer.parseInt(token.getValue()));
									logger.debug("Setting " + n + " to "
											+ query.getParameter("p" + n));
								} else {
									throw new ParserException(
											"Expected an integer value for column " + n);
								}
							} else {
								throw new IcatException(IcatExceptionType.INTERNAL,
										"Don't know how to process " + fType
												+ " as selection attribute");
							}
						}
						try {
							eb = query.getSingleResult();
							cache.put(key, eb);
						} catch (NoResultException e) {
							throw new IcatException(IcatExceptionType.NO_SUCH_OBJECT_FOUND,
									"Import failed when looking up existing object for attribute "
											+ f.getName());
						} catch (NonUniqueResultException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									"Import failed with multiple results when looking up existing object for attribute "
											+ f.getName());
						}
					}
					setter.invoke(bean, eb);
				}
			}
		}
		boolean createTimeSet = bean.getCreateTime() != null;
		boolean modTimeSet = bean.getModTime() != null;
		boolean createIdSet = bean.getCreateId() != null;
		boolean modIdSet = bean.getModId() != null;
		Long id = null;
		try {
			id = beanManager.create(userId, bean, manager, userTransaction, allAttributes).getPk();
		} catch (IcatException e) {
			if (e.getType() == IcatExceptionType.OBJECT_ALREADY_EXISTS) {
				if (duplicateAction == DuplicateAction.IGNORE) {
					logger.debug("Adding " + line + " to " + table.getName()
							+ " gives duplicate exception but DuplicateAction is IGNORE");
					return;
				} else if (duplicateAction == DuplicateAction.CHECK) {
					EntityBaseBean other = beanManager.lookup(bean, manager);
					if (other == null) {// Somebody else got rid of it meanwhile
						id = beanManager.create(userId, bean, manager, userTransaction, false)
								.getPk();
						logger.debug("Adding " + line + " to " + table.getName()
								+ " gives duplicate exception but it has now vanished");
					} else { // Compare bean and other
						if (allAttributes) {
							if (createIdSet && !bean.getCreateId().equals(other.getCreateId())) {
								throw new IcatException(IcatExceptionType.VALIDATION,
										"Duplicate check fails for field \"createId\" of "
												+ table.getName());
							}
							if (createTimeSet
									&& Math.abs(bean.getCreateTime().getTime()
											- other.getCreateTime().getTime()) > 1000) {
								throw new IcatException(IcatExceptionType.VALIDATION,
										"Duplicate check fails for field \"createTime\" of "
												+ table.getName());
							}
							if (modIdSet && !bean.getModId().equals(other.getModId())) {
								throw new IcatException(IcatExceptionType.VALIDATION,
										"Duplicate check fails for field \"modId\" of "
												+ table.getName());
							}
							if (modTimeSet
									&& Math.abs(bean.getModTime().getTime()
											- other.getModTime().getTime()) > 1000) {
								throw new IcatException(IcatExceptionType.VALIDATION,
										"Duplicate check fails for field \"modTime\" of "
												+ table.getName());
							}

						}
						Class<? extends EntityBaseBean> klass = bean.getClass();
						Map<Field, Method> getters = eiHandler.getGetters(klass);
						Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();
						for (Field f : eiHandler.getFields(klass)) {
							if (updaters.contains(f)) {
								if (EntityBaseBean.class.isAssignableFrom(f.getType())) {
									EntityBaseBean beanField = (EntityBaseBean) getters.get(f)
											.invoke(bean);
									EntityBaseBean otherField = (EntityBaseBean) getters.get(f)
											.invoke(other);
									if (beanField == null) {
										if (otherField != null) {
											throw new IcatException(IcatExceptionType.VALIDATION,
													"Duplicate check fails for field "
															+ f.getName() + " of "
															+ f.getDeclaringClass().getSimpleName());
										}
									} else { // beanField is not null
										if (otherField == null) {
											throw new IcatException(IcatExceptionType.VALIDATION,
													"Duplicate check fails for field "
															+ f.getName() + " of "
															+ f.getDeclaringClass().getSimpleName());
										} // both not null
										if (beanField.getId().longValue() != otherField.getId()
												.longValue()) {
											throw new IcatException(IcatExceptionType.VALIDATION,
													"Duplicate check fails for field "
															+ f.getName() + " of "
															+ f.getDeclaringClass().getSimpleName());
										}
									}
								} else {
									Object beanField = getters.get(f).invoke(bean);
									Object otherField = getters.get(f).invoke(other);
									if (beanField == null) {
										if (otherField != null) {
											throw new IcatException(IcatExceptionType.VALIDATION,
													"Duplicate check fails for field "
															+ f.getName() + " of "
															+ f.getDeclaringClass().getSimpleName());
										}
									} else {// beanField is not null
										if (beanField instanceof Date) { // Milliseconds get lost
											if (Math.abs(((Date) beanField).getTime()
													- ((Date) otherField).getTime()) > 1000) {
												throw new IcatException(
														IcatExceptionType.VALIDATION,
														"Duplicate check fails for field "
																+ f.getName()
																+ " of "
																+ f.getDeclaringClass()
																		.getSimpleName());
											}
										} else {
											if (!beanField.equals(otherField)) {
												throw new IcatException(
														IcatExceptionType.VALIDATION,
														"Duplicate check fails for field "
																+ f.getName()
																+ " of "
																+ f.getDeclaringClass()
																		.getSimpleName());
											}
										}
									}

								}
							}
						}
						logger.debug("Adding " + line + " to " + table.getName()
								+ " gives duplicate exception but DuplicateAction is CHECK");
					}

				} else if (duplicateAction == DuplicateAction.OVERWRITE) {
					EntityBaseBean other = beanManager.lookup(bean, manager);
					if (other == null) {// Somebody else got rid of it meanwhile
						id = beanManager.create(userId, bean, manager, userTransaction, false)
								.getPk();
						logger.debug("Adding " + line + " to " + table.getName()
								+ " gives duplicate exception but it has now vanished");
					} else {
						id = other.getId();
						bean.setId(id);
						beanManager.update(userId, bean, manager, userTransaction, allAttributes);
						logger.debug("Adding " + line + " to " + table.getName()
								+ " gives duplicate exception but DuplicateAction is OVERWRITE");
					}
				} else {
					throw e;
				}
			}
		}
		if (save != null && id != null) {
			bean.setId(id);
			ids.put(table.getName() + "." + save, id);
			idCache.put(id, bean);
			logger.debug("Saved " + id + " as " + table.getName() + "." + save
					+ " for import lookup");
		}
	}

	private Table processTableHeader(String header) throws IcatException, LexerException,
			ParserException {
		logger.debug("Process import table header " + header);
		List<Token> tokens = null;
		tokens = Tokenizer.getTokens(header);
		Input input = new Input(tokens);
		Table table = new Table(input);
		input.checkEnded();
		return table;
	}

	public Response exportData(String jsonString, EntityManager manager,
			UserTransaction userTransaction) throws IcatException {
		if (jsonString == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "json must not be null");
		}
		String sessionId = null;
		String query = null;
		ExportType exportType = ExportType.QUERY;
		Attributes attributes = Attributes.USER;
		try (JsonParser parser = Json.createParser(new ByteArrayInputStream(jsonString.getBytes()))) {
			String key = null;
			while (parser.hasNext()) {
				JsonParser.Event event = parser.next();
				if (event == Event.KEY_NAME) {
					key = parser.getString();
				} else if (event == Event.VALUE_STRING || event == Event.VALUE_NUMBER) {
					if (key.equals("sessionId")) {
						sessionId = parser.getString();
					} else if (key.equals("query")) {
						query = parser.getString();
					} else if (key.equals("type")) {
						try {
							exportType = ExportType.valueOf(parser.getString().toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									parser.getString() + " is not a valid value for 'type'");
						}
					} else if (key.equals("attributes")) {
						try {
							attributes = Attributes.valueOf(parser.getString().toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									parser.getString() + " is not a valid value for 'attributes'");
						}
					} else {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, key
								+ " is not an expected key in the json");
					}
				}
			}
		}
		logger.debug(sessionId + " issues " + query);
		String userId = getUserName(sessionId, manager);
		if (exportType == ExportType.QUERY) {
			return beanManager.export(userId, query, attributes == Attributes.ALL, manager,
					userTransaction);
		} else {
			return beanManager.export(userId, attributes == Attributes.ALL, manager,
					userTransaction);
		}
	}

}
