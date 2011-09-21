package uk.icat3.sessionbeans.manager;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;

import org.apache.log4j.Logger;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Facility;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.BeanManager;
import uk.icat3.sessionbeans.EJBObject;

@Stateless()
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class BeanManagerBean extends EJBObject implements BeanManagerLocal {
    
    static Logger logger = Logger.getLogger(DatasetManagerBean.class);
    
    public BeanManagerBean() {}
    
	@WebMethod
	public Object create(String sessionId, EntityBaseBean bean) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException,
			ObjectAlreadyExistsException, IcatInternalException {
		try {
			String userId = user.getUserIdFromSessionId(sessionId);
			return BeanManager.create(userId, bean, manager);
		} catch (SessionException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (InsufficientPrivilegesException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (NoSuchObjectFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ValidationException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (ObjectAlreadyExistsException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (IcatInternalException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatInternalException(e.getMessage());
		}
	}

	@WebMethod()
	public void delete(String sessionId, EntityBaseBean bean) throws SessionException,
			NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, IcatInternalException {
		String userId = user.getUserIdFromSessionId(sessionId);
		BeanManager.delete(userId, bean, manager);
	}

	@WebMethod()
	public void update(String sessionId,  EntityBaseBean bean) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException, IcatInternalException {
		String userId = user.getUserIdFromSessionId(sessionId);
		BeanManager.update(userId, bean, manager);
	}

	@Override
	public void dummy(Facility facility) {
		// Do nothing
	}

	@Override
	public EntityBaseBean get(String sessionId, String query, Object primaryKey) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException, BadParameterException, IcatInternalException {
		String userId = user.getUserIdFromSessionId(sessionId);
		return BeanManager.get(userId, query, primaryKey, manager);
	}
    
}
