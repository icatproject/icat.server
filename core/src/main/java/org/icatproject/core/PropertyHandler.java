package org.icatproject.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.icatproject.authentication.Authenticator;

public class PropertyHandler {

	private static PropertyHandler instance = null;
	private static final Logger logger = Logger.getLogger(PropertyHandler.class);

	synchronized public static PropertyHandler getInstance() {
		if (instance == null) {
			instance = new PropertyHandler();
		}
		return instance;
	}

	private Map<String, Authenticator> authPlugins = new HashMap<String, Authenticator>();

	public Map<String, Authenticator> getAuthPlugins() {
		return authPlugins;
	}

	private Set<String> rootUserNames = new HashSet<String>();

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

		String names = props.getProperty("rootUserNames").trim();
		if (names == null) {
			String msg = "rootUserNames is not set";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}
		for (String name : names.split("\\s+")) {
			rootUserNames.add(name);
		}

	}
}
