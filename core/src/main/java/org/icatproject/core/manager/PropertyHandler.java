package org.icatproject.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.IcatException;

@DependsOn("LoggingConfigurator")
@Singleton
public class PropertyHandler {

	public class HostPort {

		private String host;
		private Integer port;

		public HostPort(Properties props, String key) {
			String hostPortString = props.getProperty(key);
			if (hostPortString != null) {
				String[] bits = hostPortString.split(":");
				host = bits[0];
				try {
					port = Integer.parseInt(bits[1]);
				} catch (NumberFormatException e) {
					abend(e.getClass() + e.getMessage());
				}
				try {
					String hostName = InetAddress.getLocalHost().getHostName();
					if (hostName.equalsIgnoreCase(bits[0])) {
						host = null;
						port = null;
						logger.debug(key + " is local machine so is ignored");
					}
				} catch (UnknownHostException e) {
					abend(e.getClass() + e.getMessage());
				}
				formattedProps.add(key + " " + hostPortString);
			}

		}

		public String getHost() {
			return host;
		}

		public Integer getPort() {
			return port;
		}

	}

	public enum Operation {
		C, U, D
	}

	private final static Logger logger = Logger.getLogger(PropertyHandler.class);;
	private final static Pattern cudPattern = Pattern.compile("[CUD]*");
	private final static Pattern srwPattern = Pattern.compile("[SRW]*");

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

	private Set<String> logRequests = new HashSet<String>();
	private String luceneDirectory;
	private int luceneCommitSeconds;

	private List<String> formattedProps = new ArrayList<String>();
	private int luceneCommitCount;

	private String luceneHost;
	private Integer lucenePort;

	@PostConstruct
	private void init() {
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

		/* log4j.properties */
		String path = props.getProperty("log4j.properties");
		if (path != null) {
			formattedProps.add("log4j.properties " + path);
		}

		/* The authn.list */
		String authnList = props.getProperty("authn.list").trim();
		if (authnList == null || authnList.isEmpty()) {
			String msg = "Property 'authn.list' must be set and must contains something";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}
		formattedProps.add("authn.list " + authnList);

		for (String mnemonic : authnList.split("\\s+")) {
			String key = "authn." + mnemonic + ".jndi";
			String jndi = props.getProperty(key).trim();
			if (jndi == null) {
				String msg = "Property '" + key + "' is not set";
				logger.fatal(msg);
				throw new IllegalStateException(msg);
			}
			HostPort hostPort = new HostPort(props, "authn." + mnemonic + ".hostPort");
			String host = hostPort.getHost();
			Integer port = hostPort.getPort();
			try {
				Context ctx = new InitialContext();
				if (host != null) {
					ctx.addToEnvironment("org.omg.CORBA.ORBInitialHost", host);
					ctx.addToEnvironment("org.omg.CORBA.ORBInitialPort", Integer.toString(port));
					logger.debug("Requesting remote authenticator at " + host + ":" + port);
				}
				Authenticator authenticator = (Authenticator) ctx.lookup(jndi);
				logger.debug("Found Authenticator: " + mnemonic + " with jndi " + jndi);
				authPlugins.put(mnemonic, authenticator);
			} catch (Throwable e) {
				String msg = e.getClass() + " reports " + e.getMessage();
				logger.fatal(msg);
				throw new IllegalStateException(msg);
			}
		}

		/* lifetimeMinutes */
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
		formattedProps.add("lifetimeMinutes " + lifetimeMinutes);

		/* rootUserNames */
		String names = props.getProperty("rootUserNames");
		if (names == null) {
			String msg = "rootUserNames is not set";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}
		for (String name : names.trim().split("\\s+")) {
			rootUserNames.add(name);
		}
		formattedProps.add("rootUserNames " + names);

		/* notification.list */
		String notificationList = props.getProperty("notification.list");
		if (notificationList == null) {
			String msg = "Property 'notification.list' must be set but may be empty";
			logger.fatal(msg);
			throw new IllegalStateException(msg);
		}
		formattedProps.add("notification.list " + notificationList);

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
				formattedProps.add(propertyName + " " + notificationOps);
				if (!notificationOps.isEmpty()) {
					Matcher m = cudPattern.matcher(notificationOps);
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

		/* log.list */
		String callLogs = props.getProperty("log.list");
		if (callLogs == null) {
			abend("Property 'log.list' must be set but may be empty");
		}
		callLogs = callLogs.trim();
		formattedProps.add("log.list " + callLogs);
		if (!callLogs.isEmpty()) {
			for (String logDest : callLogs.split("\\s+")) {
				if (logDest.equals("file") || logDest.equals("table")) {
					String propertyName = "log." + logDest;
					String logOps = props.getProperty(propertyName);

					if (logOps == null) {
						abend("Property '" + propertyName + "' must be set but may be empty");
					}
					logOps = logOps.trim();
					formattedProps.add(propertyName + " " + logOps);
					if (!logOps.isEmpty()) {
						Matcher m = srwPattern.matcher(logOps);
						if (!m.matches()) {
							abend("Property  '" + propertyName
									+ "' must only contain the letters S, R and W");
						}
						for (String c : new String[] { "S", "R", "W" }) {
							if (logOps.indexOf(c) >= 0) {
								logRequests.add(logDest + ":" + c);
								logger.debug("Log request added " + logDest + ":" + c);
							}
						}
					}
				} else {
					abend("Value '" + logDest
							+ "' specified in 'log.list' is neither 'file' nor 'tables'");
				}
			}
		}
		logger.debug("There are " + logRequests.size() + " log requests");

		/* Lucene Host */
		HostPort hostPort = new HostPort(props, "lucene.hostPort");
		luceneHost = hostPort.getHost();
		lucenePort = hostPort.getPort();

		/* Lucene Directory */
		luceneDirectory = props.getProperty("lucene.directory");
		if (luceneDirectory != null) {
			formattedProps.add("lucene.directory " + luceneDirectory);

			String luceneCommitSecondsString = props.getProperty("lucene.commitSeconds");
			if (luceneCommitSecondsString == null) {
				abend("Value of 'lucene.commitSeconds' may not be null when the lucene.directory is set");
			}
			try {
				luceneCommitSeconds = Integer.parseInt(luceneCommitSecondsString);
				formattedProps.add("lucene.commitSeconds " + luceneCommitSeconds);
				if (luceneCommitSeconds <= 0) {
					abend("Value of 'lucene.commitSeconds'" + luceneCommitSecondsString
							+ "' is not an integer greater than 0");
				}
			} catch (NumberFormatException e) {
				abend("Value of 'lucene.commitSeconds'" + luceneCommitSecondsString
						+ "' is not an integer greater than 0");
			}

			String luceneCommitCountString = props.getProperty("lucene.commitCount");
			if (luceneCommitCountString == null) {
				abend("Value of 'lucene.commitCount' may not be null when the lucene.diretcory is set");
			}
			try {
				luceneCommitCount = Integer.parseInt(luceneCommitCountString);
				formattedProps.add("lucene.commitCount " + luceneCommitCount);
				if (luceneCommitCount <= 0) {
					abend("Value of 'lucene.commitCount'" + luceneCommitCountString
							+ "' is not an integer greater than 0");
				}
			} catch (NumberFormatException e) {
				abend("Value of 'lucene.commitCount'" + luceneCommitCountString
						+ "' is not an integer greater than 0");
			}
		}
	}

	private void abend(String msg) {
		logger.fatal(msg);
		throw new IllegalStateException(msg);
	}

	public Map<String, NotificationRequest> getNotificationRequests() {
		return notificationRequests;
	}

	public Set<String> getLogRequests() {
		return logRequests;
	}

	public String getLuceneDirectory() {
		return luceneDirectory;
	}

	public int getLuceneRefreshSeconds() {
		return luceneCommitSeconds;
	}

	public List<String> props() {
		return formattedProps;
	}

	public int getLuceneCommitCount() {
		return luceneCommitCount;
	}

	public String getLuceneHost() {
		return luceneHost;
	}

	public Integer getLucenePort() {
		return lucenePort;
	}

}
