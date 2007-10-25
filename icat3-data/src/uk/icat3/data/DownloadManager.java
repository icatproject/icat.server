/*
 * Download.java
 *
 * Created on 17-Oct-2007, 13:05:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.data;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import uk.ac.dl.srbapi.cog.CogUtil;
import uk.ac.dl.srbapi.srb.SRBFileManagerThread;
import uk.ac.dl.srbapi.srb.Url;
import uk.ac.dl.srbapi.util.Constants;
import uk.icat3.data.exceptions.DownloadException;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatasetInclude;

/**
 *
 * @author gjd37
 */
public class DownloadManager {
    
    static Logger log = Logger.getLogger(DownloadManager.class);
    
    public static File downloadDatafile(String userId, Long datafileId, String credential,  String facility, EntityManager manager)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        GSSCredential proxy = null;
        try {
            proxy = CogUtil.loadStringProxy(credential);
        } catch (Exception ex) {
            log.error("Unable to download data", ex);
            throw new DownloadException("Unable to download data, cause unknown");
        }
        
        return downloadDatafile(userId, datafileId, proxy, facility, manager);
    }
    
    public static File downloadDatafile(String userId, Long datafileId, GSSCredential credential, String facility, EntityManager manager)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException{
        log.trace("downloadDatafile("+userId+", "+datafileId+", credential, EntityManager)");
        
        Datafile dataFile = null;
        
        //get the datafile if has permissions to read file
        dataFile = DataFileManager.getDataFile(userId, datafileId, manager);
        
        //check permission to download
        GateKeeper.performAuthorisation(userId, dataFile, AccessType.DOWNLOAD, manager);
        
        Collection<Url> urls = new ArrayList<Url>();
        if(dataFile.getFileSize() == null) throw new DownloadException("No file size for "+dataFile+", unable to download.");
        if(dataFile.getLocation() == null) throw new MalformedURLException(dataFile+" has malformed URL: "+dataFile.getLocation());
        urls.add(new Url(dataFile.getLocation()));
        
        
        return DownloadManager.getData(urls, credential, facility);
    }
    
    public static File downloadDataset(String userId, Long datasetId, String credential, String facility,EntityManager manager)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        GSSCredential proxy = null;
        try {
            proxy = CogUtil.loadStringProxy(credential);
            
        } catch (Exception ex) {
            log.error("Unable to download data", ex);
            throw new DownloadException("Unable to download data, cause unknown");
        }
        return downloadDataset(userId, datasetId, proxy, facility, manager);
    }
    
    public static File downloadDataset(String userId, Long datasetId, GSSCredential credential, String facility, EntityManager manager)  throws SessionException, NoSuchObjectFoundException, NoSuchUserException, InsufficientPrivilegesException, MalformedURLException, DownloadException {
        log.trace("downloadDataset("+userId+", "+datasetId+", credential, EntityManager)");
        
        Dataset dataSet = null;
        
        //get the datafile if has permissions
        dataSet = DataSetManager.getDataSet(userId, datasetId, DatasetInclude.DATASET_AND_DATAFILES_ONLY, manager);
        
        Collection<Url> urls = new ArrayList<Url>();
        
        for (Datafile df : dataSet.getDatafileCollection()) {
            try{
                if(df.getFileSize() == null) throw new DownloadException("No file size for "+df+", not adding to lisr");
                urls.add(new Url(df.getLocation()));
            } catch(Exception ex){
                log.trace("Exception adding "+df+" to list to download, not adding",ex);
            }
        }
        if(urls.isEmpty()) throw new DownloadException(dataSet+" is emtpy, no files to download");
        
        return DownloadManager.getData(urls, credential, facility);
    }
    
    private static File getData(Collection<Url> fileUrls, String credential, String facility) throws DownloadException {
        log.trace("getData("+fileUrls+", credential)");
        try {
            GSSCredential proxy = CogUtil.loadProxy(credential);
            return getData(fileUrls, proxy, facility);
        } catch (Exception ex) {
            log.error("Unable to download data", ex);
            throw new DownloadException("Unable to download data");
        }
    }
    
    private static File getData(Collection<Url> fileUrls, GSSCredential credential, String facility) throws DownloadException {
        log.trace("getData("+fileUrls+", credential)");
        
        SRBFileManagerThread th = new SRBFileManagerThread(fileUrls, credential, false);
        Date today = new Date();
        th.setDownloadDir(Constants.DOWNLOAD_DIR+facility+File.separator+today.getDate()+"-"+(today.getMonth()+1)+File.separator+Math.random());
        
        //th.setIncludedErrors(new int[]{-2,-1});
        th.start();
        
        log.trace("Percent complete...");
        int i = 0;
        while(true){
            if(th.isFinished()){
                
                break;
            } else if(th.getException() != null){
                //System.out.println("exception "+th.getException());
                break;
            } else if(th.getException() == null){
                log.trace(""+th.getPercentageComplete()+" %");
                i++;
                try {
                    //first ten seconds check every half second, after 2 seconds
                    if(i < 20) Thread.sleep(500);
                    else Thread.sleep(2000);
                } catch (InterruptedException ex) {                    
                }
            }
        }
        
        log.trace("Finished download, awaiting error report.");
        System.out.println("");
        if(th.getException() == null){
            log.trace("No errors");
            log.trace("Returned file path is: "+th.getFile().getAbsolutePath());
            log.trace(th.getTimeStats());
            return th.getFile();
            
        } else {
            log.trace("Exception: "+th.getException());
            throw new DownloadException(th.getException().getMessage());
        }
    }
    
    public static void main(String[] args) throws MalformedURLException, DownloadException{
        Collection<Url> srbUrls = new ArrayList<Url>();
        srbUrls.add(new Url("file://c:/SRB-3.4.2-r5.tar.gz"));
        srbUrls.add(new Url("file://c:/out"));
        
        System.out.println(getData(srbUrls, new GSSCredential() {

            public void dispose() throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public GSSName getName() throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public GSSName getName(Oid mech) throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getRemainingLifetime() throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getRemainingInitLifetime(Oid mech) throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getRemainingAcceptLifetime(Oid mech) throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getUsage() throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getUsage(Oid mech) throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Oid[] getMechs() throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void add(GSSName name, int initLifetime, int acceptLifetime, Oid mech, int usage) throws GSSException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, "ISIS"));
    }
}
