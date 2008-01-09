/*
 * DownloadManagerBean.java
 *
 * Created on 17-Oct-2007, 14:09:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.data;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.xml.ws.soap.MTOM;
import org.apache.log4j.Logger;
import uk.icat3.data.DownloadManager;
import uk.icat3.data.exceptions.DownloadException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.user.UserDetails;
import uk.ac.dl.srbapi.srb.Url;
import uk.ac.dl.srbapi.util.IOTools;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
@Stateless
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DownloadManagerBean extends EJBObject implements DownloadManagerLocal {

    static Logger log = Logger.getLogger(DownloadManagerBean.class);

    public String downloadDatafile(String sessionId, Long datafileId) throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException {

        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        Datafile datafile = DataFileManager.getDataFile(userId, datafileId, manager);

        Collection<Datafile> validDatafiles = new ArrayList<Datafile>();
        validDatafiles.add(datafile);
        
        //check download
        if (datafile.getIcatRole().isActionDownload()) {
            return generateDownloadUrl(validDatafiles, sessionId);
        } else {
            throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + datafile);
        }
    }

    public String downloadDataset(String sessionId, Long datasetId) throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException {

        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

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
            return generateDownloadUrl(validDatafiles, sessionId);
        } else {
            throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform 'DOWNLOAD' operation on " + dataset);
        }
    }

    private String generateDownloadUrl(Collection<Datafile> datafiles, String sessionId) {

        StringBuilder builder = new StringBuilder();
        //builder.append("?sid="+sessionId+"&name="+file.getName()+"&file="+fileReturned);
        return builder.toString();
    }
}
