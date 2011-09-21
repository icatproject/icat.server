/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.mdb;

import uk.icat3.logging.util.LoggingBeanRemote;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
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
import uk.icat3.manager.InvestigationManager;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.VIEW_INVESTIGATION_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ViewInvestigationsMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    @PersistenceContext(unitName = "icat3-exposed")
    private EntityManager exposed;
    static Logger log;
    @EJB
    LoggingBeanRemote bean;

    public ViewInvestigationsMDB() {
    }

    public ViewInvestigationsMDB(EntityManager em) {
        this.em = em;
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(ViewInvestigationsMDB.class);
        log.info("=================View investigations message received=======================");
        try {
            ObjectMessage msg = (ObjectMessage) message;
            String sessionId = msg.getStringProperty(PropertyNames.SESSION_ID);
            String userId = msg.getStringProperty(PropertyNames.USER_ID);
            String method = msg.getStringProperty(PropertyNames.METHOD);
            String time = msg.getStringProperty(PropertyNames.TIME);
            Timestamp viewTime = Timestamp.valueOf(time);

            SimpleView view = new SimpleView();
            view.setLogin(em.find(Login.class, sessionId));
            view.setViewTime(viewTime);
            view.setMethod(method);
            view.setUserId(userId);
            em.persist(view);
            log.trace("SimpleView persisted");

            InvInclude include = null;
            try {
                String inc = msg.getStringProperty(PropertyNames.INCLUDE);
                include = bean.getInvIncludeByName(inc);
            } catch (Exception e) {
                log.warn("InvInclude not found");
            }

            ArrayList<Long> invIds = (ArrayList<Long>) msg.getObject();
            for (Long id : invIds) {
                Investigation i = InvestigationManager.getInvestigation(userId, id, exposed);
                InvestigationView invView = new InvestigationView();
                invView.setSimpleView(view);
                invView.setInvestigationId(i.getId());
                invView.setInstrument(i.getInstrument());
                if (include != null) {
                    invView.setInvInclude(include);
                }
                Collection<Investigator> ppl = i.getInvestigatorCollection();
                String pi = null;
                for (Investigator in : ppl) {
                    //try to find investigation PI
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
            log.debug("Message bean completed");

        } catch (Exception e) {
           log.fatal("Error in ViewInvestigationsMDB", e);
        }
    }
}
