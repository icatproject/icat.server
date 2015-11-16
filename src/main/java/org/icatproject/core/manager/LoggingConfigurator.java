package org.icatproject.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

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

		String path = props.getProperty("logback.xml");
		if (path != null) {
			f = new File(path);
			if (!f.exists()) {
				String msg = "logback.xml file " + f.getAbsolutePath() + " specified in icat.properties not found";
				throw new IllegalStateException(msg);
			}
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext((Context) LoggerFactory.getILoggerFactory());
				context.reset();
				configurator.doConfigure(f);
			} catch (JoranException je) {
				// StatusPrinter will handle this
			}
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}

		Logger logger = LoggerFactory.getLogger(LoggingConfigurator.class);
		if (path != null) {
			logger.info("Logging configuration read from " + path);
		} else {
			logger.info("Using logback default configuration");
		}
	}
}
