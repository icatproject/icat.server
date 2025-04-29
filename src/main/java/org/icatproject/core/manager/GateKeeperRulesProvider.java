package org.icatproject.core.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.icatproject.core.entity.PublicStep;
import org.icatproject.core.entity.Rule;

/*
 * This class contains methods that provide authorization rules from the
 * database to the GateKeeper. They need to be run outside of the transaction
 * that is active in GateKeeper, so that they cannot be affected by any changes
 * made in that transaction. This is acheived by having them in a separate EJB
 * with bean-managed transactions (these never use the transaction of the
 * calling bean).
 */
@ApplicationScoped
@Transactional(TxType.REQUIRES_NEW)
public class GateKeeperRulesProvider {

	@PersistenceContext(unitName = "icat")
	private EntityManager entityManager;

	public List<String> getRules(String ruleQuery, String member, String bean, String attribute) {
		return entityManager
			.createNamedQuery(ruleQuery, String.class)
			.setParameter("member", member)
			.setParameter("bean", bean)
			.setParameter("attribute", attribute)
			.getResultList();
	}

	public List<String> getRules(String ruleQuery, String member, String bean) {
		return entityManager
			.createNamedQuery(ruleQuery, String.class)
			.setParameter("member", member)
			.setParameter("bean", bean)
			.getResultList();
	}

	public Map<String, Set<String>> getPublicSteps() {
		Map<String, Set<String>> publicSteps = new HashMap<>();
		List<PublicStep> steps = entityManager.createNamedQuery(PublicStep.GET_ALL_QUERY, PublicStep.class).getResultList();

		for (PublicStep step : steps) {
			Set<String> fieldNames = publicSteps.get(step.getOrigin());
			if (fieldNames == null) {
				fieldNames = new HashSet<>();
				publicSteps.put(step.getOrigin(), fieldNames);
			}
			fieldNames.add(step.getField());
		}

		// return unmodifiable copy
		publicSteps.replaceAll((k, v) -> Set.copyOf(v));
		return Map.copyOf(publicSteps);
	}

	public Set<String> getPublicTables() {
		List<String> tableNames = entityManager.createNamedQuery(Rule.PUBLIC_QUERY, String.class).getResultList();

		// return unmodifiable copy
		return Set.copyOf(tableNames);
	}
}
