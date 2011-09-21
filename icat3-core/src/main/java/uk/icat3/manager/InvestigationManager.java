package uk.icat3.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.EntityManager;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;

public class InvestigationManager {
	
	
	private static void filterDatasets(String userId, Investigation investigation, boolean cascade,
			EntityManager manager) {
		Collection<Dataset> datasetsAllowed = new ArrayList<Dataset>();
		for (Dataset dataset : investigation.getDatasetCollection()) {
			try {
				GateKeeper.performAuthorisation(userId, dataset, AccessType.READ, manager);
				datasetsAllowed.add(dataset);

				// now filter datafiles
				if (cascade) {
					DatasetManager.filterDatafiles(userId, dataset, cascade, manager);
				}
			} catch (Exception ignore) {
			}
		}
		ManagerUtil.log.debug("Adding " + datasetsAllowed.size() + " datasets to " + investigation
				+ " from a total of " + investigation.getDatasetCollection().size());
		// now add the datasets to the investigation
		investigation.setDatasetCollection(datasetsAllowed);

	}


	public static void getInvestigationInformation(String userId, Collection<Investigation> investigations,
			InvestigationInclude include, EntityManager manager) {
		Iterator<Investigation> it = investigations.iterator();
		while (it.hasNext()) {
			try {
				GateKeeper.performAuthorisation(userId, it.next(), AccessType.READ, manager);
			} catch (InsufficientPrivilegesException ex) {
				it.remove();
			}
		}
		if (include != null) {
			if (include.toString().equals(InvestigationInclude.NONE.toString())) {
				// do nothing
				return;
			}
			for (Investigation investigation : investigations) {
				ManagerUtil.log.trace("Setting investigation to include: " + include);
				investigation.setInvestigationInclude(include);

				if (include.isInvestigators()) {
					ManagerUtil.log.trace("Including investigators");
					investigation.getInvestigatorCollection().size();
				}
				if (include.isKeywords()) {
					ManagerUtil.log.trace("Including keywords");
					investigation.getKeywordCollection().size();
				}
				if (include.isPublications()) {
					ManagerUtil.log.trace("Including publications");
					investigation.getPublicationCollection().size();
				}
				if (include.isSamples()) {
					ManagerUtil.log.trace("Including samples");
					investigation.getSampleCollection().size();
				}
				if (include.isShifts()) {
					ManagerUtil.log.trace("Including shifts");
					investigation.getShiftCollection().size();
				}
				if (include.isRoles()) {
					ManagerUtil.log.trace("Including roles");
					// get role (this adds the role)
					try {
						GateKeeper.performAuthorisation(userId, investigation, AccessType.READ, manager);
					} catch (InsufficientPrivilegesException ex) {
						ManagerUtil.log.fatal("User has not got access to investigation that search has returned", ex);
					}
				}

				if (include.isDatasetsAndDatafiles()) {
					ManagerUtil.log.trace("Including Datasets And Datafiles");
					investigation.getDatasetCollection().size();
					filterDatasets(userId, investigation, true, manager);
				} else if (include.isDatasets()) {
					ManagerUtil.log.trace("Including Datasets only");
					investigation.getDatasetCollection().size();
					filterDatasets(userId, investigation, false, manager);
				}

				// set the investigation includes in the class
				// This is because of JAXWS, it would down load all of the relationships with out
				// this workaround
				// See in Investigation.getInvestigatorCollection_() method
				if (include == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY) {
					for (Dataset dataset : investigation.getDatasetCollection()) {
						ManagerUtil.log
								.trace("Setting data sets to include: " + DatasetInclude.DATASET_PARAMETERS_ONLY);
						dataset.setDatasetInclude(DatasetInclude.DATASET_PARAMETERS_ONLY);
					}
				} else if (include.isDatasetsAndDatafiles() && !include.isDatasetsDatafilesAndParameters()) {
					for (Dataset dataset : investigation.getDatasetCollection()) {
						ManagerUtil.log.trace("Setting data sets to include: "
								+ DatasetInclude.DATASET_AND_DATAFILES_ONLY);
						dataset.setDatasetInclude(DatasetInclude.DATASET_AND_DATAFILES_ONLY);
						for (Datafile datafile : dataset.getDatafileCollection()) {
							ManagerUtil.log.trace("Setting data file to include: " + DatafileInclude.NONE);
							datafile.setDatafileInclude(DatafileInclude.NONE);
						}
					}
				} // override above if only want datafiles and not its parameters
				else if (include.isDatasetsDatafilesAndParameters()) {
					for (Dataset dataset : investigation.getDatasetCollection()) {
						ManagerUtil.log.trace("Setting data sets to include: "
								+ DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS);
						dataset.setDatasetInclude(DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS);
						for (Datafile datafile : dataset.getDatafileCollection()) {
							ManagerUtil.log.trace("Setting data file to include: " + DatafileInclude.ALL);
							datafile.setDatafileInclude(DatafileInclude.ALL);
						}
					}
				}
			}
		}
	}

	

	/**
	 * Returns a list of {@link Investigation} investigations from a list of {@link Investigation}
	 * investigation ids if the user has access to the investigations. Also gets extra information
	 * regarding the investigation. See {@link InvestigationInclude}
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param investigationIds
	 *            Ids of investigations
	 * @param includes
	 *            information that is needed to be returned with the investigation
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return collection of {@link Investigation} investigation objects
	 */
	public static Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds,
			InvestigationInclude includes, EntityManager manager) throws InsufficientPrivilegesException,
			NoSuchObjectFoundException {

		Collection<Investigation> investigations = new ArrayList<Investigation>();

		for (Long investigationId : investigationIds) {

			// check investigation exist
			Investigation investigation = ManagerUtil.find(Investigation.class, investigationId, manager);

			// check user has read access
			GateKeeper.performAuthorisation(userId, investigation, AccessType.READ, manager);

			// add to arraylist
			investigations.add(investigation);
		}

		// add include information
		getInvestigationInformation(userId, investigations, includes, manager);

		return investigations;
	}

	/**
	 * Returns a {@link Investigation} investigation from a {@link Investigation} id if the user has
	 * access to the investigation.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param investigationId
	 *            Id of investigations
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return {@link Investigation} object
	 */
	public static Investigation getInvestigation(String userId, Long investigationId, EntityManager manager)
			throws InsufficientPrivilegesException, NoSuchObjectFoundException {
		Collection<Long> investigationIds = new ArrayList<Long>();
		investigationIds.add(investigationId);
		return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager).iterator().next();
	}

	/**
	 * Returns a list of {@link Investigation} investigations from a list of {@link Investigation}
	 * investigation ids if the user has access to the investigations.
	 * 
	 * @param userId
	 *            federalId of the user.
	 * @param investigationIds
	 *            Ids of investigations
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return collection of {@link Investigation} investigation objects
	 */
	public static Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds,
			EntityManager manager) throws InsufficientPrivilegesException, NoSuchObjectFoundException {
		return getInvestigations(userId, investigationIds, InvestigationInclude.NONE, manager);
	}

}