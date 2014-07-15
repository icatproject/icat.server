package org.icatproject.core.manager.importParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class TableField {

	// tableField = ("?" ":" integer)
	// | (String (":" integer) | parenAttList)
	// | (String "(" "?" ":" integer ")" )

	private String name;
	private Integer offset;
	private ParenAttList parenAttlist;
	private Field field;
	private Method setter;
	private String jpql;
	private List<Attribute> attributes;
	private boolean qmark;
	private final static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	private static Map<String, Method> systemSetters = new HashMap<>();

	static {
		try {
			systemSetters.put("createId",
					EntityBaseBean.class.getDeclaredMethod("setCreateId", String.class));
			systemSetters.put("createTime",
					EntityBaseBean.class.getDeclaredMethod("setCreateTime", Date.class));
			systemSetters.put("modId",
					EntityBaseBean.class.getDeclaredMethod("setModId", String.class));
			systemSetters.put("modTime",
					EntityBaseBean.class.getDeclaredMethod("setModTime", Date.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e.getClass() + " " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public TableField(Input input, Class<EntityBaseBean> tableClass,
			Map<String, Field> fieldsByName, Map<Field, Method> setters) throws ParserException,
			IcatException {
		Token next = input.consume(Token.Type.NAME, Token.Type.QMARK);
		name = next.getValue();
		if (next.getType() == Token.Type.QMARK) {
			input.consume(Token.Type.COLON);
			offset = Integer.parseInt(input.consume(Token.Type.INTEGER).getValue());
			attributes = Collections.EMPTY_LIST;
		} else if (input.peek(1).getType() == Token.Type.QMARK) {
			qmark = true;
			input.consume(Token.Type.OPENPAREN);
			input.consume();
			input.consume(Token.Type.COLON);
			offset = Integer.parseInt(input.consume(Token.Type.INTEGER).getValue());
			field = fieldsByName.get(name);
			if (field == null) {
				throw new ParserException(name + " is not a field of " + tableClass.getSimpleName());
			}
			setter = setters.get(field);
			attributes = Collections.EMPTY_LIST;
			input.consume(Token.Type.CLOSEPAREN);
		} else {
			field = fieldsByName.get(name);
			if (field == null) {
				throw new ParserException(name + " is not a field of " + tableClass.getSimpleName());
			}
			setter = setters.get(field);
			if (setter == null) {
				setter = systemSetters.get(field.getName());
			}
			if (input.peek(0).getType() == Token.Type.COLON) {
				input.consume();
				offset = Integer.parseInt(input.consume(Token.Type.INTEGER).getValue());
				attributes = Collections.EMPTY_LIST;
			} else {
				Class<EntityBaseBean> entityClass = (Class<EntityBaseBean>) field.getType();
				parenAttlist = new ParenAttList(input, entityClass,
						eiHandler.getFieldsByName(entityClass), eiHandler.getSetters(tableClass),
						"n0");
				StringBuilder sb = new StringBuilder("SELECT n0 FROM "
						+ entityClass.getSimpleName() + " n0" + parenAttlist.getJoins());
				String sep = " WHERE ";
				for (String s : parenAttlist.getWheres()) {
					sb.append(sep + s);
					sep = " AND ";
				}
				jpql = sb.toString();
				attributes = parenAttlist.getAttributes();
			}
		}
	}

	@Override
	public String toString() {
		return parenAttlist == null ? name + (qmark ? "(?" : "") + ":" + offset
				+ (qmark ? ")" : "") : name + parenAttlist;
	}

	public Integer getOffset() {
		return offset;
	}

	public Field getField() {
		return field;
	}

	public Method getSetter() {
		return setter;
	}

	public String getJPQL() {
		return jpql;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public boolean isQmark() {
		return qmark;
	}

}
