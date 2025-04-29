package org.icatproject.core.manager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class GateKeeperRulesCache {

	private static final Logger logger = LoggerFactory.getLogger(GateKeeperRulesCache.class);

	private static class CacheObject {
		public final String ruleQuery;
		public final String member;
		public final String bean;
		public final String attribute;

		public CacheObject(String ruleQuery, String member, String bean, String attribute) {
			this.ruleQuery = ruleQuery;
			this.member    = member;
			this.bean      = bean;
			this.attribute = attribute;
		}

		@Override
		public int hashCode() {
			return Objects.hash(ruleQuery, member, bean, attribute);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CacheObject) {
				CacheObject that = (CacheObject) obj;

				if (Objects.equals(this.ruleQuery, that.ruleQuery)
				 && Objects.equals(this.member,    that.member)
				 && Objects.equals(this.bean,      that.bean)
				 && Objects.equals(this.attribute, that.attribute)) {
					return true;
				}
			}

			return false;
		}
	}

	@Inject
	GateKeeperRulesProvider gateKeeperRulesProvider;

	private Map<String, Set<String>> publicSteps = null;
	private Set<String> publicTables = null;
	private Map<CacheObject, List<String>> rulesCache = new HashMap<>();

	public List<String> getRules(String ruleQuery, String member, String bean, String attribute) {
		CacheObject cacheObject = new CacheObject(ruleQuery, member, bean, attribute);

		List<String> rules = rulesCache.get(cacheObject);

		if (rules == null) {
			rules = gateKeeperRulesProvider.getRules(ruleQuery, member, bean, attribute);

			// Sort the results by string length. It is probably faster to evaluate a shorter query.
			rules.sort(Comparator.nullsFirst(Comparator.comparing(String::length)));

			rulesCache.put(cacheObject, rules);
		}

		return rules;
	}

	public List<String> getRules(String ruleQuery, String member, String bean) {
		CacheObject cacheObject = new CacheObject(ruleQuery, member, bean, null);

		List<String> rules = rulesCache.get(cacheObject);

		if (rules == null) {
			rules = gateKeeperRulesProvider.getRules(ruleQuery, member, bean);

			// Sort the results by string length. It is probably faster to evaluate a shorter query.
			rules.sort(Comparator.nullsFirst(Comparator.comparing(String::length)));

			rulesCache.put(cacheObject, rules);
		}

		return rules;
	}

	public Map<String, Set<String>> getPublicSteps() {
		if (publicSteps == null) {
			publicSteps = gateKeeperRulesProvider.getPublicSteps();
			logger.debug("There are {} publicSteps: {}", publicSteps.size(), publicSteps.toString());
		}
		return publicSteps;
	}

	public Set<String> getPublicTables() {
		if (publicTables == null) {
			publicTables = gateKeeperRulesProvider.getPublicTables();
			logger.debug("There are {} publicTables: {}", publicTables.size(), publicTables.toString());
		}
		return publicTables;
	}
}
