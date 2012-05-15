package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.Datafile;


public class DatafileManager {

	static Logger log = Logger.getLogger(DatafileManager.class);

	public static List<Datafile> getDataFiles(String userId, Collection<Long> dataFileIds,
			EntityManager manager) throws IcatException {
		log.trace("getDataFile(" + userId + ", " + dataFileIds + " EntityManager)");

		List<Datafile> dataFiles = new ArrayList<Datafile>();

		for (Long dataFileId : dataFileIds) {

			// check DataFile exist
			Datafile dataFile = manager.find(Datafile.class, dataFileId);
			if (dataFile == null) {
				throw new IcatException(IcatException.IcatExceptionType.NO_SUCH_OBJECT_FOUND, "Datafile [id:"
						+ dataFileId + "] not found.");
			}

			// check user has read access
			GateKeeper.performAuthorisation(userId, dataFile, org.icatproject.core.manager.AccessType.READ,
					manager);

			// add to arraylist
			dataFiles.add(dataFile);
		}

		return dataFiles;
	}

	public static Datafile getDataFile(String userId, Long dataFileId, EntityManager manager)
			throws IcatException {
		List<Long> dataFiles = new ArrayList<Long>();
		dataFiles.add(dataFileId);

		List<Datafile> datafiles = getDataFiles(userId, dataFiles, manager);
		return datafiles.iterator().next();
	}

}