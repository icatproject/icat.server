/*
 * Download.java
 *
 * Created on 17-Oct-2007, 13:05:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.data;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
public class DownloadManager {

    static Logger log = Logger.getLogger(DownloadManager.class);

    public static String downloadDatafile(String userId, Long datafileId, EntityManager manager) throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException {
        Collection<Long> datafileIds = new ArrayList<Long>();
        datafileIds.add(datafileId);
        return downloadDatafiles(userId, datafileIds, manager);
    }

    public static String downloadDatafiles(String userId, Collection<Long> datafileIds, EntityManager manager) throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException {
        log.trace("downloadDatafiles("+userId+", "+datafileIds+", EntityManager)");

        Collection<Datafile> datafiles = DataFileManager.getDataFiles(userId, datafileIds, manager);

        Collection<Datafile> validDatafiles = new ArrayList<Datafile>();
        for (Datafile datafile : datafiles) {
            if (datafile.getIcatRole().isActionDownload()) {
                validDatafiles.add(datafile);
            }
        }

        //check download
        if (!validDatafiles.isEmpty()) {
            return generateDownloadUrl(validDatafiles, userId);
        } else {
            if (datafileIds.size() == 1) {
                throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafiles.iterator().next());
            } else {
                throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafiles);

            }
        }
    }

    public static String downloadDataset(String userId, Long datasetId, EntityManager manager) throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException {
        log.trace("downloadDataset("+userId+", "+datasetId+", EntityManager)");

        Dataset dataset = DataSetManager.getDataSet(userId, datasetId, DatasetInclude.DATASET_AND_DATAFILES_ONLY, manager);
        Collection<Datafile> datafiles = dataset.getDatafileCollection();

        Collection<Datafile> validDatafiles = new ArrayList<Datafile>();

        for (Datafile datafile : datafiles) {
            if (datafile.getIcatRole().isActionDownload()) {
                validDatafiles.add(datafile);
            }
        }

        //check download
        if (dataset.getIcatRole().isActionDownload() && !validDatafiles.isEmpty()) {
            return generateDownloadUrl(validDatafiles, userId);
        } else {
            throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + dataset);
        }
    }

    private static String generateDownloadUrl(Collection<Datafile> datafiles, String userId) {

        StringBuilder builder = new StringBuilder();
        //builder.append("?sid="+sessionId+"&name="+file.getName()+"&file="+fileReturned);
        return builder.toString();
    }
}
