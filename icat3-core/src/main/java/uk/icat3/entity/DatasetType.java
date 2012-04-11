package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

@Comment("A type of data set")
@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "datasetTypeGenerator", pkColumnValue = "DatasetType")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME"}) })
public class DatasetType extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(DatasetType.class);

	@OneToMany(mappedBy = "type")
	private List<Dataset> datasets = new ArrayList<Dataset>();

	@Comment("A description of this data set type")
	private String description;
	
	@Comment("The facility which has defined this data set type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "datasetTypeGenerator")
	private Long id;

	@Comment("A short name identifying this data set type within the facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	/* Needed for JPA */
	public DatasetType() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatasetType for " + this.includes);
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
		if (!this.includes.contains(Facility.class)) {
			this.facility = null;
		}
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "DatasetType[name=" + this.name + "]";
	}
	
	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

}
