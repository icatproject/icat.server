/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.mdb;

import uk.icat3.logging.util.LoggingBeanRemote;
import java.sql.Timestamp;
import java.util.Collection;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.logging.entity.InvInclude;
import uk.icat3.logging.entity.InvestigationView;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.entity.SimpleView;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.VIEW_MY_INV_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ViewMyInvestigationsMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    @PersistenceContext(unitName = "icat3-exposed")
    private EntityManager exposed;
    static Logger log;
    @EJB
    LoggingBeanRemote bean;

    public ViewMyInvestigationsMDB() {
    }

    public ViewMyInvestigationsMDB(EntityManager em) {
        this.em = em;
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(ViewMyInvestigationsMDB.class);
        log.info("=============ViewMyInvestigations message received==================");
        try {
            TextMessage myMsg = (TextMessage) message;
            String sessionId = myMsg.getStringProperty(PropertyNames.SESSION_ID);
            String userId = myMsg.getText();
            String time = myMsg.getStringProperty(PropertyNames.TIME);
            String method = myMsg.getStringProperty(PropertyNames.METHOD);
            Timestamp viewTime = Timestamp.valueOf(time);

            SimpleView view = new SimpleView();
            view.setLogin(em.find(Login.class, sessionId));
            view.setUserId(userId);
            view.setViewTime(viewTime);
            view.setMethod(method);
            em.persist(view);
            log.trace("SimpleView persisted");

            InvInclude include = null;
            try {
                String inc = myMsg.getStringProperty(PropertyNames.INCLUDE);
                include = bean.getInvIncludeByName(inc);
            } catch (Exception e) {
                log.warn("No investigation include property found");
            }

            String start = null;
            String no = null;

            try {
                start = myMsg.getStringProperty(PropertyNames.START_INDEX);
                no = myMsg.getStringProperty(PropertyNames.NO_RESULTS);
            } catch (Exception e) {
                log.warn("Properties not found");
            }

            Collection<Investigation> invs = InvestigationSearch.getUsersInvestigations(userId, exposed);
            for (Investigation i : invs) {
                InvestigationView invView = new InvestigationView();
                invView.setSimpleView(view);
                invView.setInvestigationId(i.getId());
                invView.setInstrument(i.getInstrument());
                if (include != null) {
                    invView.setInvInclude(include);
                }
                if (start != null) {
                    invView.setStartIndex(new Long(start));
                }
                if (no != null) {
                    invView.setNoResults(new Long(no));
                }
                Collection<Investigator> ppl = i.getInvestigatorCollection();
                String pi = null;
                for (Investigator in : ppl) {
                    //try to get investigation PI
                    if (in.getRole().equals(PropertyNames.PI)) {
                        pi = in.getFacilityUser().getFacilityUserId();
                        log.trace("Investigation admin found");
                        break;
                    }
                }
                if (pi != null) {
                    invView.setPiId(new Long(pi));
                }
                em.persist(invView);
                log.trace("investigation view persisted");
            }




        } catch (Exception e) {
            log.fatal("Error in ViewMyInvestigationsMDB", e);
        }
    }
}
