/*
 * Download.java
 *
 * Created on 17-Oct-2007, 13:05:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.data;

import static uk.icat3.data.DownloadConstants.CGI_NAME;
import static uk.icat3.data.DownloadConstants.DATAFILEID_NAME;
import static uk.icat3.data.DownloadConstants.DATASETID_NAME;
import static uk.icat3.data.DownloadConstants.DOWNLOAD_SCHEME;
import static uk.icat3.data.DownloadConstants.HOST_NAME;
import static uk.icat3.data.DownloadConstants.SESSIONID_NAME;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.data.DownloadConstants.ACTION;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DatafileManager;
import uk.icat3.manager.DatasetManager;
import uk.icat3.util.DatasetInclude;

/**
 * All methods for downloading file (ie getting the URL of the download service and giving it to the
 * user of ICAT so they can invoke it, normally a HTTP GET). The user needs READ and DOWNLOAD access
 * for this.
 * 
 * Also a method for the download service to check if the user has access to download what they have
 * requested through the HTTP GET
 * 
 * @author gjd37
 */
public class DownloadManager {

	static Logger log = Logger.getLogger(DownloadManager.class);

	/**
	 * Generates the download URL for the download of a single file. The method checks if the users
	 * has permission to download the file first
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param datafileId
	 *            datafile id
	 * @param manager
	 *            Entity manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return URL that will be used to download the data file
	 */
	public static String downloadDatafile(String userId, String sessionId, Long datafileId, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		Collection<Long> datafileIds = new ArrayList<Long>();
		datafileIds.add(datafileId);
		return downloadDatafiles(userId, sessionId, datafileIds, manager);
	}

	/**
	 * Generates the download URL for the download of a collection of files. The method checks if
	 * the users has permission to download the files first
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param datafileIds
	 *            collection of datafile ids
	 * @param manager
	 *            Entity manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return URL that will be used to download the collection of data files
	 */
	public static String downloadDatafiles(String userId, String sessionId, Collection<Long> datafileIds,
			EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		log.trace("downloadDatafiles(" + userId + ", " + sessionId + ", " + datafileIds + ", EntityManager)");

		// Check authz
		DatafileManager.getDataFiles(userId, datafileIds, manager);

		return generateDownloadUrl(datafileIds, sessionId);
	}

	/**
	 * Generates the download URL for the download of a data set. The method checks if the users has
	 * permission to download the dataset files first
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param datasetId
	 *            dataset id
	 * @param manager
	 *            Entity manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return URL that will be used to download the dataset
	 */
	public static String downloadDataset(String userId, String sessionId, Long datasetId, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		log.trace("downloadDataset(" + userId + ", " + sessionId + ", " + datasetId + ", EntityManager)");

		boolean zip = true;

		// get the dataset and its files.
		Dataset dataset = DatasetManager.getDataSet(userId, datasetId, DatasetInclude.DATASET_AND_DATAFILES_ONLY,
				manager);

		// if only one file, zip false
		if (dataset.getDatafileCollection().size() == 1) {
			zip = false;
		}

		return generateDownloadUrl(datasetId, sessionId, zip);
	}

	/**
	 * Generates the URL from the given dataset.
	 * 
	 * @param datasetId
	 *            dataset Id of the dataset that is to be downloaded
	 * @param sessionId
	 *            session id of the user for ICAT
	 * @return URL that will be used to download the datafiles
	 */
	private static String generateDownloadUrl(Long datasetId, String sessionId, boolean zip) {

		StringBuilder builder = new StringBuilder();
		String DOWNLOAD_ACTION = ACTION.ZIP.toString();
		if (!zip) {
			DOWNLOAD_ACTION = ACTION.DOWNLOAD.toString();
		}

		builder.append(DOWNLOAD_SCHEME + "://" + HOST_NAME + "/" + CGI_NAME + "?" + SESSIONID_NAME + "=" + sessionId
				+ "&action=" + DOWNLOAD_ACTION);
		builder.append("&" + DATASETID_NAME + "=" + datasetId);

		return builder.toString();
	}

	/**
	 * Generates the URL from the given datafiles.
	 * 
	 * @param datafiles
	 *            collection of the data files that are to be downloaded
	 * @param sessionId
	 *            session id of the user for ICAT
	 * @return URL that will be used to download the datafiles
	 */
	private static String generateDownloadUrl(Collection<Long> datafileIds, String sessionId) {

		StringBuilder builder = new StringBuilder();
		String DOWNLOAD_ACTION = ACTION.ZIP.toString();
		if (datafileIds.size() == 1) {
			DOWNLOAD_ACTION = ACTION.DOWNLOAD.toString();
		}

		builder.append(DOWNLOAD_SCHEME + "://" + HOST_NAME + "/" + CGI_NAME + "?" + SESSIONID_NAME + "=" + sessionId
				+ "&action=" + DOWNLOAD_ACTION);
		for (Long datafileId : datafileIds) {
			builder.append("&" + DATAFILEID_NAME + "=" + datafileId);
		}
		return builder.toString();
	}

	/**
	 * Checks if user has access to download the files.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param fileNames
	 *            names of the files that are to be downloaded
	 * @param manager
	 *            Entity manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return DownloadInfo information about the download
	 */
	public static DownloadInfo checkDatafileDownloadAccess(String userId, Collection<Long> datafileIds,
			EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		log.trace("checkFileDownloadAccess(" + userId + ", " + datafileIds + ", EntityManager)");

		// now check that the user has access to read, download
		downloadDatafiles(userId, "sessionIdDummy", datafileIds, manager);

		// this should be cached in single entity manager call
		Collection<Datafile> datafiles = DatafileManager.getDataFiles(userId, datafileIds, manager);

		Collection<String> fileNames = new ArrayList<String>();
		Collection<String> fileLocations = new ArrayList<String>();
		for (Datafile datafile : datafiles) {
			fileNames.add(datafile.getName());
			fileLocations.add(datafile.getLocation());
		}

		// create download info
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.setDatafileNames(fileNames);
		downloadInfo.setDatafileLocations(fileLocations);
		downloadInfo.setUserId(userId);

		// user had access
		return downloadInfo;
	}

	/**
	 * Checks if user has access to download the files.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param fileNames
	 *            names of the files that are to be downloaded
	 * @param manager
	 *            Entity manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return DownloadInfo information about the download
	 */
	public static DownloadInfo checkDatasetDownloadAccess(String userId, Long datasetId, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		log.trace("checkFileDownloadAccess(" + userId + ", " + datasetId + ", EntityManager)");

		// now check that the user has access to read, download
		downloadDataset(userId, "sessionIdDummy", datasetId, manager);

		// this should be cached in single entity manager call
		Dataset dataset = DatasetManager.getDataSet(userId, datasetId, DatasetInclude.DATASET_AND_DATAFILES_ONLY,
				manager);
		Collection<Datafile> datafiles = dataset.getDatafileCollection();

		Collection<String> fileNames = new ArrayList<String>();
		Collection<String> fileLocations = new ArrayList<String>();
		for (Datafile datafile : datafiles) {
			fileNames.add(datafile.getName());
			fileLocations.add(datafile.getLocation());
		}

		// create download info
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.setDatafileNames(fileNames);
		downloadInfo.setDatafileLocations(fileLocations);
		downloadInfo.setUserId(userId);

		// user had access
		return downloadInfo;
	}

	public static void main(String[] args) {
		System.out.println(ACTION.DOWNLOAD);
	}
}
