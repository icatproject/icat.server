package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.SingletonFinder;

@Comment("An allowed step for an INCLUDE identifed by the origin entity and the field name for navigation. "
		+ "Including an entry here is much more efficient than having to use the authorization rules.")
@SuppressWarnings("serial")
@Entity
@NamedQuery(name = "AllowedStep.GetAllQuery", query = "SELECT ps FROM PublicStep ps")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "ORIGIN", "FIELD" }) })
public class PublicStep extends EntityBaseBean implements Serializable {

	public static final String GET_ALL_QUERY = "AllowedStep.GetAllQuery";
	private static final Logger logger = Logger.getLogger(PublicStep.class);

	private static final EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	@Comment("The origin entity")
	@Column(name = "ORIGIN", nullable = false, length = 32)
	private String origin;

	@Comment("The field used to represent the relationship on the origin side")
	@Column(name = "FIELD", nullable = false, length = 32)
	private String field;

	// Needed for JPA
	public PublicStep() {
	}

	private void fixup(EntityManager manager, GateKeeper gateKeeper) throws IcatException {
		Class<? extends EntityBaseBean> bean = EntityInfoHandler.getClass(origin);
		Set<Relationship> rs = eiHandler.getRelatedEntities(bean);
		boolean found = false;
		for (Relationship r : rs) {
			if (r.getField().getName().equals(field)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, "Field value "
					+ this.field + " does not implement a relationship from " + origin);
		}
	}

	@Override
	public void postMergeFixup(EntityManager manager, GateKeeper gateKeeper) throws IcatException {
		super.postMergeFixup(manager, gateKeeper);
		this.fixup(manager, gateKeeper);
	}

	@Override
	public void preparePersist(String modId, EntityManager manager, GateKeeper gateKeeper, boolean rootUser)
			throws IcatException {
		super.preparePersist(modId, manager, gateKeeper, rootUser);
		this.fixup(manager, gateKeeper);
	}

	@PostRemove()
	void postRemove() {
		try {
			SingletonFinder.getGateKeeper().requestUpdatePublicSteps();
		} catch (Throwable e) {
			logger.error(e.getClass() + " " + e.getMessage());
		}
	}

	@PostPersist()
	void postPersist() {
		try {
			SingletonFinder.getGateKeeper().requestUpdatePublicSteps();
		} catch (Throwable e) {
			logger.error(e.getClass() + " " + e.getMessage());
		}
	}

}
