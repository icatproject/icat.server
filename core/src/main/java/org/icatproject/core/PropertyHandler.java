package org.icatproject.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.NotificationRequest;

public class PropertyHandler {

	public enum Operation {
		C, U, D
	}

	private static PropertyHandler instance = null;
	private static final Logger logger = Logger.getLogger(PropertyHandler.class);

	synchronized public static PropertyHandler getInstance() {
		if (instance == null) {
			instance = new PropertyHandler();
		}
		return instance;
	}

	private final static Pattern cruPattern = Pattern.compile("[CUD]*");

	private Map<String, Authenticator> authPlugins = new HashMap<String, Authenticator>();

	public Map<String, Authenticator> getAuthPlugins() {
		return authPlugins;
	}

	private Set<String> rootUserNames = new HashSet<String>();

	private Map<String, NotificationRequest> notificationRequests = new HashMap<String, NotificationRequest>();

	public Set<String> getRootUserNames() {
		return rootUserNames;
	}

	public int getLifetimeMinutes() {
		return lifetimeMinutes;
	}

	private int lifetimeMinutes;

	private PropertyHandler() {
		File f = new File("icat.properties");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(f));
			logger.info("Property file " + f + " loaded");
		} catch (Exception e) {
			String msg = "Problem with " + f.getAbsolutePath() + "  " + e.getMessage();
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}

		String authnList = props.getProperty("authn.list").trim();
		if (authnList == null || authnList.isEmpty()) {
			String msg = "Property 'authn.list' must be set and must contains something";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}
		Context ctx = null;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			throw new IllegalStateException(e.getClass() + " " + e.getMessage());
		}
		for (String mnemonic : authnList.split("\\s+")) {
			String key = "authn." + mnemonic + ".jndi";
			String jndi = props.getProperty(key).trim();
			if (jndi == null) {
				String msg = "Property '" + key + "' is not set";
				logger.fatal(msg);
				throw new IllegalStateException(msg);
			}
			try {
				Authenticator authenticator = (Authenticator) ctx.lookup(jndi);
				logger.debug("Found Authenticator: " + mnemonic + " with jndi " + jndi);
				authPlugins.put(mnemonic, authenticator);
			} catch (Throwable e) {
				String msg = e.getClass() + " reports " + e.getMessage();
				logger.fatal(msg);
				throw new IllegalStateException(msg);
			}
		}

		String ltm = props.getProperty("lifetimeMinutes");
		if (ltm == null) {
			String msg = "lifetimeMinutes is not set";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}

		try {
			lifetimeMinutes = Integer.parseInt(ltm);
		} catch (NumberFormatException e) {
			String msg = "lifetimeMinutes '" + ltm + "' does not represent an integer";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}

		String names = props.getProperty("rootUserNames");
		if (names == null) {
			String msg = "rootUserNames is not set";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}
		for (String name : names.trim().split("\\s+")) {
			rootUserNames.add(name);
		}

		String notificationList = props.getProperty("notification.list");
		if (notificationList == null) {
			String msg = "Property 'notification.list' must be set but may be empty";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}

		notificationList = notificationList.trim();
		if (!notificationList.isEmpty()) {
			EntityInfoHandler ei = EntityInfoHandler.getInstance();
			for (String entity : notificationList.split("\\s+")) {
				try {
					ei.getEntityInfo(entity);
				} catch (IcatException e) {
					String msg = "Value '" + entity
							+ "' specified in 'notification.list' is not an ICAT entity";
					logger.fatal(msg);
					throw new IllegalStateException(msg);
				}
				String propertyName = "notification." + entity;
				String notificationOps = props.getProperty(propertyName);
				if (notificationOps == null) {
					String msg = "Property '" + propertyName + "' must be set but may be empty";
					logger.fatal(msg);
					throw new IllegalStateException(msg);
				}
				notificationOps = notificationOps.trim();
				if (!notificationOps.isEmpty()) {
					Matcher m = cruPattern.matcher(notificationOps);
					if (!m.matches()) {
						String msg = "Property  '" + propertyName
								+ "' must only contain the letters C, U and D";
						logger.fatal(msg);
						throw new IllegalStateException(msg);
					}
					for (String c : new String[] { "C", "U", "D" }) {
						if (notificationOps.indexOf(c) >= 0) {
							notificationRequests.put(entity + ":" + c, new NotificationRequest(
									Operation.valueOf(Operation.class, c), entity));
						}
					}
				}
			}
		}
	}

	public Map<String, NotificationRequest> getNotificationRequests() {
		return notificationRequests;
	}
}
