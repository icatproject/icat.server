/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.interceptor;

import uk.icat3.search.KeywordDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.Timestamp;
import java.util.Date;
import javax.ejb.EJB;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import org.apache.log4j.Logger;
import uk.icat3.sessionbeans.user.UserSession;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
public class KeywordsInterceptor {

    static QueueConnectionFactory cf;
    static Queue queue;
    static Logger log;
    @EJB
    UserSession user;

    @AroundInvoke
    public Object checkArguments(InvocationContext ctx) throws Exception {
        log = Logger.getLogger(KeywordsInterceptor.class);
        Object[] args = ctx.getParameters();
        String className = ctx.getTarget().getClass().getSimpleName();
        log.trace("Class name: " + className);
        String methodName = ctx.getMethod().getName();
        log.trace("Method: " + methodName);

        try {
            String sessionId = (String) args[0];


            cf = (QueueConnectionFactory) new InitialContext().lookup(QueueNames.CONNECTION_FACTORY);
            queue = (Queue) new InitialContext().lookup(QueueNames.KEYWORD_QUEUE);
            log.trace("Queue: " + queue.getQueueName());
            QueueConnection connection = cf.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);

            ObjectMessage keywordMsg = session.createObjectMessage();
            Timestamp time = new Timestamp(new Date().getTime());
            String userId = user.getUserIdFromSessionId(sessionId);
            keywordMsg = (ObjectMessage) PropertySetter.setProperties(keywordMsg, sessionId, userId, methodName, time);

            if (args.length == 2) {
                //Method is searchByKeywords
                log.info("Args is 2- plain keyword search");
                Collection<String> keywords = (Collection<String>) args[1];
                ArrayList<String> keys = new ArrayList<String>();
                keys.addAll(keywords);
                keywordMsg.setObject(keys);
            }

            if (args.length == 4) {
                //Extract info from KeywordDetails and set as properties
                log.debug("Args is 4- method is searchByKeywordsAll");
                KeywordDetails details = (KeywordDetails) args[1];
                keywordMsg.setIntProperty(PropertyNames.START_INDEX, Integer.parseInt(args[2].toString()));
                keywordMsg.setIntProperty(PropertyNames.NO_RESULTS, Integer.parseInt(args[3].toString()));
                keywordMsg.setStringProperty(PropertyNames.INCLUDE, details.getInvestigationInclude().toString());
                keywordMsg.setBooleanProperty(PropertyNames.CASE_SENSITIVE, details.isCaseSensitive());

                Collection<String> keywords = details.getKeywords();
                ArrayList<String> keys = new ArrayList<String>();
                keys.addAll(keywords);
                keywordMsg.setObject(keys);
            }

            sender.send(keywordMsg);
            session.close();
        } catch (Exception e) {
            log.fatal("Error in KeywordsInterceptor", e);
        }
        return ctx.proceed();

    }
}
