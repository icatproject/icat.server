package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

@Comment("A data file format")
@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "datafileFormatGenerator", pkColumnValue = "DatafileFormat")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "VERSION" }) })
public class DatafileFormat extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(DatafileFormat.class);

	@Comment("Files with this format")
	@OneToMany(mappedBy = "datafileFormat")
	private List<Datafile> datafiles = new ArrayList<Datafile>();

	@Comment("An informal description of the format")
	private String description;

	@Comment("Holds the underlying format - such as binary or text")
	private String type;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "datafileFormatGenerator")
	private Long id;

	@Comment("A short name identifying the format -e.g. \"mp3\"")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("The version if needed.  The version code may be part of the basic name")
	@Column(name = "VERSION")
	private String version;

	/* Needed for JPA */
	public DatafileFormat() {
	}

	public List<Datafile> getDatafiles() {
		return this.datafiles;
	}

	public String getDescription() {
		return this.description;
	}

	public String getType() {
		return this.type;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public String getVersion() {
		return this.version;
	}

	public void setDatafiles(List<Datafile> datafiles) {
		this.datafiles = datafiles;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "DatafileFormat[id=" + this.id + "]";
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatafileFormat for " + includes);

		if (!this.includes.contains(Datafile.class)) {
			this.datafiles = null;
		}

	}

}
