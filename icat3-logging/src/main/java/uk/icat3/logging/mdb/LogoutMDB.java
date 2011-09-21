/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.mdb;

import java.sql.Timestamp;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.LOGOUT_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class LogoutMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    static Logger log;

    public LogoutMDB(EntityManager em) {
        this.em = em;
    }

    public LogoutMDB() {
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(LogoutMDB.class);
        try {
        log.info("======================Logout message received======================");
        TextMessage msg = (TextMessage) message;
        String sessionId = msg.getText();
        String logoutTime = msg.getStringProperty(PropertyNames.TIME);
        Timestamp time = Timestamp.valueOf(logoutTime);

        Login login = em.find(Login.class, sessionId);
        login.setLogoutTime(time);

        log.debug("Logout time: " + time.toString());
        em.merge(login);
        log.trace("Login merged successfully");
        
        } catch (Exception e) {
           log.fatal("Error in LogoutMDB", e);
           e.printStackTrace();
        }
    }
}
