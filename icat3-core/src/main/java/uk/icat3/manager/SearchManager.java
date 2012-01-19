package uk.icat3.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.security.parser.Input;
import uk.icat3.security.parser.LexerException;
import uk.icat3.security.parser.ParserException;
import uk.icat3.security.parser.SearchQuery;
import uk.icat3.security.parser.Token;
import uk.icat3.security.parser.Tokenizer;
import uk.icat3.util.AccessType;

public class SearchManager {

	private static DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	static Logger logger = Logger.getLogger(SearchManager.class);
	private static final Pattern timestampPattern = Pattern.compile(":ts(\\d{14})");

	public static SearchResponse search(String userId, String query, EntityManager manager) throws BadParameterException,
			IcatInternalException, InsufficientPrivilegesException {

		SearchManager.logger.debug(userId + " searches for " + query);

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
		SearchManager.logger.debug("JPQL: " + jpql);

		/* Create query and add parameter values for any timestamps */
		Matcher m = SearchManager.timestampPattern.matcher(jpql);
		javax.persistence.Query jpqlQuery = manager.createQuery(jpql);
		while (m.find()) {
			Date d = null;
			try {
				d = SearchManager.df.parse(m.group(1));
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

		SearchManager.logger.debug("Obtained " + result.size() + " results.");
		NotificationMessages nms = new NotificationMessages(userId, result.size(), q.getFirstEntity(), query, manager);
		return new SearchResponse(result, nms);
	}
}
