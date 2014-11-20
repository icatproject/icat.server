package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Permissible value for string parameter types")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "VALUE", "PARAMETERTYPE_ID" }) })
public class PermissibleStringValue extends EntityBaseBean implements Serializable {

	@Comment("The parameter type to which this permissible string value applies")
	@JoinColumn(name = "PARAMETERTYPE_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private ParameterType type;

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	@Comment("The value of the string")
	@Column(name = "VALUE", nullable = false)
	private String value;

	/* Needed for JPA */
	public PermissibleStringValue() {
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
