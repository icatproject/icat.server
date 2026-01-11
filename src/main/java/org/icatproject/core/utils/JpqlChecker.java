package org.icatproject.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.EntityManager;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;

public class JpqlChecker {

	// Pattern that matches the timestamp format accepted by Icat
	private static final Pattern TS_PATTERN = Pattern.compile("\\{\\s*ts\\s+\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\s*\\}");

	public static void checkJPQL(String query, EntityManager entityManager) throws IcatException {

		// Icat does not accept the JDBC standard timestamp format (Icat's format is without quotes). Therefore, we
		// must replace any timestamps before checking whether the query is valid JPQL, because the timestamps will not
		// be valid JPQL.
		Matcher m = TS_PATTERN.matcher(query);
		query = m.replaceAll(" CURRENT_TIMESTAMP ");

		try {
			entityManager.createQuery(query);
		} catch (IllegalArgumentException e) {
			m.reset();
			if (m.find()) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Timestamp literals have been replaced... " + e.getMessage());
			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getMessage());
			}
		}
	}
}
