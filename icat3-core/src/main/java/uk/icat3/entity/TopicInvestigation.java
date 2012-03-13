package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
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

@Comment("Many to many relationship between a topic and an investigation")
@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "topicInvestigationGenerator", pkColumnValue = "TopicInvestigation")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "TOPIC_ID", "INVESTIGATION_ID" }) })
public class TopicInvestigation extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(TopicInvestigation.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "topicInvestigationGenerator")
	private Long id;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	@JoinColumn(name = "TOPIC_ID", nullable = false)
	@ManyToOne
	private Topic topic;

	/* Needed for JPA */
	public TopicInvestigation() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	@Override
	public String toString() {
		return "TopicInvestigation[id=" + id + "]";
	}

	@Override
	public Object getPK() {
		return id;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling TopicInvestigation for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
		if (!this.includes.contains(Topic.class)) {
			this.topic = null;
		}
	}

}
