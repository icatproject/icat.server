package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
