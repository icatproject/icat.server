package org.icatproject.userldap.facility;

/**
 *
 * @author scb24683
 */
import java.sql.Timestamp;
import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import org.apache.log4j.Logger;

public class LoginInterceptor {

    static Logger log;
    static Queue queue;
    static QueueConnectionFactory cf;

    public static void sendLoginMessage(String sessionId, String userId, Timestamp loginTime) {
        log = Logger.getLogger(LoginInterceptor.class);
        try {
            cf = (QueueConnectionFactory) new InitialContext().lookup("jms/icat/QueueConnectionFactory");
            queue = (Queue)new InitialContext().lookup("jms/login/Queue");
            log.info("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            Message loginMessage = session.createTextMessage(sessionId);
            loginMessage.setStringProperty("userId", userId);
            loginMessage.setStringProperty("time", loginTime.toString());
            System.out.println("Message text: " + sessionId);

            sender.send(loginMessage);
            log.info("Message sent");
            session.close();

        } catch (Exception e) {
            log.fatal("Error sending message", e);
        }
    }
}
