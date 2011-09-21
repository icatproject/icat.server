package uk.icat3.sessionbeans.search;

import java.util.List;

import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.Search;
import uk.icat3.sessionbeans.EJBObject;

@Stateless
public class SearchBean extends EJBObject implements SearchLocal {

	static Logger logger = Logger.getLogger(SearchBean.class);

	@Override
	public List<?> search(String sessionId, String query) throws SessionException, IcatInternalException,
			BadParameterException, InsufficientPrivilegesException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			return Search.search(userId, query, manager);
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (BadParameterException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

}
