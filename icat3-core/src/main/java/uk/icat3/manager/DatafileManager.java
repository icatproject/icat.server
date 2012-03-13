package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

public class DatafileManager {

	static Logger log = Logger.getLogger(DatafileManager.class);

	public static List<Datafile> getDataFiles(String userId, Collection<Long> dataFileIds, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, IcatInternalException {
		log.trace("getDataFile(" + userId + ", " + dataFileIds + " EntityManager)");

		List<Datafile> dataFiles = new ArrayList<Datafile>();

		for (Long dataFileId : dataFileIds) {

			// check DataFile exist
			Datafile dataFile = manager.find(Datafile.class, dataFileId);
			if (dataFile == null) {
				throw new NoSuchObjectFoundException("Datafile [id:" + dataFileId + "] not found.");
			}

			// check user has read access
			GateKeeper.performAuthorisation(userId, dataFile, uk.icat3.manager.AccessType.READ, manager);

			// add to arraylist
			dataFiles.add(dataFile);
		}

		return dataFiles;
	}

	public static Datafile getDataFile(String userId, Long dataFileId, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, IcatInternalException {
		List<Long> dataFiles = new ArrayList<Long>();
		dataFiles.add(dataFileId);

		List<Datafile> datafiles = getDataFiles(userId, dataFiles, manager);
		return datafiles.iterator().next();
	}

}