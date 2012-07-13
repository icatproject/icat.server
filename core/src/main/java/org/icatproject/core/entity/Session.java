package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.icatproject.core.IcatException;
import org.icatproject.core.authentication.Authentication;

@SuppressWarnings("serial")
@Entity
public class Session implements Serializable {

	@Id
	private String id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date expireDateTime;

	private String userName;

	private String mechanism;

	// Needed by JPA
	public Session() {
	}

	public Session(Authentication authentication, int lifetimeMinutes) {
		this.id = UUID.randomUUID().toString();
		this.userName = authentication.getUserName();
		this.mechanism = authentication.getMechanism();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, lifetimeMinutes);
		this.expireDateTime = cal.getTime();
	}

	public String checkValid() throws IcatException {
		if (expireDateTime.before(new Date()))
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session id:" + id
					+ " has expired");
		return userName;
	}

	public String toString() {
		return userName + "(" + mechanism + ") "
				+ (expireDateTime.before(new Date()) ? "expired" : "valid");
	}

	public double getRemainingMinutes() throws IcatException {
		long millis = expireDateTime.getTime() - System.currentTimeMillis();
		if (millis < 0) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Session id:" + id
					+ " has expired");
		}
		return millis / 60000.0;
	}

	public String getId() {
		return id;
	}

}
