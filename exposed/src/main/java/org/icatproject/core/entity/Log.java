package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@Comment("To store call logs if configured in icat.properties")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
public class Log extends EntityBaseBean implements Serializable {

	@Comment("The operation")
	private String operation;

	@Comment("Duration of call in milliseconds")
	private Long duration;

	@Comment("The name of the first entity involved if any for a read or write operation")
	private String entityName;

	@Comment("The id of the first entity involved if any for a read or write operation")
	private Long entityId;

	@Comment("The query specified in a read operation")
	@Column(length = 4000)
	private String query;

	/* Needed for JPA */
	public Log() {
	}

	public Log(String operation, long duration, String entityName, Long entityId, String query) {
		this.operation = operation;
		this.duration = duration;
		this.entityName = entityName;
		this.entityId = entityId;
		this.query = query;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
