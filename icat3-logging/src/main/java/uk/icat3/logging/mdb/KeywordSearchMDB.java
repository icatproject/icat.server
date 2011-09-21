/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.mdb;

import uk.icat3.logging.util.LoggingBeanRemote;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import uk.icat3.logging.entity.AdvKeyword;
import uk.icat3.logging.entity.AdvancedSearch;
import uk.icat3.logging.entity.InvInclude;
import uk.icat3.logging.entity.LogKeyword;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.entity.Search;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.KEYWORD_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class KeywordSearchMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    static Logger log;
    @EJB
    LoggingBeanRemote bean;

    public KeywordSearchMDB() {
    }

    public KeywordSearchMDB(EntityManager em) {
        this.em = em;
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(KeywordSearchMDB.class);
        log.info("===================Keyword message received======================");
        try {
            ObjectMessage msg = (ObjectMessage) message;
            String sessionId = msg.getStringProperty(PropertyNames.SESSION_ID);
            String userId = msg.getStringProperty(PropertyNames.USER_ID);
            String method = msg.getStringProperty(PropertyNames.METHOD);
            String time = msg.getStringProperty(PropertyNames.TIME);
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


            //Get extra properties- may not exist depending on method
            try {
                boolean caseSen = msg.getBooleanProperty(PropertyNames.CASE_SENSITIVE);
                int start = msg.getIntProperty(PropertyNames.START_INDEX);
                int noResults = msg.getIntProperty(PropertyNames.NO_RESULTS);
                String include = msg.getStringProperty(PropertyNames.INCLUDE);

                if (caseSen) {
                    adv.setCaseSensitive("Yes");
                }
                adv.setStartIndex(new Long(start));
                adv.setNoResults(new Long(noResults));

                InvInclude inv = bean.getInvIncludeByName(include);
                adv.setInvInclude1(inv);
            } catch (Exception e) {
                log.warn("One or more of these properties does not exist");
            }

            em.persist(adv);
            log.trace("AdvancedSearch persisted");

            ArrayList<String> keywords = (ArrayList<String>) msg.getObject();
            for (String word : keywords) {
                LogKeyword kw = bean.findKeyword(word);
                if (kw == null) {
                    kw = new LogKeyword();
                    kw.setKeyword(word);
                    em.persist(kw);
                    log.debug("Keyword persisted successfully");
                } else {
                    log.trace("Keyword found in database");
                }
                AdvKeyword akw = new AdvKeyword();
                akw.setAdvancedSearch(adv);
                akw.setLogKeyword(kw);
                em.persist(akw);
                log.trace("AdvKeyword persisted succesfully");
            }

        } catch (Exception e) {
            log.fatal("Error in keyword mdb", e);
        }


    }
}
