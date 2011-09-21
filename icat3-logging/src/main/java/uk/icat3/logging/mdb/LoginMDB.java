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
@MessageDriven(mappedName = QueueNames.LOGIN_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class LoginMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    static Logger log;

    public LoginMDB(EntityManager em) {
        this.em = em;
    }

    public LoginMDB() {
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(LoginMDB.class);
        try {
            log.info("======================Login message received======================");
            TextMessage msg = (TextMessage) message;
            String sessionId = msg.getText();
            String userId = msg.getStringProperty(PropertyNames.USER_ID);
            String loginTime = msg.getStringProperty(PropertyNames.TIME);
            Timestamp time = Timestamp.valueOf(loginTime);

            Login login = new Login();
            login.setSessionId(sessionId);
            login.setUserId(userId);
            login.setLoginTime(time);

            log.debug("Login time: " + time.toString());
            em.persist(login);
            log.trace("Login persisted successfully");

        } catch (Exception e) {
            log.fatal("Error in LoginMDB", e);
            e.printStackTrace();
        }

    }
}
