/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.interceptor;

import java.sql.Timestamp;
import java.util.Date;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import org.apache.log4j.Logger;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.sessionbeans.user.UserSession;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
public class AdvancedSearchInterceptor {

    static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(AdvancedSearchInterceptor.class);
        Object[] args = ctx.getParameters();
        String className = ctx.getTarget().getClass().getSimpleName();
        log.trace("Class name: " + className);
        String methodName = ctx.getMethod().getName();
        log.trace("Method: " + methodName);

        try {
            String sessionId = (String) args[0];

            cf = (QueueConnectionFactory) new InitialContext().lookup(QueueNames.CONNECTION_FACTORY);
            queue = (Queue) new InitialContext().lookup(QueueNames.ADVANCED_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            AdvancedSearchDetails details = (AdvancedSearchDetails) args[1];
            ObjectMessage advMsg = session.createObjectMessage(details);
            String userId = user.getUserIdFromSessionId(sessionId);
            Timestamp time = new Timestamp(new Date().getTime());
            advMsg = (ObjectMessage) PropertySetter.setProperties(advMsg, sessionId, userId, methodName, time);

            if (args.length == 4) {
                //Method is searchByAdvancedPagination so need to save extra parameters
                advMsg.setIntProperty(PropertyNames.START_INDEX, Integer.parseInt(args[2].toString()));
                advMsg.setIntProperty(PropertyNames.NO_RESULTS, Integer.parseInt(args[3].toString()));
            }

            sender.send(advMsg);
            session.close();

        } catch (Exception e) {
            log.fatal("Error in Advanced interceptor", e);
        }

        return ctx.proceed();
    }
}
