package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import uk.icat3.entity.Rule;
import uk.icat3.entity.UserGroup;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.RuleManager;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.sessionbeans.user.UserSessionBean;
import uk.icat3.sessionbeans.user.UserSessionLocal;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RuleManagerBean extends EJBObject implements RuleManagerLocal {

	@EJB
	private UserSessionLocal user;

	@Override
	public void addUserGroupMember(String sessionId, String name, String member) throws ObjectAlreadyExistsException,
			SessionException {
		checkAdmin(sessionId);
		RuleManager.addUserGroupMember(name, member, manager);
	}

	private void checkAdmin(String sessionId) throws SessionException {
		if (!UserSessionBean.ADMIN.equals(user.getUserIdFromSessionId(sessionId))) {
			throw new SessionException("You must be logged in as " + UserSessionBean.ADMIN + " to do this");
		}
	}

	@Override
	public void removeUserGroupMember(String sessionId, String name, String member) throws NoSuchObjectFoundException,
			SessionException {
		checkAdmin(sessionId);
		RuleManager.removeUserGroupMember(name, member, manager);
	}

	@Override
	public long addRule(String sessionId, String groupName, String what, String crud, String restriction)
			throws BadParameterException, IcatInternalException, SessionException {
		checkAdmin(sessionId);
		return RuleManager.addRule(groupName, what, crud, restriction, manager);
	}

	@Override
	public void removeRule(String sessionId, long id) throws NoSuchObjectFoundException, SessionException {
		checkAdmin(sessionId);
		RuleManager.removeRule(id, manager);
	}

	@Override
	public List<Rule> listRules(String sessionId) throws SessionException {
		checkAdmin(sessionId);
		return RuleManager.listRules(manager);
	}

	@Override
	public Collection<UserGroup> listUserGroups(String sessionId) throws SessionException {
		checkAdmin(sessionId);
		return RuleManager.listUserGroups(manager);
	}
}
