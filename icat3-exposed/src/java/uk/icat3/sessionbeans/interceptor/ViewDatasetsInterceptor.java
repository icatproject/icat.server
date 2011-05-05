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
import uk.icat3.util.DatasetInclude;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
public class ViewDatasetsInterceptor {

    static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(ViewDatasetsInterceptor.class);
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
            queue = (Queue) new InitialContext().lookup(QueueNames.VIEW_DATASET_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            ObjectMessage setMsg = session.createObjectMessage();
            setMsg = (ObjectMessage)PropertySetter.setProperties(setMsg, sessionId, userId, methodName, time);
            ArrayList<Long> setIds = new ArrayList<Long>();
            
            if (args[1] instanceof Long) {
                //getDataset
                log.debug("args at 1 is Long");
                setIds.add(new Long(args[1].toString()));
            } else if (args[1] instanceof Collection) {
                //getDatasets
                log.debug("args at 1 is Collection");
                Collection<Long> ids = (Collection<Long>)args[1];
                setIds.addAll(ids);
            }
            
            setMsg.setObject(setIds);

            if (args.length == 3) {
                //getDatasetIncludes
                log.debug("Has dataset include");
                DatasetInclude include = (DatasetInclude)args[2];
                setMsg.setStringProperty(PropertyNames.INCLUDE, include.toString());
            }

            sender.send(setMsg);
            session.close();

        } catch (Exception e) {
            log.fatal("Error in dataset view interceptor", e);
        }

        return ctx.proceed();

    }

}
