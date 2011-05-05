/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import uk.icat3.logging.entity.DatafileDownload;
import uk.icat3.logging.entity.DatasetInclude;
import uk.icat3.logging.entity.LogInstrument;
import uk.icat3.logging.entity.InvInclude;
import uk.icat3.logging.entity.LogInvestigator;
import uk.icat3.logging.entity.LogKeyword;
import uk.icat3.logging.entity.LoggedDownload;
import uk.icat3.user.User;

/**
 *
 * @author scb24683
 */
@Stateless
public class LoggingBean implements LoggingBeanRemote, LoggingBeanLocal {

    @PersistenceContext(unitName = "icat3-logging")
    private EntityManager em;
    static Logger log;
    User user;

    public LoggingBean() {
        log = Logger.getLogger(LoggingBean.class);
    }

    public LoggingBean(EntityManager emIn) {
        log = Logger.getLogger(LoggingBean.class);
        em = emIn;
    }

    public String getUserIdFromSessionId(String sessionId) {
        log.info("getUserIdFromSessionId called");
        try {
            Query q = em.createQuery("select l.userId from Login l where l.sessionId = ?1");
            String userId = (String) q.setParameter(1, sessionId).getSingleResult();
            log.info("User id " + userId);
            return userId;
        } catch (Exception e) {
            log.fatal("Error calling getUserIdFromSessionId", e);
            return null;
        }
    }

    public InvInclude getInvIncludeByName(String name) {
        log.info("getInvIncludeByName called");
        try {
            Query q = em.createQuery("select i from InvInclude i where i.name = ?1");
            InvInclude inv = (InvInclude) q.setParameter(1, name).getSingleResult();
            return inv;
        } catch (Exception e) {
            log.fatal("Error calling getInvIncludeByName", e);
            return null;
        }

    }

    public DatasetInclude getDatasetIncByName(String name) {
        log.info("getDatasetIncByName called");
        try {
            Query q = em.createQuery("select d from DatasetInclude d where d.name = ?1");
            DatasetInclude inc = (DatasetInclude) q.setParameter(1, name).getSingleResult();
            return inc;
        } catch (Exception e) {
            log.fatal("Error calling getDatasetIncByName", e);
            return null;
        }
    }

    public LogKeyword findKeyword(String keyword) {
        log.info("findKeyword called");
        try {
            Query q = em.createQuery("select k from LogKeyword k where k.keyword = ?1");
            LogKeyword key = (LogKeyword) q.setParameter(1, keyword).getSingleResult();
            return key;
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal("Error calling findKeyword", e);
            return null;
        }
    }

    public LogInstrument getInstrumentByName(String name) {
        log.info("getInstrumentByName called");
        try {
            Query q = em.createQuery("select i from LogInstrument i where i.name = ?1");
            LogInstrument i = (LogInstrument) q.setParameter(1, name).getSingleResult();
            return i;
        } catch (Exception e) {
            log.fatal("Error calling getInstrumentByName", e);
            return null;
        }
    }

    public LogInvestigator searchForInvestigator(String name) {
        log.info("searchForInvestigator called");
        try {
            Query q = em.createQuery("select i from LogInvestigator i where i.name = ?1");
            LogInvestigator i = (LogInvestigator) q.setParameter(1, name).getSingleResult();
            return i;
        } catch (Exception e) {
            log.fatal("Error calling searchForInvestigator", e);
            return null;
        }
    }

    public List<LoggedDownload> getDataDownloadsByInvestigation(String sessionId, Long investigationId) {
        log.info("getDataDownloadsByInvestigation called " + investigationId);
        try {
            Long userId = new Long(this.getUserIdFromSessionId(sessionId));
            log.info("User id: " + userId);
            Query q = em.createQuery("select f from DatafileDownload f where f.investigationId = ?1 and f.piId = ?2");
            List<DatafileDownload> files = (List<DatafileDownload>) q.setParameter(1, investigationId).setParameter(2, userId).getResultList();
            List<LoggedDownload> downloads = new ArrayList();
            for (int i = 0; i < files.size(); i++) {
                DatafileDownload file = files.get(i);
                LoggedDownload dl = new LoggedDownload();
                dl.setDatafileId(file.getDatafileId());
                dl.setInvestigationId(file.getInvestigationId());
                dl.setFilename(file.getFilename());
                dl.setFilesize(file.getFilesize().toString());
                dl.setUserId(file.getDownload().getUserId());
                downloads.add(dl);
            }
            return downloads;
        } catch (Exception e) {
            log.fatal("Error calling getDataDownloadsByInvestigation", e);
            return null;
        }
    }

    public List<LoggedDownload> getDataDownloadsByDatafile(String sessionId, Long datafileId) {
        log.info("getDataDownloadsByDatafile");
        try {
            Long userId = new Long(this.getUserIdFromSessionId(sessionId));
            Query q = em.createQuery("select f from DatafileDownload f where f.datafileId = ?1 and f.piId = ?2");
            List<DatafileDownload> files = (List<DatafileDownload>) q.setParameter(1, datafileId).setParameter(2, userId).getResultList();
            List<LoggedDownload> downloads = new ArrayList();
            for (int i = 0; i < files.size(); i++) {
                DatafileDownload file = files.get(i);
                LoggedDownload dl = new LoggedDownload();
                dl.setDatafileId(file.getDatafileId());
                dl.setInvestigationId(file.getInvestigationId());
                dl.setDatasetId(file.getDatasetId());
                dl.setFilename(file.getFilename());
                dl.setFilesize(file.getFilesize().toString());
                dl.setUserId(file.getDownload().getUserId());
                downloads.add(dl);
            }
            return downloads;
        } catch (Exception e) {
            log.fatal("Error calling getDataDownloadsByDatafile", e);
            return null;
        }
    }

    public List<LoggedDownload> getWhoDownloadedMyData(String sessionId) {
        log.info("getWhoDownloadedMyData called");
        try {
            Long userId = new Long(this.getUserIdFromSessionId(sessionId));
            Query q = em.createQuery("select d from DatafileDownload d where d.piId = ?1");
            List<DatafileDownload> files = (List<DatafileDownload>)q.setParameter(1, userId).getResultList();
            List<LoggedDownload> downloads = new ArrayList();
            for (int i = 0; i < files.size(); i++) {
                DatafileDownload file = files.get(i);
                LoggedDownload dl = new LoggedDownload();
                dl.setDatafileId(file.getDatafileId());
                dl.setInvestigationId(file.getInvestigationId());
                dl.setDatasetId(file.getDatasetId());
                dl.setFilename(file.getFilename());
                dl.setFilesize(file.getFilesize().toString());
                dl.setUserId(file.getDownload().getUserId());
                downloads.add(dl);
            }
            return downloads;
        } catch (Exception e) {
            log.fatal("Error calling getWhoDownloadedMyData", e);
            return null;
        }
    }

}
