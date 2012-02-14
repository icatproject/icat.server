package uk.icat3.logging.mdb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.logging.entity.DatafileDownload;
import uk.icat3.logging.entity.Download;
import uk.icat3.logging.entity.Login;
import uk.icat3.logging.util.LoggingBeanRemote;
import uk.icat3.logging.util.PropertyNames;
import uk.icat3.logging.util.QueueNames;
import uk.icat3.manager.BeanManager;

/**
 * 
 * @author scb24683
 */
@MessageDriven(mappedName = QueueNames.DOWNLOAD_QUEUE, activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class DownloadMDB implements MessageListener {

	@PersistenceContext(unitName = "icat3-logging")
	private EntityManager em;
	@PersistenceContext(unitName = "icat3-exposed")
	private EntityManager exposed;
	static Logger log;
	@EJB
	LoggingBeanRemote bean;

	public DownloadMDB() {
	}

	public DownloadMDB(EntityManager em) {
		this.em = em;
	}

	public void onMessage(Message message) {
		log = Logger.getLogger(DownloadMDB.class);
		log.info("======================Download message received=============================");
		try {
			String sessionId = message.getStringProperty(PropertyNames.SESSION_ID);
			String userId = message.getStringProperty(PropertyNames.USER_ID);
			String method = message.getStringProperty(PropertyNames.METHOD);
			String time = message.getStringProperty(PropertyNames.TIME);
			Timestamp downloadTime = Timestamp.valueOf(time);

			// create new generic download object
			Download download = new Download();
			download.setLogin(em.find(Login.class, sessionId));
			download.setUserId(userId);
			download.setDownloadTime(downloadTime);
			download.setMethod(method);
			em.persist(download);
			log.trace("Download persisted");

			Collection<Datafile> files = new ArrayList<Datafile>();

			if (message instanceof TextMessage) {
				// TextMessage is for downloadDataset
				TextMessage msg = (TextMessage) message;
				Long datasetId = new Long(msg.getText());
				Dataset dataset = (Dataset) BeanManager.get(userId, "Dataset", datasetId, exposed).getBean();
				files = dataset.getDatafiles();
			} else if (message instanceof ObjectMessage) {
				// ObjectMessage is for downloadDatafile and downloadDatafiles
				ObjectMessage msg = (ObjectMessage) message;
				@SuppressWarnings("unchecked")
				List<Long> ids = (List<Long>) msg.getObject();
				log.debug("==== Size of array list is " + ids.size() + " =====");
				for (Long fileId : ids) {
					log.debug("File id: " + fileId);
					Datafile datafile = (Datafile) BeanManager.get(userId, "Datafile", fileId, exposed).getBean();
					files.add(datafile);
				}
			}

			download.setNoFiles(new Long(files.size()));
			long totalSize = 0;

			for (Datafile file : files) {
				totalSize = totalSize + file.getFileSize();
				Dataset set = file.getDataset();
				Investigation i = set.getInvestigation();
				Collection<Investigator> invs = i.getInvestigators();
				String pi = null;
				for (Investigator in : invs) {
					// try to find Investigation PI
					if (in.getRole().equals(PropertyNames.PI)) {
						pi = in.getUser().getName();
						log.debug("Investigation admin found");
						break;
					}
				}

				DatafileDownload fileDownload = new DatafileDownload();
				fileDownload.setDatafileId(file.getId());
				fileDownload.setDatasetId(set.getId());
				fileDownload.setDownload(download);
				fileDownload.setFilesize(new Long(file.getFileSize()));
				fileDownload.setInvestigationId(i.getId());
				fileDownload.setInstrument(i.getInstrument().getName());
				if (file.getDatafileFormat() != null) {
					fileDownload.setFormat(file.getDatafileFormat().getFormatType());
				}
				if (pi != null) {
					fileDownload.setPiId(new Long(pi));
				}
				log.debug("File create time: " + file.getDatafileCreateTime());
				Date now = new Date();
				// Calculate file age (result in days)
				long diff = (now.getTime() - file.getDatafileCreateTime().getTime()) / (1000 * 60 * 60 * 24);

				log.debug("Date difference (file age): " + String.valueOf(diff));
				fileDownload.setAge(String.valueOf(diff));
				fileDownload.setFilename(file.getName());
				em.persist(fileDownload);

			}

			download.setTotalSize(new Long(totalSize));
			em.merge(download);

			log.trace("Download message completed");

		} catch (Exception e) {
			log.fatal("Error in DownloadMDB", e);
		}
	}

}
