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
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.logging.entity.DatasetInclude;
import uk.icat3.logging.entity.DatasetView;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.entity.SimpleView;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.manager.DataSetManager;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.VIEW_DATASET_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ViewDatasetsMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    @PersistenceContext(unitName = "icat3-exposed")
    private EntityManager exposed;
    static Logger log;
    @EJB
    LoggingBeanRemote bean;

    public ViewDatasetsMDB() {
    }

    public ViewDatasetsMDB(EntityManager em) {
        this.em = em;
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(ViewDatasetsMDB.class);
        log.info("===========================Dataset View message received=============================");
        try {
            ObjectMessage msg = (ObjectMessage) message;
            String sessionId = msg.getStringProperty(PropertyNames.SESSION_ID);
            String userId = msg.getStringProperty(PropertyNames.USER_ID);
            String time = msg.getStringProperty(PropertyNames.TIME);
            String method = msg.getStringProperty(PropertyNames.METHOD);
            Timestamp viewTime = Timestamp.valueOf(time);

            SimpleView view = new SimpleView();
            view.setLogin(em.find(Login.class, sessionId));
            view.setUserId(userId);
            view.setViewTime(viewTime);
            view.setMethod(method);
            em.persist(view);
            log.trace("SimpleView persisted");


            ArrayList<Long> setIds = (ArrayList<Long>) msg.getObject();
            DatasetInclude include = null;
            try {
                String incName = msg.getStringProperty(PropertyNames.INCLUDE);
                include = bean.getDatasetIncByName(incName);
            } catch (Exception e) {
                log.warn("Property not found");
            }

            for (Long setId : setIds) {
                Dataset dataset = DataSetManager.getDataSet(userId, setId, exposed);
                Investigation investigation = dataset.getInvestigation();
                Collection<Investigator> invs = investigation.getInvestigatorCollection();
                String pi = null;
                for (Investigator inv : invs) {
                    //try to find investigation PI
                    if (inv.getRole().equals(PropertyNames.PI)) {
                        pi = inv.getFacilityUser().getFacilityUserId();
                        log.trace("Investigation admin found");
                        break;
                    }
                }

                DatasetView datasetView = new DatasetView();
                datasetView.setSimpleView(view);
                datasetView.setDatasetId(dataset.getId());
                datasetView.setInvestigationId(investigation.getId());
                if (pi != null) {
                    datasetView.setPiId(new Long(pi));
                }
                datasetView.setInstrument(investigation.getInstrument());
                if (include != null) {
                    datasetView.setDatasetInclude(include);
                }

                em.persist(datasetView);
                log.trace("DatasetView persisted");
            }

        } catch (Exception e) {
            log.fatal("Error in dataset view mdb", e);
        }
    }
}
