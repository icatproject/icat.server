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
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import org.apache.log4j.Logger;
import uk.icat3.entity.Sample;
import uk.icat3.logging.util.QueueNames;
import uk.icat3.sessionbeans.user.UserSession;

/**
 *
 * @author scb24683
 */
public class SampleSearchInterceptor {

    static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(SampleSearchInterceptor.class);
        Object[] args = ctx.getParameters();
        String className = ctx.getTarget().getClass().getSimpleName();
        log.trace("Class name: " + className);
        String methodName = ctx.getMethod().getName();
        log.trace("Method: " + methodName);

        try {
            String sessionId = (String) args[0];
            String userId = user.getUserIdFromSessionId(sessionId);
            cf = (QueueConnectionFactory) new InitialContext().lookup(QueueNames.CONNECTION_FACTORY);
            queue = (Queue) new InitialContext().lookup(QueueNames.SAMPLE_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);
            Timestamp time = new Timestamp(new Date().getTime());

            if (args[1] instanceof String) {
                //method is searchSamplesBySampleName- args[1] is sample name
                TextMessage sampleMsg = session.createTextMessage(args[1].toString());
                sampleMsg = (TextMessage)PropertySetter.setProperties(sampleMsg, sessionId, userId, methodName, time);
                sender.send(sampleMsg);
            } else if (args[1] instanceof Sample) {
                //method is search datasets by sample
                ObjectMessage sampleMsg = session.createObjectMessage((Sample)args[1]);
                sampleMsg = (ObjectMessage)PropertySetter.setProperties(sampleMsg, sessionId, userId, methodName, time);
                sender.send(sampleMsg);
            }
            session.close();
        } catch (Exception e) {
            log.fatal("Error in sample interceptor", e);
        }
        return ctx.proceed();
    }

}
