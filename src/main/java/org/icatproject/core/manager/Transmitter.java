package org.icatproject.core.manager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.icatproject.core.manager.NotificationMessage.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@DependsOn("LoggingConfigurator")
@Singleton
public class Transmitter {

	private static Logger logger = LoggerFactory.getLogger(Transmitter.class);
	private final static Marker fatal = MarkerFactory.getMarker("FATAL");

	private Topic topic;

	@EJB
	PropertyHandler propertyHandler;

	private TopicConnection topicConnection;

	@PostConstruct
	private void init() {

		try {
			InitialContext ic = new InitialContext();
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ic
					.lookup(propertyHandler.getJmsTopicConnectionFactory());
			topicConnection = topicConnectionFactory.createTopicConnection();
			topic = (Topic) ic.lookup(propertyHandler.getJmsPrefix() + "jms/ICAT/Topic");
			logger.info("Transmitter created");
		} catch (JMSException | NamingException e) {
			logger.error(fatal, "Problem with JMS " + e);
			throw new IllegalStateException(e.getMessage());
		}

	}

	@PreDestroy()
	private void exit() {
		try {
			if (topicConnection != null) {
				topicConnection.close();
			}
			logger.info("Transmitter closing down");
		} catch (JMSException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public void processMessage(NotificationMessage notificationMessage) throws JMSException {
		Message message = notificationMessage.getMessage();
		if (message != null) {
			Session jmsSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer jmsProducer = jmsSession.createProducer(topic);
			ObjectMessage jmsg = jmsSession.createObjectMessage();
			jmsg.setStringProperty("entity", message.getEntityName());
			jmsg.setStringProperty("operation", message.getOperation());
			jmsg.setObject(message.getEntityId());
			jmsProducer.send(jmsg);
			logger.debug("Sent jms message " + message.getOperation() + " " + message.getEntityName() + " "
					+ message.getEntityId());
			jmsSession.close();
		}

	}
}
