package uk.icat3.util;

import javax.persistence.EntityManager;

import uk.icat3.entity.Group;
import uk.icat3.entity.Rule;
import uk.icat3.entity.User;
import uk.icat3.entity.UserGroup;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.BeanManager;

public class RuleManager {

	public static void oldAddUserGroupMember(String groupName, String userName, EntityManager em)
			throws InsufficientPrivilegesException, ObjectAlreadyExistsException, ValidationException,
			NoSuchObjectFoundException, IcatInternalException, BadParameterException {
		addUserGroupMember("root", groupName, userName, em);
	}

	public static void oldAddRule(String groupName, String beanName, String crudFlags, String restriction,
			EntityManager em) throws InsufficientPrivilegesException, ObjectAlreadyExistsException,
			ValidationException, NoSuchObjectFoundException, IcatInternalException, BadParameterException {
		if (restriction != null) {
			addRule("root", groupName, beanName + " " + restriction, crudFlags, em);
		} else {
			addRule("root", groupName, beanName, crudFlags, em);
		}
	}

	public static void addRule(String userid, String groupName, String what, String crudFlags, EntityManager em)
			throws InsufficientPrivilegesException, ObjectAlreadyExistsException, ValidationException,
			NoSuchObjectFoundException, IcatInternalException, BadParameterException {
		Rule rule = new Rule();
		if (groupName != null) {
			Group g = (Group) BeanManager.get(userid, "Group", groupName, em).getBean();
			rule.setGroup(g);
		}
		rule.setWhat(what);
		rule.setCrudFlags(crudFlags);
		BeanManager.create(userid, rule, em);
	}

	public static void addUserGroupMember(String userid, String groupName, String userName, EntityManager em)
			throws InsufficientPrivilegesException, ObjectAlreadyExistsException, ValidationException,
			NoSuchObjectFoundException, IcatInternalException, BadParameterException {
		Group group = null;
		try {
			group = (Group) BeanManager.get(userid, "Group", groupName, em).getBean();
		} catch (NoSuchObjectFoundException e) {
			group = new Group();
			group.setName(groupName);
			BeanManager.create(userid, group, em);
		}
		User user = null;
		try {
			user = (User) BeanManager.get(userid, "User", groupName, em).getBean();
		} catch (NoSuchObjectFoundException e) {
			user = new User();
			user.setName(userName);
			BeanManager.create(userid, user, em);
		}
		UserGroup userGroup = new UserGroup();
		userGroup.setUser(user);
		userGroup.setGroup(group);
		BeanManager.create(userid, userGroup, em);
	}
}
