package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;

@Comment("Permissible value for string parameter types")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "VALUE",
		"PARAMETERTYPE_ID" }) })
@TableGenerator(name = "permissibleStringValueGenerator", pkColumnValue = "PermissibleStringValue")
public class PermissibleStringValue extends EntityBaseBean implements
		Serializable {

	private static Logger logger = Logger
			.getLogger(PermissibleStringValue.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "permissibleStringValueGenerator")
	private Long id;

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

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling PermissibleStringValue for " + includes);
		if (!this.includes.contains(ParameterType.class)) {
			this.type = null;
		}
	}

	public Long getId() {
		return id;
	}

	@Override
	public Object getPK() {
		return id;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager)
			throws NoSuchObjectFoundException, BadParameterException,
			IcatInternalException, ValidationException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "PermissibleStringValue[id=" + this.id + "]";
	}

}
