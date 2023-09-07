package org.icatproject.core.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParsingException;
import javax.naming.InitialContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.icatproject.authentication.Authentication;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.CheckedProperties.CheckedPropertyException;
import org.icatproject.utils.ContainerGetter;
import org.icatproject.utils.ContainerGetter.ContainerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Singleton
public class PropertyHandler {

	public class RestAuthenticator implements Authenticator {

		private String mnemonic;
		private List<String> urls;

		public RestAuthenticator(String mnemonic, String urls) throws IcatException {
			this.mnemonic = mnemonic;
			this.urls = Arrays.asList(urls.split("\\s+"));
			String desc = null;
			for (String url : this.urls) {
				try {
					URI uri = new URIBuilder(url).setPath("/authn." + mnemonic + "/" + "description").build();

					logger.trace("Calling " + uri);
					try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
						HttpGet httpGet = new HttpGet(uri);
						try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
							String resp = getString(response);
							if (desc == null) {
								desc = resp;
							} else if (!desc.equals(resp)) {
								throw new IcatException(IcatExceptionType.INTERNAL,
										"authenticators have mismatched descriptions");
							}
						}
					}
				} catch (URISyntaxException | IOException | IcatException e) {
					logger.error(e.getClass() + " " + e.getMessage());
				}
			}
			if (desc == null) {
				throw new IcatException(IcatExceptionType.INTERNAL,
						"No authenticator of type " + mnemonic + " is working");
			}
		}

		@Override
		public Authentication authenticate(Map<String, String> credentials, String ip) throws IcatException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos)) {
				gen.writeStartObject();
				gen.writeStartArray("credentials");
				for (Entry<String, String> entry : credentials.entrySet()) {
					gen.writeStartObject().write(entry.getKey(), entry.getValue()).writeEnd();
				}
				gen.writeEnd();
				gen.write("ip", ip);
				gen.writeEnd().close();
			}
			for (String url : this.urls) {
				try {
					URI uri = new URIBuilder(url).setPath("/authn." + mnemonic + "/" + "authenticate").build();

					List<NameValuePair> formparams = new ArrayList<>();
					formparams.add(new BasicNameValuePair("json", baos.toString()));
					try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
						HttpPost httpPost = new HttpPost(uri);
						httpPost.setEntity(new UrlEncodedFormEntity(formparams));
						try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
							Rest.checkStatus(response, IcatExceptionType.SESSION);
							try (JsonReader r = Json
									.createReader(new ByteArrayInputStream(getString(response).getBytes()))) {
								JsonObject o = r.readObject();
								String username = o.getString("username");
								String mechanism = null;
								if (o.containsKey("mechanism")) {
									mechanism = o.getString("mechanism");
								}
								return new Authentication(username, mechanism);
							}
						}
					}
				} catch (URISyntaxException | IOException e) {
					logger.error("Authenticator of type", mnemonic, "reports", e.getClass().getName(), e.getMessage());
				}
			}
			throw new IcatException(IcatExceptionType.INTERNAL, "No authenticator of type " + mnemonic + " is working");
		}

		@Override
		public String getDescription() throws IcatException {
			for (String url : this.urls) {
				try {
					URI uri = new URIBuilder(url).setPath("/authn." + mnemonic + "/" + "description").build();
					try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
						HttpGet httpGet = new HttpGet(uri);
						try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
							return getString(response);
						}
					}
				} catch (URISyntaxException | IOException | IcatException e) {
					logger.error(e.getClass() + " " + e.getMessage());
				}
			}
			throw new IcatException(IcatExceptionType.INTERNAL, "No authenticator of type " + mnemonic + " is working");
		}

		private String getString(CloseableHttpResponse response) throws IcatException {
			checkStatus(response);
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				throw new IcatException(IcatExceptionType.INTERNAL, "No http entity returned in response");
			}
			try {
				return EntityUtils.toString(entity);
			} catch (ParseException | IOException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		}

		private void checkStatus(HttpResponse response) throws IcatException {
			StatusLine status = response.getStatusLine();
			if (status == null) {
				throw new IcatException(IcatExceptionType.INTERNAL, "Status line returned is empty");
			}
			int rc = status.getStatusCode();
			if (rc / 100 != 2) {
				HttpEntity entity = response.getEntity();
				String error;
				if (entity == null) {
					throw new IcatException(IcatExceptionType.INTERNAL, "No explanation provided");
				} else {
					try {
						error = EntityUtils.toString(entity);
					} catch (ParseException | IOException e) {
						throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
					}
				}
				try (JsonParser parser = Json.createParser(new ByteArrayInputStream(error.getBytes()))) {
					String code = null;
					String message = null;
					String key = "";
					while (parser.hasNext()) {
						JsonParser.Event event = parser.next();
						if (event == Event.KEY_NAME) {
							key = parser.getString();
						} else if (event == Event.VALUE_STRING) {
							if (key.equals("code")) {
								code = parser.getString();
							} else if (key.equals("message")) {
								message = parser.getString();
							}
						}
					}

					if (code == null || message == null) {
						throw new IcatException(IcatExceptionType.INTERNAL, error);
					}
					throw new IcatException(IcatExceptionType.INTERNAL, message);
				} catch (JsonParsingException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, error);
				}
			}

		}

	}

	public enum CallType {
		READ, WRITE, SESSION, INFO
	}

	public enum SearchEngine {
		LUCENE, ELASTICSEARCH, OPENSEARCH
	}

	public class ExtendedAuthenticator {

		private Authenticator authenticator;
		private String friendly;
		private boolean admin;

		public ExtendedAuthenticator(Authenticator authenticator, String friendly, boolean admin) {
			this.authenticator = authenticator;
			this.friendly = friendly;
			this.admin = admin;
		}

		public Authenticator getAuthenticator() {
			return authenticator;
		}

		public String getFriendly() {
			return friendly;
		}

		public boolean isAdmin() {
			return admin;
		}

	}

	public enum Operation {
		C, U
	}

	private final static Logger logger = LoggerFactory.getLogger(PropertyHandler.class);
	private final static Marker fatal = MarkerFactory.getMarker("FATAL");
	private final static Pattern cuPattern = Pattern.compile("[CU]*");

	private Map<String, ExtendedAuthenticator> authPlugins = new LinkedHashMap<>();

	public Map<String, ExtendedAuthenticator> getAuthPlugins() {
		return authPlugins;
	}

	private Set<String> rootUserNames = new HashSet<String>();

	private Map<String, NotificationRequest> notificationRequests = new HashMap<String, NotificationRequest>();

	public Set<String> getRootUserNames() {
		return rootUserNames;
	}

	/**
	 * Configure which entities will be indexed on ingest
	 */
	private Set<String> entitiesToIndex = new HashSet<String>();

	public Set<String> getEntitiesToIndex() {
		return entitiesToIndex;
	}

	public int getLifetimeMinutes() {
		return lifetimeMinutes;
	}

	private int lifetimeMinutes;

	private Set<CallType> logSet = new HashSet<>();

	private List<String> formattedProps = new ArrayList<String>();

	private int maxEntities;
	private int maxIdsInQuery;
	private long importCacheSize;
	private long exportCacheSize;
	private ContainerType containerType;
	private String jmsTopicConnectionFactory;
	private String digestKey;
	private SearchEngine searchEngine;
	private List<URL> searchUrls = new ArrayList<>();
	private int searchPopulateBlockSize;
	private int searchSearchBlockSize;
	private Path searchDirectory;
	private long searchBacklogHandlerIntervalMillis;
	private long searchAggregateFilesIntervalMillis;
	private long searchMaxSearchTimeMillis;
	private String unitAliasOptions;
	private Map<String, String> cluster = new HashMap<>();
	private long searchEnqueuedRequestIntervalMillis;

	@PostConstruct
	private void init() {
		CheckedProperties props = new CheckedProperties();
		try {
			props.loadFromResource("run.properties");
			logger.info("Property file run.properties loaded");
			String key;

			/* The authn.list */
			String authnList = props.getString("authn.list");
			formattedProps.add("authn.list " + authnList);

			for (String mnemonic : authnList.split("\\s+")) {
				Authenticator authen = null;
				String keyJndi = "authn." + mnemonic + ".jndi";
				String keyUrl = "authn." + mnemonic + ".url";
				if (props.has(keyJndi) && props.has(keyUrl)) {
					abend("Both " + keyJndi + " and " + keyUrl + " have been specified in run.properties");
				}
				if (props.has(keyJndi)) {
					String jndi = props.getString(keyJndi);
					formattedProps.add(keyJndi + " " + jndi);
					String hpKey = "authn." + mnemonic + ".hostPort";
					if (props.has(hpKey)) {
						abend("Key  '" + hpKey + " specified in run.properties is no longer permitted");
					}
					try {
						authen = (Authenticator) new InitialContext().lookup(jndi);
					} catch (Throwable e) {
						abend(e.getClass() + " reports " + e.getMessage());
					}
					logger.debug("Found Authenticator: " + mnemonic + " with jndi " + jndi);
				} else {
					String urls = props.getString(keyUrl);
					try {
						authen = new RestAuthenticator(mnemonic, urls);
					} catch (IcatException e) {
						abend(e.getClass() + " " + e.getMessage());
					}
					formattedProps.add(keyUrl + " = " + urls);
				}

				key = "authn." + mnemonic + ".friendly";
				String friendly = null;
				if (props.has(key)) {
					friendly = props.getString(key);
					formattedProps.add(key + " " + friendly);
				}

				key = "authn." + mnemonic + ".admin";
				boolean admin = props.getBoolean(key, false);
				if (props.has(key)) {
					formattedProps.add(key + " " + admin);
				}

				ExtendedAuthenticator authenticator = new ExtendedAuthenticator(authen, friendly, admin);
				authPlugins.put(mnemonic, authenticator);

			}

			/* lifetimeMinutes */
			lifetimeMinutes = props.getPositiveInt("lifetimeMinutes");
			formattedProps.add("lifetimeMinutes " + lifetimeMinutes);

			/* rootUserNames */
			String names = props.getString("rootUserNames");
			for (String name : names.split("\\s+")) {
				rootUserNames.add(name);
			}
			formattedProps.add("rootUserNames " + names);

			/* entitiesToIndex */
			key = "search.entitiesToIndex";
			if (props.has(key)) {
				String indexableEntities = props.getString(key);
				for (String indexableEntity : indexableEntities.split("\\s+")) {
					entitiesToIndex.add(indexableEntity);
				}
				logger.info("search.entitiesToIndex: {}", entitiesToIndex.toString());
			} else {
				/*
				 * If the property is not specified, we default to all the entities which
				 * currently override the EntityBaseBean.getDoc() method. This should
				 * result in no change to behaviour if the property is not specified.
				 */
				entitiesToIndex.addAll(Arrays.asList("Datafile", "DatafileFormat", "DatafileParameter",
						"Dataset", "DatasetParameter", "DatasetType", "Facility", "Instrument", "InstrumentScientist",
						"Investigation", "InvestigationInstrument", "InvestigationParameter", "InvestigationType",
						"InvestigationUser", "ParameterType", "Sample", "SampleType", "SampleParameter", "User"));
				logger.info("search.entitiesToIndex not set. Defaulting to: {}", entitiesToIndex.toString());
			}
			formattedProps.add("search.entitiesToIndex " + entitiesToIndex.toString());

			/* notification.list */
			key = "notification.list";
			if (props.has(key)) {
				String notificationList = props.getString(key);
				formattedProps.add(key + " " + notificationList);

				EntityInfoHandler ei = EntityInfoHandler.getInstance();
				for (String entity : notificationList.split("\\s+")) {
					try {
						ei.getEntityInfo(entity);
					} catch (IcatException e) {
						String msg = "Value '" + entity + "' specified in 'notification.list' is not an ICAT entity";
						logger.error(fatal, msg);
						throw new IllegalStateException(msg);
					}
					key = "notification." + entity;
					String notificationOps = props.getString(key);

					formattedProps.add(key + " " + notificationOps);

					Matcher m = cuPattern.matcher(notificationOps);
					if (!m.matches()) {
						String msg = "Property  '" + key + "' must only contain the letters C and U";
						logger.error(fatal, msg);
						throw new IllegalStateException(msg);
					}
					for (String c : new String[] { "C", "U" }) {
						if (notificationOps.indexOf(c) >= 0) {
							notificationRequests.put(entity + ":" + c,
									new NotificationRequest(Operation.valueOf(Operation.class, c), entity));
						}
					}
				}
				logger.info("notification.list: {}", notificationList);
			} else {
				logger.info("'notification.list' entry not present so no notifications will be sent");
			}

			/* Call logging categories */
			key = "log.list";
			if (props.has(key)) {
				String callLogs = props.getString(key);
				formattedProps.add(key + " " + callLogs);
				for (String callTypeString : callLogs.split("\\s+")) {
					try {
						logSet.add(CallType.valueOf(callTypeString.toUpperCase()));
					} catch (IllegalArgumentException e) {
						String msg = "Value " + callTypeString + " in log.list must be chosen from "
								+ Arrays.asList(CallType.values());
						logger.error(fatal, msg);
						throw new IllegalStateException(msg);
					}
				}
				logger.info("log.list: {}", logSet);
			} else {
				logger.info("'log.list' entry not present so no JMS call logging will be performed");
			}

			/* Search Host */
			if (props.has("search.engine")) {
				try {
					searchEngine = SearchEngine.valueOf(props.getString("search.engine").toUpperCase());
				} catch (IllegalArgumentException e) {
					String msg = "Value " + props.getString("search.engine") + " of search.engine must be chosen from "
							+ Arrays.asList(SearchEngine.values());
					throw new IllegalStateException(msg);
				}

				for (String urlString : props.getString("search.urls").split("\\s+")) {
					try {
						searchUrls.add(new URL(urlString));
					} catch (MalformedURLException e) {
						abend("Url in search.urls " + urlString + " is not a valid URL");
					}
				}

				// In principle, clustered engines like OPENSEARCH or ELASTICSEARCH should
				// support multiple urls for the nodes in the cluster, however this is not yet
				// implemented
				if (searchUrls.size() != 1) {
					String msg = "Exactly one value for search.urls must be provided when using " + searchEngine;
					throw new IllegalStateException(msg);
				}
				formattedProps.add("search.urls" + " " + searchUrls.toString());
				logger.info("Using {} as search engine with url(s) {}", searchEngine, searchUrls);

				searchPopulateBlockSize = props.getPositiveInt("search.populateBlockSize");
				formattedProps.add("search.populateBlockSize" + " " + searchPopulateBlockSize);

				searchSearchBlockSize = props.getPositiveInt("search.searchBlockSize");
				formattedProps.add("search.searchBlockSize" + " " + searchSearchBlockSize);

				searchDirectory = props.getPath("search.directory");
				if (!searchDirectory.toFile().isDirectory()) {
					String msg = searchDirectory + " is not a directory";
					logger.error(fatal, msg);
					throw new IllegalStateException(msg);
				}
				formattedProps.add("search.directory" + " " + searchDirectory);

				searchBacklogHandlerIntervalMillis = props.getPositiveLong("search.backlogHandlerIntervalSeconds");
				formattedProps.add("search.backlogHandlerIntervalSeconds" + " " + searchBacklogHandlerIntervalMillis);
				searchBacklogHandlerIntervalMillis *= 1000;

				searchEnqueuedRequestIntervalMillis = props.getPositiveLong("search.enqueuedRequestIntervalSeconds");
				formattedProps.add("search.enqueuedRequestIntervalSeconds" + " " + searchEnqueuedRequestIntervalMillis);
				searchEnqueuedRequestIntervalMillis *= 1000;

				searchAggregateFilesIntervalMillis = props.getNonNegativeLong("search.aggregateFilesIntervalSeconds");
				searchAggregateFilesIntervalMillis *= 1000;

				searchMaxSearchTimeMillis = props.getPositiveLong("search.maxSearchTimeSeconds");
				formattedProps.add("search.maxSearchTimeSeconds" + " " + searchMaxSearchTimeMillis);
				searchMaxSearchTimeMillis *= 1000;
			} else {
				logger.info("'search.engine' entry not present so no free text search available");
			}

			unitAliasOptions = props.getString("units", "");

			/*
			 * maxEntities, importCacheSize, exportCacheSize, maxIdsInQuery, key
			 */
			maxEntities = props.getPositiveInt("maxEntities");
			formattedProps.add("maxEntities " + maxEntities);

			importCacheSize = props.getPositiveLong("importCacheSize");
			formattedProps.add("importCacheSize " + importCacheSize);

			exportCacheSize = props.getPositiveLong("exportCacheSize");
			formattedProps.add("exportCacheSize " + exportCacheSize);

			maxIdsInQuery = props.getPositiveInt("maxIdsInQuery");
			formattedProps.add("maxIdsInQuery " + maxIdsInQuery);

			if (props.has("key")) {
				digestKey = props.getString("key");
				formattedProps.add("key " + digestKey);
				logger.info("Key is " + (digestKey == null ? "not set" : "set"));
			}

			key = "cluster";
			if (props.has(key)) {
				String clusterString = props.getString(key);
				formattedProps.add(key + " " + clusterString);
				cluster = new HashMap<>();
				for (String urlString : clusterString.split("\\s+")) {
					URL url = null;
					try {
						url = new URL(urlString);
					} catch (MalformedURLException e) {
						abend("Url in cluster " + urlString + " is not a valid URL");
					}
					String host = url.getHost();
					InetAddress address = null;
					try {
						address = InetAddress.getByName(host);
					} catch (UnknownHostException e) {
						abend("Host " + host + " in cluster specification is not known");
					}
					String hostAddress = address.getHostAddress();
					try {
						if (hostAddress.equals(InetAddress.getLocalHost().getHostAddress())) {
							continue;
						}
					} catch (UnknownHostException e) {
						// Ignore
					}

					if (Arrays.asList("localhost.localdomain", "localhost", "127.0.0.1").contains(host)) {
						continue;
					}

					cluster.put(address.getHostAddress(), url.toExternalForm());
					logger.info("Cluster includes " + url.toExternalForm() + " " + hostAddress);
				}
			}

			/* JMS stuff */
			jmsTopicConnectionFactory = props.getString("jms.topicConnectionFactory",
					"java:comp/DefaultJMSConnectionFactory");
			formattedProps.add("jms.topicConnectionFactory " + jmsTopicConnectionFactory);

			/* find type of container and set flags */
			containerType = ContainerGetter.getContainer();
			logger.info("ICAT has been deployed in a " + containerType + " container");
			if (containerType == ContainerType.UNKNOWN) {
				abend("Container type " + containerType + " is not recognised");
			}
		} catch (CheckedPropertyException e) {
			abend(e.getMessage());
		}

	}

	public Map<String, String> getCluster() {
		return cluster;
	}

	private void abend(String msg) {
		logger.error(fatal, msg);
		throw new IllegalStateException(msg);
	}

	public Map<String, NotificationRequest> getNotificationRequests() {
		return notificationRequests;
	}

	public Set<CallType> getLogSet() {
		return logSet;
	}

	public List<String> props() {
		return formattedProps;
	}

	public int getMaxEntities() {
		return maxEntities;
	}

	public int getMaxIdsInQuery() {
		return maxIdsInQuery;
	}

	public long getImportCacheSize() {
		return importCacheSize;
	}

	public long getExportCacheSize() {
		return exportCacheSize;
	}

	public ContainerType getContainerType() {
		return containerType;
	}

	public String getJmsTopicConnectionFactory() {
		return jmsTopicConnectionFactory;
	}

	public String getKey() {
		return digestKey;
	}

	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	public List<URL> getSearchUrls() {
		return searchUrls;
	}

	public int getSearchPopulateBlockSize() {
		return searchPopulateBlockSize;
	}

	public int getSearchSearchBlockSize() {
		return searchSearchBlockSize;
	}

	public long getSearchBacklogHandlerIntervalMillis() {
		return searchBacklogHandlerIntervalMillis;
	}

	public long getSearchEnqueuedRequestIntervalMillis() {
		return searchEnqueuedRequestIntervalMillis;
	}

	public long getSearchAggregateFilesIntervalMillis() {
		return searchAggregateFilesIntervalMillis;
	}

	public long getSearchMaxSearchTimeMillis() {
		return searchMaxSearchTimeMillis;
	}

	public Path getSearchDirectory() {
		return searchDirectory;
	}

	public String getUnitAliasOptions() {
		return unitAliasOptions;
	}

}
