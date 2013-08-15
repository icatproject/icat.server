package org.icatproject.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

@Singleton
@Startup
public class LoggingConfigurator {

	@PostConstruct
	private void init() {
		File f = new File("icat.properties");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(f));
		} catch (Exception e) {
			String msg = "Problem with " + f.getAbsolutePath() + "  " + e.getMessage();
			throw new IllegalStateException(msg);
		}

		String path = props.getProperty("log4j.properties");
		if (path != null) {
			f = new File(path);
			if (!f.exists()) {
				String msg = "log4j.properties file " + f.getAbsolutePath()
						+ " specified in icat.properties not found";
				throw new IllegalStateException(msg);
			}
			PropertyConfigurator.configure(path);

		} else {
			/*
			 * This seems to be necessary even though the default initialisation is to load from the
			 * Classpath
			 */
			PropertyConfigurator.configure(LoggingConfigurator.class.getClassLoader().getResource(
					"log4j.properties"));
		}

		Logger logger = Logger.getLogger(LoggingConfigurator.class);
		if (path != null) {
			logger.info("Logging configuration read from " + path);
		} else {
			logger.info("Using log4j default configuration");
		}
	}
}
