package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import uk.icat3.entity.Rule;
import uk.icat3.entity.UserGroup;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.SessionException;

@Local
public interface RuleManagerLocal {

	public void addUserGroupMember(String sessionId, String name, String member) throws ObjectAlreadyExistsException,
			SessionException;

	public void removeUserGroupMember(String sessionId, String name, String member) throws NoSuchObjectFoundException,
			SessionException;

	public long addRule(String sessionId, String groupName, String what, String crud, String restriction)
			throws BadParameterException, IcatInternalException, SessionException;

	public void removeRule(String sessionId, long id) throws NoSuchObjectFoundException, SessionException;

	public List<Rule> listRules(String sessionId) throws SessionException;

	public Collection<UserGroup> listUserGroups(String sessionId) throws SessionException;

}
