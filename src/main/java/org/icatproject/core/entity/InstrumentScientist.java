package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

@Comment("Relationship between an ICAT user as an instrument scientist and the instrument")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID", "INSTRUMENT_ID" }) })
public class InstrumentScientist extends EntityBaseBean implements Serializable {

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
	public InstrumentScientist() {
	}

	@Override
	public void getDoc(EntityManager manager, JsonGenerator gen) throws IcatException {
		if (user.getName() == null) {
			user = manager.find(user.getClass(), user.id);
		}
		user.getDoc(manager, gen);
		SearchApi.encodeLong(gen, "instrument.id", instrument.id);
		SearchApi.encodeLong(gen, "id", id);
	}

}
