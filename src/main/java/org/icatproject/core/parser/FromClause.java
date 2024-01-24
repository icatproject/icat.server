package org.icatproject.core.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FromClause {

	private static final EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	private String clause;

	private Map<String, Class<? extends EntityBaseBean>> authzMap = new HashMap<>();
	private Map<String, String> replaceMap = new HashMap<>();
	private Set<String> aliases = new HashSet<>();
	private int aliasNumber = 0;

	private static Logger logger = LoggerFactory.getLogger(FromClause.class);

	@SuppressWarnings("unchecked")
	public FromClause(Input input, Set<String> idPaths) throws ParserException, IcatException {
		logger.trace("Creating FromClause for idPaths {}", idPaths);
		for (String idPath : idPaths) {
			aliases.add(idPath.split("\\.")[0]);
		}

		StringBuilder sb = new StringBuilder();
		Class<? extends EntityBaseBean> currentEntity = null;
		input.consume(Token.Type.FROM);
		Token t = input.peek(0);

		Map<String, Class<? extends EntityBaseBean>> beans = new HashMap<>();

		/* Look at each token until the end of the FROM clause is detected */
		while (t != null && t.getType() != Token.Type.WHERE && t.getType() != Token.Type.GROUP
				&& t.getType() != Token.Type.HAVING && t.getType() != Token.Type.ORDER
				&& t.getType() != Token.Type.INCLUDE && t.getType() != Token.Type.LIMIT) {
			input.consume();
			String val = t.getValue();
			sb.append(" " + val);
			if (t.getType() == Token.Type.NAME) {
				if (EntityInfoHandler.getAlphabeticEntityNames().contains(val)) {
					currentEntity = EntityInfoHandler.getClass(val);
				} else {
					int dot = val.indexOf('.');
					if (dot <= 0) {
						/* Must be a JPQL identification variable */
						if (currentEntity == null) {
							throw new ParserException(
									"No information to determine type of " + val + " in the FROM clause");
						}
						if (beans.put(val.toUpperCase(), currentEntity) != null) {
							throw new ParserException("JPQL indentification variable " + val
									+ "has already been defined in the FROM clause");
						}
					} else {
						String idv = val.substring(0, dot).toUpperCase();
						currentEntity = beans.get(idv);

						while (dot >= 0) {
							int dotn = val.indexOf(".", dot + 1);
							String relPath = dotn < 0 ? val.substring(dot + 1) : val.substring(dot + 1, dotn);

							Relationship r = eiHandler.getRelationshipsByName(currentEntity).get(relPath);
							if (r == null) {
								throw new ParserException(
										currentEntity.getSimpleName() + " has no relationship field " + relPath);
							}
							currentEntity = r.getDestinationBean();
							dot = dotn;
						}

						if (currentEntity == null) {
							throw new ParserException(
									"idVar '" + idv + "' referenced in the FROM clause is not defined.");
						}
					}

				}
			} else if (t.getType() == Token.Type.FETCH) {
				throw new ParserException("The FETCH keyword is not permitted");
			}
			t = input.peek(0);
		}

		for (String idPath : idPaths) {
			int dot = idPath.indexOf(".");
			String idv = dot < 0 ? idPath.toUpperCase() : idPath.substring(0, dot).toUpperCase();
			Class<? extends EntityBaseBean> bean = beans.get(idv);
			if (bean == null) {
				throw new ParserException(
						"idVar '" + idv + "' referenced in the SELECT clause is not defined in the FROM clause.");
			}
			String authzPath = idPath + ".id";
			while (dot >= 0) {
				int dotn = idPath.indexOf(".", dot + 1);
				String relPath = dotn < 0 ? idPath.substring(dot + 1) : idPath.substring(dot + 1, dotn);

				Field f = eiHandler.getFieldsByName(bean).get(relPath);
				if (f == null) {
					throw new ParserException(bean.getSimpleName() + " has no field " + relPath);
				}
				Class<?> fieldClass = f.getType();

				if (EntityBaseBean.class.isAssignableFrom(fieldClass)) {
					logger.trace("{} assignable to {}", relPath, fieldClass);
					bean = (Class<? extends EntityBaseBean>) fieldClass;
					if (dotn >= 0) {
						authzPath = idPath.substring(0, dotn) + ".id";
					}
				} else if (List.class.isAssignableFrom(fieldClass)) {
					ParameterizedType fieldType = (ParameterizedType) f.getGenericType();
					Class<?> actualType = (Class<?>) fieldType.getActualTypeArguments()[0];
					if (EntityBaseBean.class.isAssignableFrom(actualType)) {
						logger.trace("{} assignable to List of {}", relPath, actualType);
						bean = (Class<? extends EntityBaseBean>) actualType;
						// Cannot perform authz on a list, so need to add a JOIN to the FROM clause
						// Also need to replace the name in the SELECT with the alias of the JOIN
						String replacement = generateAlias();
						sb.append(" JOIN " + idPath + " " + replacement);
						authzPath = replacement + ".id";
						replaceMap.put(idPath, replacement);
					} else {
						logger.trace("{} of type List of {} not assignable", relPath, actualType);
						authzPath = idPath.substring(0, dot) + ".id";
					}
				} else {
					logger.trace("{} of type {} not assignable", relPath, fieldClass);
					authzPath = idPath.substring(0, dot) + ".id";
				}
				dot = dotn;
			}
			logger.trace("For authz will check {} using rules for {}", authzPath, bean.getSimpleName());
			authzMap.put(authzPath, bean);
		}

		clause = sb.toString();
	}

	private String generateAlias() {
		String alias = "a" + aliasNumber;
		aliasNumber++;
		if (aliases.contains(alias)) {
			return generateAlias();
		} else {
			return alias;
		}
	}

	@Override
	public String toString() {
		return clause;
	}

	public Map<String, Class<? extends EntityBaseBean>> getAuthzMap() {
		return authzMap;
	}

	public Map<String, String> getReplaceMap() {
		return replaceMap;
	}

}
