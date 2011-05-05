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
public class ViewInvestigationsInterceptor {

        static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(ViewInvestigationsInterceptor.class);
        Object[] args = ctx.getParameters();
        String className = ctx.getTarget().getClass().getSimpleName();
        log.trace("Class name: " + className);
        String methodName = ctx.getMethod().getName();
        log.trace("Method: " + methodName);

        try {
            String sessionId = (String) args[0];
            String userId = user.getUserIdFromSessionId(sessionId);
            Timestamp time = new Timestamp(new Date().getTime());
            cf = (QueueConnectionFactory) new InitialContext().lookup(QueueNames.CONNECTION_FACTORY);
            queue = (Queue) new InitialContext().lookup(QueueNames.VIEW_INVESTIGATION_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);
            
            ObjectMessage invMsg = session.createObjectMessage();
            invMsg = (ObjectMessage)PropertySetter.setProperties(invMsg, sessionId, userId, methodName, time);

            ArrayList<Long> invIds = new ArrayList<Long>();
            if (args[1] instanceof Long) {
                //getInvestigation
                invIds.add(new Long(args[1].toString()));
            } else if (args[1] instanceof Collection) {
                //getInvestigations
                Collection<Long> ids = (Collection<Long>)args[1];
                invIds.addAll(ids);
            }

            invMsg.setObject(invIds);
            if (args.length == 3) {
                //getInvestigationInclude AND getInvestigationsInclude
                invMsg.setStringProperty(PropertyNames.INCLUDE, args[2].toString());
            }

            sender.send(invMsg);
            session.close();


        } catch (Exception e) {
            log.fatal("Error in ViewInvestigationsInterceptor", e);
        }
        return ctx.proceed();
    }

}
