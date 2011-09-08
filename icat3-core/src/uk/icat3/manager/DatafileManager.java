package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;

public class DatafileManager {

	static Logger log = Logger.getLogger(DatafileManager.class);

	/**
	 * Gets the data file objects from a list of data file ids, depending if the user has access to
	 * read the data files.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param dataFileIds
	 *            Ids of data files
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return a collection of {@link Datafile} objects
	 */
	public static Collection<Datafile> getDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		log.trace("getDataFile(" + userId + ", " + dataFileIds + " EntityManager)");

		Collection<Datafile> dataFiles = new ArrayList<Datafile>();

		for (Long dataFileId : dataFileIds) {

			// check DataFile exist
			Datafile dataFile = ManagerUtil.find(Datafile.class, dataFileId, manager);

			// check user has read access
			GateKeeper.performAuthorisation(userId, dataFile, AccessType.READ, manager);

			// add to arraylist
			dataFiles.add(dataFile);
		}

		return dataFiles;
	}

	/**
	 * Gets a data file object from a data file id, depending if the user has access to read the
	 * data file
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param dataFileId
	 *            Id of data file
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return {@link Datafile}
	 */
	public static Datafile getDataFile(String userId, Long dataFileId, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		Collection<Long> dataFiles = new ArrayList<Long>();
		dataFiles.add(dataFileId);

		Collection<Datafile> datafiles = getDataFiles(userId, dataFiles, manager);
		return datafiles.iterator().next();
	}

}