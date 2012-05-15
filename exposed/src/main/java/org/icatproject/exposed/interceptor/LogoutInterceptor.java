package org.icatproject.exposed.interceptor;

import java.sql.Timestamp;
import java.util.Date;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.icatproject.exposed.util.PropertyNames;
import org.icatproject.exposed.util.QueueNames;


/**
 *
 * @author scb24683
 */
public class LogoutInterceptor {

    static QueueConnectionFactory cf;
    static Queue queue;
    static Logger log;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        try {
        log = Logger.getLogger(LogoutInterceptor.class);
        Object[] args = ctx.getParameters();
        String className = ctx.getTarget().getClass().getSimpleName();
        log.trace("Class name: " + className);
        String methodName = ctx.getMethod().getName();
        log.trace("Method: " + methodName);

        String sessionId = (String) args[0];
        if ((sessionId == null) || (sessionId.length() == 0)) {
            throw new Exception("sessionId should not be null");
        }

        cf = (QueueConnectionFactory) new InitialContext().lookup(QueueNames.CONNECTION_FACTORY);
        queue = (Queue)new InitialContext().lookup(QueueNames.LOGOUT_QUEUE);
        log.trace("Queue logout: " + queue.getQueueName());
        QueueConnection connection = cf.createQueueConnection();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueSender sender = session.createSender(queue);

        Message logoutMessage = session.createTextMessage(sessionId);
        Timestamp time = new Timestamp(new Date().getTime());
        //Messages will not accept timestamp property- must change to string
        logoutMessage.setStringProperty(PropertyNames.TIME, time.toString());
        sender.send(logoutMessage);
        session.close();

        } catch (Exception e) {
            log.fatal("Error in LogoutInterceptor", e);
        }
        return ctx.proceed();
    }
}
