package uk.icat3.entity;

import java.io.Serializable;

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

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID", "INSTRUMENT_ID" }) })
@TableGenerator(name = "facilityInstrumentScientistGenerator", pkColumnValue = "FacilityInstrumentScientist")
public class FacilityInstrumentScientist extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(FacilityInstrumentScientist.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "facilityInstrumentScientistGenerator")
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	@JoinColumn(name = "INSTRUMENT_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Instrument instrument;

	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	/* Needed for JPA */
	public FacilityInstrumentScientist() {
	}

	@Override
	public String toString() {
		return "FacilityInstrumentScientist[id=" + id + "]";
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
		logger.trace("Marshalling FacilityCycle for " + includes);
		if (!this.includes.contains(User.class)) {
			this.user = null;
		}
		if (!this.includes.contains(Instrument.class)) {
			this.instrument = null;
		}
	}

}
