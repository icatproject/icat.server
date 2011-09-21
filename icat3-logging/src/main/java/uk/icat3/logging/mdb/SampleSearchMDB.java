/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.mdb;

import uk.icat3.logging.util.LoggingBeanRemote;
import java.sql.Timestamp;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import uk.icat3.entity.Sample;
import uk.icat3.logging.entity.AdvancedSearch;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.entity.Search;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.SAMPLE_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class SampleSearchMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    static Logger log;
    @EJB
    LoggingBeanRemote bean;

    public SampleSearchMDB() {
    }

    public SampleSearchMDB(EntityManager em) {
        this.em = em;
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(SampleSearchMDB.class);
        log.info("==================Sample message received================");
        try {
            String sessionId = message.getStringProperty(PropertyNames.SESSION_ID);
            String userId = message.getStringProperty(PropertyNames.USER_ID);
            String method = message.getStringProperty(PropertyNames.METHOD);
            String time = message.getStringProperty(PropertyNames.TIME);
            Timestamp searchTime = Timestamp.valueOf(time);

            Search search = new Search();
            search.setLogin(em.find(Login.class, sessionId));
            search.setUserId(userId);
            search.setMethod(method);
            search.setSearchTime(searchTime);
            em.persist(search);
            log.trace("Search persisted");

            AdvancedSearch adv = new AdvancedSearch();
            adv.setSearch(search);
            if (message instanceof TextMessage) {
                //message text is sample name
                TextMessage msg = (TextMessage)message;
                adv.setSample(msg.getText());
            } else if (message instanceof ObjectMessage) {
                //message object is Sample
                ObjectMessage msg = (ObjectMessage)message;
                Sample sample = (Sample)msg.getObject();
                if (sample.getId() != null) {
                    adv.setSampleId(sample.getId());
                    adv.setSample(sample.getName());
                } else if (sample.getName() != null) {
                    adv.setSample("(No id) " + sample.getName());
                } else {
                    adv.setSample("Empty sample searched for");
                }
                
            }
            em.persist(adv);
            log.trace("AdvancedSearch persisted");

        } catch (Exception e) {
            log.fatal("Error in sample message", e);
        }
    }
}
