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

@Comment("Used to represent an arbitrary relationship between data files")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SOURCE_DATAFILE_ID", "DEST_DATAFILE_ID" }) })
@TableGenerator(name = "relatedDatafileGenerator", pkColumnValue = "RelatedDatafile")
public class RelatedDatafile extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(RelatedDatafile.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "relatedDatafileGenerator")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_DATAFILE_ID", nullable = false)
	private Datafile sourceDatafile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEST_DATAFILE_ID", nullable = false)
	private Datafile destDatafile;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Datafile getSourceDatafile() {
		return sourceDatafile;
	}

	public void setSourceDatafile(Datafile sourceDatafile) {
		this.sourceDatafile = sourceDatafile;
	}

	public Datafile getDestDatafile() {
		return destDatafile;
	}

	public void setDestDatafile(Datafile destDatafile) {
		this.destDatafile = destDatafile;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	@Comment("Identifies the type of relationship between the two datafiles - e.g. \"COPY\"")
	@Column(name = "RELATION", nullable = false)
	private String relation;

	/* Needed for JPA */
	public RelatedDatafile() {
	}

	@Override
	public String toString() {
		return "RelatedDatafile[id=" + id + "]";
	}

	@Override
	public Object getPK() {
		return id;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling RelatedDatafile for " + includes);

		if (!this.includes.contains(Datafile.class)) {
			this.sourceDatafile = null;
			this.destDatafile = null;
		}
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

}
