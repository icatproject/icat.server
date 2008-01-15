/*
 * TestInvalidUser.java
 *
 * Created on 07 March 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.data;

import java.util.ArrayList;
import java.util.Collection;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import org.apache.log4j.Logger;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.exceptions.*;
import static org.junit.Assert.*;
import static uk.icat3.util.TestConstants.*;

/**
 *
 * @author gjd37
 */
public class TestDatafileDownload extends BaseTestClassTX {

    private static Logger log = Logger.getLogger(TestDatafileDownload.class);

    /**
     * Tests datafile download
     */
    //@Test
    public void testDataFileDownload() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + " for user " + VALID_USER_FOR_INVESTIGATION);

        String url = DownloadManager.downloadDatafile(VALID_USER_FOR_INVESTIGATION, "sessionId", VALID_DATA_FILE_ID, em);

        log.trace("URL returned is " + url);
        assertNotNull("URL returned cannot be null", url);
        assertTrue("URL contains sid", url.contains("sid"));

        //only contains one name parameter
        int index = url.indexOf("name");
        String url2 = url.substring(index + 1, url.length());
        int index2 = url2.indexOf("name");
        assertTrue("URL contains one name parameter", index2 == -1);

    }

    /**
     * Tests datafile download fo invalid user
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testDataFileDownloadInvalidUser() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + " for user " + INVALID_USER);

        try {
            String url = DownloadManager.downloadDatafile(INVALID_USER, "sessionId", VALID_DATA_FILE_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'READ'", ex.getMessage().contains("READ"));
            throw ex;
        }
    }

    /**
     * Tests datafile download for only reader, not downloader
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testDataFileDownloadReaderUser() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + " for user " + READER_USER);

        try {
            String url = DownloadManager.downloadDatafile(READER_USER, "sessionId", VALID_DATA_FILE_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'DOWNLOAD'", ex.getMessage().contains("DOWNLOAD"));
            throw ex;
        }
    }

    /**
     * Tests dataset download
     */
    //@Test
    public void testDataSetDownload() throws ICATAPIException {
        log.info("Testing download dataset " + VALID_DATA_SET_ID + " for user " + VALID_USER_FOR_INVESTIGATION);

        String url = DownloadManager.downloadDataset(VALID_USER_FOR_INVESTIGATION, "sessionId", VALID_DATA_FILE_ID, em);

        log.trace("URL returned is " + url);
        assertNotNull("URL returned cannot be null", url);
        assertTrue("URL contains sid", url.contains("sid"));

        //only contains 2 name parameters
        int index = url.indexOf("name");
        String url2 = url.substring(index + 1, url.length());
        int index2 = url2.indexOf("name");
        String url3 = url2.substring(index2 + 1, url2.length());
        int index3 = url3.indexOf("name");
        assertTrue("URL contains 2 name parameters", index3 == -1);
    }

    /**
     * Tests dataset download invalid user
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testDataSetDownloadInvalidUser() throws ICATAPIException {
        log.info("Testing download dataset " + VALID_DATA_SET_ID + " for user " + INVALID_USER);

        try {
            String url = DownloadManager.downloadDataset(INVALID_USER, "sessionId", VALID_DATA_FILE_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'READ'", ex.getMessage().contains("READ"));
            throw ex;
        }
    }

    /**
     * Tests dataset download reader user
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testDataSetDownloadReaderUser() throws ICATAPIException {
        log.info("Testing download dataset " + VALID_DATA_SET_ID + " for user " + READER_USER);

        try {
            String url = DownloadManager.downloadDataset(READER_USER, "sessionId", VALID_DATA_FILE_ID, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'DOWNLOAD'", ex.getMessage().contains("DOWNLOAD"));
            throw ex;
        }
    }

    /**
     * Tests datafiles download
     */
    //@Test
    public void testDataFilesDownload() throws ICATAPIException {
        log.info("Testing download datafiles 3,57 for user " + VALID_USER_FOR_INVESTIGATION);

        Collection<Long> datafileIds = new ArrayList<Long>();
        datafileIds.add(57L);
        datafileIds.add(3L);

        String url = DownloadManager.downloadDatafiles(VALID_USER_FOR_INVESTIGATION, "sessionId", datafileIds, em);

        log.trace("URL returned is " + url);
        assertNotNull("URL returned cannot be null", url);
        assertTrue("URL contains sid", url.contains("sid"));

        //only contains 2 name parameters
        int index = url.indexOf("name");
        String url2 = url.substring(index + 1, url.length());
        int index2 = url2.indexOf("name");
        String url3 = url2.substring(index2 + 1, url2.length());
        int index3 = url3.indexOf("name");
        assertTrue("URL contains 2 name parameters", index3 == -1);
    }

    /**
     * Tests datafiles download
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testDataFilesDownloadInvalidUser() throws ICATAPIException {
        log.info("Testing download datafiles 3,57 for user " + INVALID_USER);

        Collection<Long> datafileIds = new ArrayList<Long>();
        datafileIds.add(57L);
        datafileIds.add(3L);

        try {
            String url = DownloadManager.downloadDatafiles(INVALID_USER, "sessionId", datafileIds, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'READ'", ex.getMessage().contains("READ"));
            throw ex;
        }
    }

    /**
     * Tests datafiles download reader user
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testDataFilesDownloadReaderUser() throws ICATAPIException {
        log.info("Testing download datafiles 3,57 for user " + READER_USER);

        Collection<Long> datafileIds = new ArrayList<Long>();
        datafileIds.add(57L);
        datafileIds.add(3L);

        try {
            String url = DownloadManager.downloadDatafiles(READER_USER, "sessionId", datafileIds, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'DOWNLOAD'", ex.getMessage().contains("DOWNLOAD"));
            throw ex;
        }
    }

    /**
     * Valid user check access test
     * 
     * @throws uk.icat3.exceptions.ICATAPIException
     */
    //@Test
    public void testAccessToDownload() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + ", name SXD015554.RAW for user " + VALID_USER_FOR_INVESTIGATION);

        Collection<String> fileNames = new ArrayList<String>();
        fileNames.add("SXD015554.RAW");
        boolean access = DownloadManager.checkFileDownloadAccess(VALID_USER_FOR_INVESTIGATION, fileNames, em);

        assertTrue("Access must be true", access);
    }
    
     /**
     * Valid user check access test
     * 
     * @throws uk.icat3.exceptions.ICATAPIException
     */
    @Test
    public void testAccessToDownload1() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + ",57 name SXD015554.RAW for user " + VALID_USER_FOR_INVESTIGATION);

        Collection<String> fileNames = new ArrayList<String>();
        fileNames.add("SXD015554.RAW");
        fileNames.add("SXD0155.RAW");
        
        boolean access = DownloadManager.checkFileDownloadAccess(VALID_USER_FOR_INVESTIGATION, fileNames, em);

        assertTrue("Access must be true", access);
    }

    /**
     * Reader user check access test
     * 
     * @throws uk.icat3.exceptions.ICATAPIException
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testAccessToDownloadReaderUser() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + ", name SXD015554.RAW for user " + READER_USER);

        Collection<String> fileNames = new ArrayList<String>();
        fileNames.add("SXD015554.RAW");

        try {
            boolean access = DownloadManager.checkFileDownloadAccess(READER_USER, fileNames, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'DOWNLOAD'", ex.getMessage().contains("DOWNLOAD"));
            throw ex;
        }
    }
    
    /**
     * Invalid user check access test
     * 
     * @throws uk.icat3.exceptions.ICATAPIException
     */
    //@Test(expected = InsufficientPrivilegesException.class)
    public void testAccessToDownloadInvalidUser() throws ICATAPIException {
        log.info("Testing download datafile " + VALID_DATA_FILE_ID + ", name SXD015554.RAW for user " + INVALID_USER);

        Collection<String> fileNames = new ArrayList<String>();
        fileNames.add("SXD015554.RAW");

        try {
            boolean access = DownloadManager.checkFileDownloadAccess(INVALID_USER, fileNames, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not have permission'", ex.getMessage().contains("does not have permission"));
            assertTrue("Exception must contain 'READ'", ex.getMessage().contains("READ"));
            throw ex;
        }
    }
    
     /**
     * Valid user check access for deleted test
     * 
     * @throws uk.icat3.exceptions.ICATAPIException
     */
    //@Test(expected = NoSuchObjectFoundException.class)
    public void testAccessToDownloadDeletedFile() throws ICATAPIException {
        log.info("Testing download datafile 56 name deleted.RAW for user " + VALID_USER_FOR_INVESTIGATION);

        Collection<String> fileNames = new ArrayList<String>();
        fileNames.add("deleted.RAW");

        try {
            boolean access = DownloadManager.checkFileDownloadAccess(VALID_USER_FOR_INVESTIGATION, fileNames, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not exist'", ex.getMessage().contains("does not exist"));            
            throw ex;
        }
    }
    
     /**
     * Valid user check access for deleted test
     * 
     * @throws uk.icat3.exceptions.ICATAPIException
     */
    //@Test(expected = NoSuchObjectFoundException.class)
    public void testAccessToDownloadInvalideletedFile() throws ICATAPIException {
        log.info("Testing download invalid datafile name 123456pp.stupid for user " + VALID_USER_FOR_INVESTIGATION);

        Collection<String> fileNames = new ArrayList<String>();
        fileNames.add("123456pp.stupid");

        try {
            boolean access = DownloadManager.checkFileDownloadAccess(VALID_USER_FOR_INVESTIGATION, fileNames, em);
        } catch (ICATAPIException ex) {
            log.warn("caught: " + ex.getClass() + " " + ex.getMessage());
            assertTrue("Exception must contain 'does not exist'", ex.getMessage().contains("does not exist"));            
            throw ex;
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestDatafileDownload.class);
    }
}
