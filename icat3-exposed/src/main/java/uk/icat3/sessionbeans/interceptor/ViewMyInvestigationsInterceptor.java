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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import org.apache.log4j.Logger;
import uk.icat3.sessionbeans.user.UserSession;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
public class ViewMyInvestigationsInterceptor {

    static Queue queue;
    static QueueConnectionFactory cf;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(ViewMyInvestigationsInterceptor.class);
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
            queue = (Queue) new InitialContext().lookup(QueueNames.VIEW_MY_INV_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            TextMessage myMsg = session.createTextMessage(userId);
            myMsg = (TextMessage)PropertySetter.setProperties(myMsg, sessionId, userId, methodName, time);

            if ((args.length == 2) || (args.length ==4)) {
                //covers getMyInvestigationsInclude and Pagination)
                InvestigationInclude include = (InvestigationInclude)args[1];
                myMsg.setStringProperty(PropertyNames.INCLUDE, include.toString());
            }

            if (args.length == 4) {
                //saves Pagination parameters
                myMsg.setStringProperty(PropertyNames.START_INDEX, args[2].toString());
                myMsg.setStringProperty(PropertyNames.NO_RESULTS, args[3].toString());
            }

            sender.send(myMsg);
            session.close();

        } catch (Exception e) {
            log.fatal("Error in ViewMyInvestigationsInterceptor", e);
        }

        return ctx.proceed();
    }

}
