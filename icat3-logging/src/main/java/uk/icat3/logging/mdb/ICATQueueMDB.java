package uk.icat3.logging.mdb;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

@MessageDriven(mappedName = "jms/ICATQueue", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class ICATQueueMDB implements MessageListener {
	private final static Logger logger = Logger.getLogger(ICATQueueMDB.class);

	public ICATQueueMDB() {
	}

	@SuppressWarnings("unused")
	@PostConstruct()
	private void initICATQueueMDB() {
		logger.info("ICATQueueMDB starting up");
	}

	public void onMessage(Message message) {

		try {
			ObjectMessage msg = (ObjectMessage) message;
			logger.debug("Timestamp " + msg.getJMSTimestamp());
			logger.debug("notificationName " + msg.getStringProperty("notificationName"));
			logger.debug("userId " + msg.getStringProperty("userId"));
			logger.debug("entityName " + msg.getStringProperty("entityName"));
			logger.debug("entityArgs " + msg.getStringProperty("entityArgs"));
			logger.debug("Key" + msg.getObject());
		} catch (Exception e) {
			logger.fatal("Error in LoginMDB", e);
			e.printStackTrace();
		}

	}
}
