package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;

public class DatasetManager {

	static Logger log = Logger.getLogger(DatasetManager.class);

	static void filterDatafiles(String userId, Dataset dataset, boolean cascade, EntityManager manager) {
		// Changed, no need to check authorisation of single files, this is doneManagerUtil.
		// implicitily by the dataset authorisation
		try {
			GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
			dataset.getDatafileCollection();

			ManagerUtil.log.debug("Adding (allowed to read) " + dataset.getDatafileCollection().size()
					+ " datafiles to " + dataset + " from a total of " + dataset.getDatafileCollection().size());
		} catch (Exception ignore) {
			ManagerUtil.log.debug("Adding (allowed to read) 0 datafiles to " + dataset + " from a total of "
					+ dataset.getDatafileCollection().size());
		}
	}

	/**
	 * Gets the data set object from a data set id, depending if the user has access to read the
	 * data set.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param dataSetId
	 *            Id of object
	 * @param includes
	 *            other information wanted with the data set
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return {@link Dataset}
	 */
	public static Dataset getDataSet(String userId, Long dataSetId, DatasetInclude includes, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		DatasetManager.log.trace("getDataSet(" + userId + ", " + dataSetId + ", " + includes + ", EntityManager)");

		Collection<Long> datasets = new ArrayList<Long>();
		datasets.add(dataSetId);

		Collection<Dataset> datasetsReturned = DatasetManager.getDataSets(userId, datasets, includes, manager);
		return datasetsReturned.iterator().next();
	}

	/**
	 * Gets the data set object from a data set id, depending if the user has access to read the
	 * data set.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param dataSetId
	 *            Id of object
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return {@link Dataset}
	 */
	public static Dataset getDataSet(String userId, Long dataSetId, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		DatasetManager.log.trace("getDataSet(" + userId + ", " + dataSetId + " EntityManager)");

		Collection<Long> datasets = new ArrayList<Long>();
		datasets.add(dataSetId);

		Collection<Dataset> datasetsReturned = DatasetManager.getDataSets(userId, datasets, DatasetInclude.NONE,
				manager);
		return datasetsReturned.iterator().next();
	}

	/**
	 * Goes and collects the information associated with the dataset depending on the
	 * DatasetInclude. See {@link DatasetInclude}
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param datasets
	 *            collection of datasets for gettting more info about them
	 * @param include
	 *            include info
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 */
	public static void getDatasetInformation(String userId, Collection<Dataset> datasets, DatasetInclude include,
			EntityManager manager) {
		for (Dataset dataset : datasets) {
			// set the investigation includes in the class
			// This is because of JAXWS, it would down load all of the relationships with out this
			// workaround
			// See in Dataset.getInvestigatorCollection_() method
			dataset.setDatasetInclude(include);
			ManagerUtil.log.trace("Setting data sets to include: " + include);

			// now collect the information associated with the investigations requested
			if (include.isDatafiles()) {
				ManagerUtil.log.trace("Including datafiles");

				// size invokes the JPA to get the information, other wise the collections are null
				dataset.getDatafileCollection().size();
				// now filter the datafiles collection
				DatasetManager.filterDatafiles(userId, dataset, true, manager);
			}

			for (Datafile datafile : dataset.getDatafileCollection()) {
				if (include.isDatafilesAndParameters()) {
					ManagerUtil.log.trace("Setting data file to include: " + DatafileInclude.ALL);
					datafile.setDatafileInclude(DatafileInclude.ALL);
				} else {
					ManagerUtil.log.trace("Setting data file to include: " + DatafileInclude.NONE);
					datafile.setDatafileInclude(DatafileInclude.NONE);
				}
			}
		}
	}

	/**
	 * Gets the data sets objects from a list of data set ids, depending if the users has access to
	 * read the data sets. Also gets extra information regarding the data set. See
	 * {@link DatasetInclude}
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param dataSetIds
	 *            Ids of objects
	 * @param includes
	 *            other information wanted with the data set
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return collection of {@link Dataset}s
	 */
	public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, DatasetInclude includes,
			EntityManager manager) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		DatasetManager.log.trace("getDataSets(" + userId + ", " + dataSetIds + ", " + includes + ", EntityManager)");

		Collection<Dataset> datasets = new ArrayList<Dataset>();

		for (Long dataSetId : dataSetIds) {

			// check Dataset exist
			Dataset dataset = ManagerUtil.find(Dataset.class, dataSetId, manager);

			// check user has read access
			GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);

			// add to arraylist
			datasets.add(dataset);
		}

		// add include information
		DatasetManager.getDatasetInformation(userId, datasets, includes, manager);

		return datasets;
	}

	/**
	 * Gets the data sets objects from a list of data set ids, depending if the users has access to
	 * read the data sets
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param dataSetIds
	 *            Ids of objects
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return collection of {@link Dataset}s
	 */
	public static Collection<Dataset> getDataSets(String userId, Collection<Long> dataSetIds, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException {
		return DatasetManager.getDataSets(userId, dataSetIds, DatasetInclude.NONE, manager);
	}

}