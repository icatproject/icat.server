package uk.icat3.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.AccessType;
import uk.icat3.security.EntityInfoHandler;
import uk.icat3.security.EntityInfoHandler.Relationship;
import uk.icat3.security.GateKeeper;
import uk.icat3.security.parser.GetQuery;
import uk.icat3.security.parser.Input;
import uk.icat3.security.parser.LexerException;
import uk.icat3.security.parser.ParserException;
import uk.icat3.security.parser.SearchQuery;
import uk.icat3.security.parser.Token;
import uk.icat3.security.parser.Tokenizer;

public class BeanManager {

	// Global class logger
	private static final Logger logger = Logger.getLogger(BeanManager.class);
	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();
	private static DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	private static final Pattern timestampPattern = Pattern.compile(":ts(\\d{14})");

	public static CreateResponse create(String userId, EntityBaseBean bean, EntityManager manager)
			throws InsufficientPrivilegesException, ObjectAlreadyExistsException, ValidationException,
			NoSuchObjectFoundException, IcatInternalException, BadParameterException {
		// TODO this code can throw a primary key exception if the same object
		// is being created by two threads. Probably need to use a Bean Managed
		// Transaction to avoid the problem.

		bean.preparePersist(userId, manager);
		bean.isUnique(manager);

		try {
			logger.trace(bean + " prepared for persist.");
			manager.persist(bean);
			logger.trace(bean + " persisted.");
			manager.flush();
			logger.trace(bean + " flushed.");
		} catch (EntityExistsException e) {
			throw new ObjectAlreadyExistsException(e.getMessage());
		} catch (Throwable e) {
			manager.clear();
			bean.preparePersist(userId, manager);
			bean.isValid(manager, true);
			e.printStackTrace(System.err);
			throw new IcatInternalException("Unexpected DB response " + e.getMessage());
		}
		// Check authz now everything persisted
		GateKeeper.performAuthorisation(userId, bean, AccessType.CREATE, manager);
		NotificationMessages notification = new NotificationMessages(userId, bean, AccessType.CREATE, manager);
		return new CreateResponse(bean.getPK(), notification);
	}

	public static NotificationMessages delete(String userId, EntityBaseBean bean, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException,
			IcatInternalException {
		EntityBaseBean beanManaged = find(bean, manager);
		GateKeeper.performAuthorisation(userId, beanManaged, AccessType.DELETE, manager);
		beanManaged.canDelete(manager);
		NotificationMessages notification = new NotificationMessages(userId, bean, AccessType.DELETE, manager);
		try {
			manager.remove(beanManaged);
			manager.flush();
			logger.trace("Deleted bean " + bean + " flushed.");
		} catch (Throwable e) {
			manager.clear();
			throw new IcatInternalException("Unexpected DB response " + e.getMessage());
		}
		return notification;

	}

	public static NotificationMessages update(String userId, EntityBaseBean bean, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException,
			IcatInternalException, BadParameterException {
		EntityBaseBean beanManaged = find(bean, manager);
		GateKeeper.performAuthorisation(userId, beanManaged, AccessType.UPDATE, manager);
		beanManaged.setModId(userId);
		beanManaged.merge(bean, manager);
		try {
			manager.flush();
			logger.trace("Updated bean " + bean + " flushed.");
		} catch (Throwable e) {
			manager.clear();
			bean.isValid(manager, false);
			throw new IcatInternalException("Unexpected DB response " + e);
		}
		NotificationMessages notification = new NotificationMessages(userId, bean, AccessType.UPDATE, manager);
		return notification;
	}

	public static GetResponse get(String userId, String query, Object primaryKey, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, BadParameterException,
			IcatInternalException {

		List<Token> tokens = null;
		try {
			tokens = Tokenizer.getTokens(query);
		} catch (LexerException e) {
			throw new BadParameterException(e.getMessage());
		}
		Input input = new Input(tokens);
		GetQuery q;
		try {
			q = new GetQuery(input);
		} catch (ParserException e) {
			throw new BadParameterException(e.getMessage());
		}

		Class<? extends EntityBaseBean> entityClass = q.getFirstEntity();
		if (primaryKey == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + " has null primary key.");
		}
		EntityBaseBean beanManaged = manager.find(entityClass, primaryKey);
		if (beanManaged == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + " [id:" + primaryKey + "] not found.");
		}

		Set<Class<? extends EntityBaseBean>> includes = q.getIncludes();
		if (includes.size() > 0) {
			beanManaged.addIncludes(includes);
		}

		GateKeeper.performAuthorisation(userId, beanManaged, AccessType.READ, manager);
		logger.debug("got " + entityClass.getSimpleName() + "[id:" + primaryKey + "]");
		NotificationMessages notification = new NotificationMessages(userId, beanManaged, AccessType.READ, manager);
		return new GetResponse(beanManaged, notification);
	}

	public static SearchResponse search(String userId, String query, EntityManager manager)
			throws BadParameterException, IcatInternalException, InsufficientPrivilegesException {

		logger.debug(userId + " searches for " + query);

		/* Parse the query */
		List<Token> tokens = null;
		try {
			tokens = Tokenizer.getTokens(query);
		} catch (LexerException e) {
			throw new BadParameterException(e.getMessage());
		}
		Input input = new Input(tokens);
		SearchQuery q;
		try {
			q = new SearchQuery(input);
		} catch (ParserException e) {
			throw new BadParameterException(e.getMessage());
		}

		/* Get the JPQL which includes authz restrictions */
		String jpql = q.getJPQL(userId, manager);
		logger.debug("JPQL: " + jpql);

		/* Create query and add parameter values for any timestamps */
		Matcher m = timestampPattern.matcher(jpql);
		javax.persistence.Query jpqlQuery = manager.createQuery(jpql);
		while (m.find()) {
			Date d = null;
			try {
				d = df.parse(m.group(1));
			} catch (ParseException e) {
				// This cannot happen - honest
			}
			jpqlQuery.setParameter("ts" + m.group(1), d);
		}

		Integer offset = q.getOffset();
		if (offset != null) {
			jpqlQuery.setFirstResult(offset);
		}
		Integer number = q.getNumber();
		if (number != null) {
			jpqlQuery.setMaxResults(number);
		}

		List<?> result = jpqlQuery.getResultList();

		Set<Class<? extends EntityBaseBean>> includes = q.getIncludes();
		if (includes.size() > 0) {
			for (Object beanManaged : result) {
				((EntityBaseBean) beanManaged).addIncludes(includes);
			}
		}

		logger.debug("Obtained " + result.size() + " results.");
		NotificationMessages nms = new NotificationMessages(userId, result.size(), q.getFirstEntity(), query, manager);
		return new SearchResponse(result, nms);
	}

	private static EntityBaseBean find(EntityBaseBean bean, EntityManager manager) throws NoSuchObjectFoundException,
			IcatInternalException {
		Object primaryKey = bean.getPK();
		Class<? extends EntityBaseBean> entityClass = bean.getClass();
		if (primaryKey == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + " has null primary key.");
		}
		EntityBaseBean object = null;
		try {
			object = manager.find(entityClass, primaryKey);
		} catch (Throwable e) {
			throw new IcatInternalException("Unexpected DB response " + e);
		}

		if (object == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}
		return object;
	}

	// This code might be in EntityBaseBean however this would mean that it
	// would be processed by JPA which gets confused by it.
	public static void merge(EntityBaseBean thisBean, Object fromBean, EntityManager manager)
			throws IcatInternalException {
		Class<? extends EntityBaseBean> klass = thisBean.getClass();
		Map<Field, Method> setters = eiHandler.getSetters(klass);
		Map<Field, Method> getters = eiHandler.getGetters(klass);

		for (Entry<Field, Method> fieldAndMethod : setters.entrySet()) {
			Field field = fieldAndMethod.getKey();
			try {
				Method m = getters.get(field);
				Object value = m.invoke(fromBean, new Object[0]);
				if (EntityBaseBean.class.isAssignableFrom(field.getType())) {
					logger.debug("Needs special processing as " + value + " is a bean");
					if (value != null) {
						Object pk = ((EntityBaseBean) value).getPK();
						logger.debug("PK is " + pk);
						value = (EntityBaseBean) manager.find(field.getType(), pk);
						fieldAndMethod.getValue().invoke(thisBean, new Object[] { value });
					} else {
						fieldAndMethod.getValue().invoke(thisBean, (EntityBaseBean) null);
					}
				} else {
					fieldAndMethod.getValue().invoke(thisBean, new Object[] { value });
				}
				logger.trace("Updated " + klass.getSimpleName() + "." + field.getName() + " to " + value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IcatInternalException("" + e);
			}
		}

	}

	// This code might be in EntityBaseBean however this would mean that it
	// would be processed by JPA which gets confused by it.
	@SuppressWarnings("unchecked")
	public static void addIncludes(EntityBaseBean thisBean, Set<Class<? extends EntityBaseBean>> requestedIncludes)
			throws IcatInternalException {
		logger.debug("addIncludes " + requestedIncludes + " for " + thisBean);
		Class<? extends EntityBaseBean> entityClass = thisBean.getClass();
		Set<Relationship> relationships = eiHandler.getRelatedEntities(entityClass);
		for (Relationship r : relationships) {
			Class<? extends EntityBaseBean> bean = r.getBean();
			if (requestedIncludes.contains(bean)) {

				// Mark as wanted
				thisBean.getIncludes().add(bean);

				// Avoid looping forever
				HashSet<Class<? extends EntityBaseBean>> includeReduced = new HashSet<Class<? extends EntityBaseBean>>(
						requestedIncludes);
				includeReduced.remove(bean);

				// Recurse into collection or single object
				Map<Field, Method> getters = eiHandler.getGetters(thisBean.getClass());

				if (r.isCollection()) {
					Collection<EntityBaseBean> collection = null;
					Field field = r.getField();
					try {
						collection = (Collection<EntityBaseBean>) getters.get(field).invoke(thisBean, (Object[]) null);
					} catch (Exception e) {
						throw new IcatInternalException(e.toString());
					}
					for (EntityBaseBean b : collection) {
						b.addIncludes(includeReduced);
					}
				} else {
					EntityBaseBean b = null;
					Field field = r.getField();
					try {
						b = (EntityBaseBean) getters.get(field).invoke(thisBean, (Object[]) null);
					} catch (Exception e) {
						throw new IcatInternalException(e.toString());
					}
					if (b != null) {
						b.addIncludes(includeReduced);
					}
				}
			}
		}

	}
}
