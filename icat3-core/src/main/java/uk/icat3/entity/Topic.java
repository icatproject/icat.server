package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "topicGenerator", pkColumnValue = "Topic")
public class Topic extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Topic.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "topicGenerator")
	private Long id;

	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "topic")
	private List<TopicInvestigation> topicInvestigations;

	/* Needed for JPA */
	public Topic() {
	}

	@Override
	public String toString() {
		return "Topic[id=" + id + "]";
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
		logger.trace("Marshalling Topic for " + includes);
		if (!this.includes.contains(TopicInvestigation.class)) {
			this.topicInvestigations = null;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TopicInvestigation> getTopicInvestigations() {
		return topicInvestigations;
	}

	public void setTopicInvestigations(List<TopicInvestigation> topicInvestigations) {
		this.topicInvestigations = topicInvestigations;
	}

}
