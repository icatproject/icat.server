package org.icatproject.exposed;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

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
			PropertyConfigurator.configure(LoggingConfigurator.class.getClassLoader().getResource(
					"log4j.properties"));
		}

		Logger logger = Logger.getLogger(LoggingConfigurator.class);
		if (path != null) {
			System.out.println("Logging configuration read from " + path);
			logger.info("Logging configuration read from " + path);
		} else {
			System.out.println("Using log4j default configuration");
			logger.info("Using log4j default configuration");
		}

	}
}
