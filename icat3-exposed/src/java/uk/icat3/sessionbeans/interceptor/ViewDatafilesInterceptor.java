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
import uk.icat3.logging.util.QueueNames;
import uk.icat3.sessionbeans.user.UserSession;

/**
 *
 * @author scb24683
 */
public class ViewDatafilesInterceptor {

    static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(ViewDatafilesInterceptor.class);
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
            queue = (Queue) new InitialContext().lookup(QueueNames.VIEW_DATAFILE_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            ObjectMessage datafileMsg = session.createObjectMessage();
            datafileMsg = (ObjectMessage)PropertySetter.setProperties(datafileMsg, sessionId, userId, methodName, time);

            ArrayList<Long> files = new ArrayList<Long>();
            if (args[1] instanceof Long) {
                //getDatafile method
                log.debug("arg at 1 is Long" + args[1].toString());
                files.add(new Long(args[1].toString()));
                log.debug(files.get(0));
            } else if (args[1] instanceof Collection) {
                //getDatafiles method
                log.debug("arg at 1 is Collection");
                Collection<Long> fileIds = (Collection<Long>)args[1];
                files.addAll(fileIds);
            }

            datafileMsg.setObject(files);
            sender.send(datafileMsg);
            session.close();

        } catch (Exception e) {
            log.fatal("Error in view datafiles interceptor", e);
        }
        return ctx.proceed();
    }

}
