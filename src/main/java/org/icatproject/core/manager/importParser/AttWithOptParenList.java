package org.icatproject.core.manager.importParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.importParser.Token.Type;

public class AttWithOptParenList {

	// AttWithOptParenList = attribute ( ":" non-neg-int ) | parenAttList)

	private String attribute;
	private ParenAttList parenAttList;
	private Integer fieldNum;
	private String joins = "";
	private List<String> wheres;
	private List<Attribute> attributes;
	private final static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	@SuppressWarnings("unchecked")
	public AttWithOptParenList(Input input, Class<EntityBaseBean> entityClass,
			Map<String, Field> fieldsByName, Map<Field, Method> setters, String parentName,
			String relName) throws ParserException, IcatException {
		attribute = input.consume(Token.Type.NAME).getValue();
		Field field = fieldsByName.get(attribute);
		if (field == null) {
			throw new ParserException(attribute + " is not a field of "
					+ entityClass.getSimpleName());
		}

		Type nextT = input.peek(0).getType();
		if (nextT == Token.Type.OPENPAREN) {
			entityClass = (Class<EntityBaseBean>) field.getType();
			parenAttList = new ParenAttList(input, entityClass,
					eiHandler.getFieldsByName(entityClass), eiHandler.getSetters(entityClass),
					parentName + relName);
			joins = " JOIN " + parentName + "." + attribute + " " + parentName + relName
					+ parenAttList.getJoins();
			wheres = parenAttList.getWheres();
			attributes = parenAttList.getAttributes();
		} else {
			input.consume(Token.Type.COLON);
			fieldNum = Integer.parseInt(input.consume(Token.Type.INTEGER).getValue());
			wheres = Arrays.asList(parentName + "." + attribute + " = :p" + fieldNum);
			attributes = Arrays.asList(new Attribute(fieldNum, field.getType().getSimpleName()));
		}
	}

	@Override
	public String toString() {
		return attribute + (parenAttList == null ? ":" + fieldNum : parenAttList);
	}

	public String getAttribute() {
		return attribute;
	}

	public String getJoins() {
		return joins;
	}

	public List<String> getWheres() {
		return wheres;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

}
