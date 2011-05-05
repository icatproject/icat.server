/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.mdb;

import uk.icat3.logging.util.LoggingBeanRemote;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.logging.entity.DatafileView;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.entity.SimpleView;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.manager.DataFileManager;
import uk.icat3.logging.util.QueueNames;

/**
 *
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.VIEW_DATAFILE_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ViewDatafilesMDB implements MessageListener {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    @PersistenceContext(unitName = "icat3-exposed")
    private EntityManager exposed;
    static Logger log;
    @EJB
    LoggingBeanRemote bean;

    public ViewDatafilesMDB() {
    }

    public ViewDatafilesMDB(EntityManager em) {
        this.em = em;
    }

    public void onMessage(Message message) {
        log = Logger.getLogger(ViewDatafilesMDB.class);
        try {
            log.info("==============================View datafiles message received=========================");
            ObjectMessage msg = (ObjectMessage) message;
            String sessionId = msg.getStringProperty(PropertyNames.SESSION_ID);
            String userId = msg.getStringProperty(PropertyNames.USER_ID);
            String method = msg.getStringProperty(PropertyNames.METHOD);
            String time = msg.getStringProperty(PropertyNames.TIME);
            Timestamp viewTime = Timestamp.valueOf(time);

            SimpleView view = new SimpleView();
            view.setLogin(em.find(Login.class, sessionId));
            view.setUserId(userId);
            view.setMethod(method);
            view.setViewTime(viewTime);
            em.persist(view);
            log.trace("SimpleView persisted");

            ArrayList<Long> fileIds = (ArrayList<Long>) msg.getObject();
            for (Long id : fileIds) {
                log.debug("Data file id to be found: " + id);
                Datafile file = DataFileManager.getDataFile(userId, id, exposed);
                log.debug("========Some datafile info: " + file.getName());

                Dataset dataset = file.getDataset();
                Investigation investigation = dataset.getInvestigation();
                Collection<Investigator> invs = investigation.getInvestigatorCollection();
                String pi = null;
                for (Investigator inv : invs) {
                    if (inv.getRole().equals(PropertyNames.PI)) {
                        pi = inv.getFacilityUser().getFacilityUserId();
                        log.trace("Investigation admin found");
                        break;
                    }
                }

                DatafileView fileView = new DatafileView();
                fileView.setSimpleView(view);
                fileView.setDatafileId(id);
                fileView.setDatasetId(dataset.getId());
                fileView.setInvestigationId(investigation.getId());
                if (pi != null) {
                    fileView.setPiId(new Long(pi));
                }
                fileView.setInstrument(investigation.getInstrument());
                fileView.setFilename(file.getName());
                fileView.setFilesize(new Long(file.getFileSize()));
                if (file.getDatafileFormat() != null) {
                    fileView.setFormat(file.getDatafileFormat().getFormatType());
                }

                log.debug("File create time: " + file.getDatafileCreateTime());
                Date now = new Date();
                //calculate file age, result in days
                long diff = (now.getTime() - file.getDatafileCreateTime().getTime()) / (1000 * 60 * 60 * 24);

                log.debug("Date difference (file age): " + String.valueOf(diff));
                fileView.setAge(String.valueOf(diff));

                em.persist(fileView);
                log.trace("DatafileView persisted");
            }

        } catch (Exception e) {
            log.fatal("Error in view datafiles message", e);
        }
    }
}
