/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.interceptor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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
import uk.icat3.sessionbeans.user.UserSession;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;
/**
 *
 * @author scb24683
 */
public class RunNumberSearchInterceptor {

    static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(RunNumberSearchInterceptor.class);
        Object[] args = ctx.getParameters();
        String className = ctx.getTarget().getClass().getSimpleName();
        log.trace("Class name: " + className);
        String methodName = ctx.getMethod().getName();
        log.trace("Method: " + methodName);

        try {
            String sessionId = (String) args[0];


            cf = (QueueConnectionFactory) new InitialContext().lookup(QueueNames.CONNECTION_FACTORY);
            queue = (Queue) new InitialContext().lookup(QueueNames.RUN_NUMBER_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            ObjectMessage runMsg = session.createObjectMessage();
            Timestamp time = new Timestamp(new Date().getTime());
            String userId = user.getUserIdFromSessionId(sessionId);
            runMsg = (ObjectMessage) PropertySetter.setProperties(runMsg, sessionId, userId, methodName, time);

            Collection<String> instruments = (Collection<String>)args[1];
            //cast collection to arraylist- message will only accept serializable object
            ArrayList<String> insList = new ArrayList<String>();
            insList.addAll(instruments);

            float start = Float.parseFloat(String.valueOf(args[2]));
            float end = Float.parseFloat(String.valueOf(args[3]));
            runMsg.setObject(insList);
            runMsg.setFloatProperty(PropertyNames.START_RUN, start);
            runMsg.setFloatProperty(PropertyNames.END_RUN, end);

            if (args.length == 6) {
                //method is searchByRunNumberPagination- save parameters
                runMsg.setIntProperty(PropertyNames.START_INDEX, Integer.parseInt(args[4].toString()));
                runMsg.setIntProperty(PropertyNames.NO_RESULTS, Integer.parseInt(args[5].toString()));
            }

            sender.send(runMsg);
            session.close();

        } catch (Exception e) {
            log.fatal("Error in RunNumber interceptor", e);
        }
        return ctx.proceed();
    }
}
