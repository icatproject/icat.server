package uk.icat3.manager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import uk.icat3.entity.Rule;
import uk.icat3.entity.UserGroup;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;

/** As rules and user groups are not beans this does not make use of the generic manager code */
public class RuleManager {
	public static void addUserGroupMember(String name, String member, EntityManager em)
			throws ObjectAlreadyExistsException {

		TypedQuery<UserGroup> query = em.createNamedQuery(UserGroup.PK, UserGroup.class).setParameter("name", name).setParameter("member", member);
		if (query.getResultList().size() != 0) {
			throw new ObjectAlreadyExistsException("UserGoup (" + name + ", " + member + ")");
		}
		UserGroup ug = new UserGroup(name, member);
		em.persist(ug);
	}

	public static void removeUserGroupMember(String name, String member, EntityManager em)
			throws NoSuchObjectFoundException {
		TypedQuery<UserGroup> query = em.createNamedQuery(UserGroup.PK, UserGroup.class).setParameter("name", name).setParameter("member", member);
		List<UserGroup> ugs = query.getResultList();
		if (ugs.size() != 1) {
			throw new NoSuchObjectFoundException("UserGoup (" + name + ", " + member + ")");
		}
		em.remove(ugs.get(0));
	}

	public static List<UserGroup> listUserGroups(EntityManager em) {
		return em.createNamedQuery(UserGroup.ALL, UserGroup.class).getResultList();

	}

	public static long addRule(String groupName, String what, String crud, String restriction,
			EntityManager em) throws BadParameterException, IcatInternalException {
		Rule r = new Rule(groupName, what, crud, restriction);
		String query = r.getCrudJPQL();
		if (query != null) {
			try {
				em.createQuery(query);
			} catch (IllegalArgumentException e) {
				throw new BadParameterException(e.getMessage());
			}
		}
		em.persist(r);
		return r.getId();
	}

	public static void removeRule(long id, EntityManager em) throws NoSuchObjectFoundException {
		Rule r = em.find(Rule.class, id);
		if (r == null) {
			throw new NoSuchObjectFoundException("Rule (" + id + ")");
		}
		em.remove(r);
	}
	
	public static List<Rule> listRules(EntityManager em) {
		return em.createNamedQuery(Rule.ALL, Rule.class).getResultList();
	}
}
