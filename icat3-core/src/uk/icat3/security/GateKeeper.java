package uk.icat3.security;

import org.apache.log4j.Logger;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.InsufficientPrivilegesException;

import uk.icat3.util.AccessType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.FacilityInstrumentScientist;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;

import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import uk.icat3.util.Queries;

/**
 * This grants or denies access to all the objects within the database
 */
public class GateKeeper {

	/** Creates a new instance of GateKeeper */
	public GateKeeper() {
	}

	// Global class logger
	static Logger log = Logger.getLogger(GateKeeper.class);

	/**
	 * Decides if a user has permission to perform an operation of type {@link AccessType} on a
	 * {@link Study} element/entity. If the user does not have permission to perform aforementioned
	 * operation then an {@link InsufficientPrivilegesException} will be thrown.
	 * 
	 * <p>
	 * A Study can have multiple Investigations, so find them all and use each one to check
	 * authorisation. If a user has authorisation on any one of the investigations contained within
	 * the study then those permissions are extended to the parent Study element.
	 * </p>
	 * 
	 * @param user
	 *            username or dn of user who is to be authorised.
	 * @param object
	 *            object entitybasebean of the element/entity that the user wishes to perform
	 *            operation on.
	 * @param access
	 *            type of operation that the user is trying to perform.
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws InsufficientPrivilegesException
	 *             if user does not have permission to perform operation.
	 */
	public static IcatRole performAuthorisation(String user, EntityBaseBean object, AccessType access,
			EntityManager manager) throws InsufficientPrivilegesException {
		// this id of the root parent of object (ie parent of root element), ie inv, ds, df
		Long rootElementsParentsId = null;
		// this id of the root element of object, ie inv, ds, df
		Long rootElementId = null;
		if (object instanceof Publication) {
			rootElementsParentsId = ((Publication) object).getInvestigationId().getId();
			rootElementId = ((Publication) object).getInvestigationId().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.PUBLICATION, manager);
		} else if (object instanceof Investigation) {
			rootElementsParentsId = ((Investigation) object).getId();
			rootElementId = ((Investigation) object).getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.INVESTIGATION, manager);
		} else if (object instanceof Keyword) {
			rootElementsParentsId = ((Keyword) object).getInvestigation().getId();
			rootElementId = ((Keyword) object).getInvestigation().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.KEYWORD, manager);
		} else if (object instanceof Dataset) {
			rootElementsParentsId = ((Dataset) object).getInvestigation().getId();
			rootElementId = ((Dataset) object).getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.DATASET, manager);
		} else if (object instanceof Datafile) {
			// Changed. Need to check the dataset parent for data file now.
			// rootElementsParentsId = ((Datafile) object).getDataset().getId();
			// rootElementId = ((Datafile) object).getId();
			rootElementsParentsId = ((Datafile) object).getDataset().getInvestigation().getId();
			rootElementId = ((Datafile) object).getDataset().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.DATASET /* ElementType.DATAFILE */, manager);
		} else if (object instanceof DatasetParameter) {
			rootElementsParentsId = ((DatasetParameter) object).getDataset().getInvestigation().getId();
			rootElementId = ((DatasetParameter) object).getDataset().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.DATASET_PARAMETER, manager);
		} else if (object instanceof DatafileParameter) {
			// Changed. Need to check the dataset parent for data file parameter now.
			// rootElementsParentsId = ((DatafileParameter)
			// object).getDatafile().getDataset().getId();
			// rootElementId = ((DatafileParameter) object).getDatafile().getId();
			rootElementsParentsId = ((DatafileParameter) object).getDatafile().getDataset().getInvestigation().getId();
			rootElementId = ((DatafileParameter) object).getDatafile().getDataset().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.DATASET /* ElementType.DATAFILE_PARAMETER */, manager);
		} else if (object instanceof SampleParameter) {
			rootElementsParentsId = ((SampleParameter) object).getSample().getInvestigationId().getId();
			rootElementId = ((SampleParameter) object).getSample().getInvestigationId().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.SAMPLE_PARAMETER, manager);
		} else if (object instanceof Sample) {
			rootElementsParentsId = ((Sample) object).getInvestigationId().getId();
			rootElementId = ((Sample) object).getInvestigationId().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId, ElementType.SAMPLE,
					manager);
		} else if (object instanceof Investigator) {
			rootElementsParentsId = ((Investigator) object).getInvestigation().getId();
			rootElementId = ((Investigator) object).getInvestigation().getId();
			return performAuthorisation(user, rootElementsParentsId, access, object, rootElementId,
					ElementType.INVESTIGATOR, manager);
			// } else if(object instanceof Study){
			// for (StudyInvestigation si : ((Study)object).getStudyInvestigationCollection()) {
			// invList.add(si.getInvestigation());
			// }//end for
			// performAuthorisation(user, invList, access, ((DatafileParameter)object), manager);
		} else {
			throw new InsufficientPrivilegesException(object.getClass().getSimpleName()
					+ " not supported for security check.");
		}

	}// end method

	/**
	 * Private method that ultimately does the low-level permission check against the database. This
	 * method retrieves all permission elements associated with a given user and investigation pair.
	 * If user has been granted the appropriate access permission in the database then the method
	 * returns without error. Otherwise an exception with appropriate details is raised, logged and
	 * thrown back to the caller.
	 * 
	 * @param user
	 *            username or dn of user who is to be authorised.
	 * @param investigations
	 *            collection if elements/entities that the user wishes to perform operation on.
	 * @param access
	 *            type of operation that the user is trying to perform.
	 * @param element
	 *            name of element/entity type that user is really trying to access in some way e.g.
	 *            datafile. This is used for purposes only.
	 * @param elementId
	 *            primary key of specific element/entity that user is trying to access.
	 * @param manager
	 *            manager object that will facilitate interaction with underlying database
	 * @throws InsufficientPrivilegesException
	 *             if user does not have permission to perform operation.
	 */
	private static IcatRole performAuthorisation(String userId, Long rootElementsParentId, AccessType access,
			EntityBaseBean object, Long rootParentsId, ElementType elementType, EntityManager manager)
			throws InsufficientPrivilegesException {
		log.debug("performAuthorisation(): " + userId + ", AccessType: " + access + ", " + object + ", rootElementId: "
				+ rootParentsId + ", rootElementsParent: " + rootElementsParentId);
		IcatAuthorisation icatAuthorisation = null;
		boolean success = false;
		IcatRole role = null;

		// check if the user is super user name, therefore superuser role
		boolean isSuperUser = (IcatRoles.SUPER_USER.toString().equals(userId)) ? true : false;

		// get icat authroisation
		if (!isSuperUser) {
			try {
				if (elementType.isInvestigationType()) {
					icatAuthorisation = findIcatAuthorisation(userId, ElementType.INVESTIGATION, rootParentsId,
							rootElementsParentId, object, manager);
				} else if (elementType.isDatasetType()) {
					icatAuthorisation = findIcatAuthorisation(userId, ElementType.DATASET, rootParentsId,
							rootElementsParentId, object, manager);
				} else if (elementType.isDatafileType()) {
					icatAuthorisation = findIcatAuthorisation(userId, ElementType.DATAFILE, rootParentsId,
							rootElementsParentId, object, manager);
				} else {
					throw new RuntimeException("Element Type: " + elementType + " not supported");
				} // should never be thrown

				// if role null, ie facility scientist then return super
				if (icatAuthorisation == null) {
					IcatRole superRole = manager.find(IcatRole.class, "SUPER");
					role = superRole;
					icatAuthorisation = new IcatAuthorisation();
					icatAuthorisation.setRole(superRole);
				} else {
					role = icatAuthorisation.getRole();
				}
				log.debug("IcatAuthorisation " + icatAuthorisation + " with Role: " + icatAuthorisation.getRole());
			} catch (InsufficientPrivilegesException ipe) {
				log.warn("User: " + userId + " does not have permission to perform '" + access + "' operation on "
						+ object);
				throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform '"
						+ access + "' operation on " + object);
			}
		} else {
			// super user role
			IcatRole superRole = manager.find(IcatRole.class, "SUPER");
			role = superRole;
			icatAuthorisation = new IcatAuthorisation();
			icatAuthorisation.setRole(superRole);
		}

		// now check the access permission from the icat authroisation
		if (isSuperUser) {
			success = true; // user has access to do anything
		} else if (access == AccessType.READ) {
			if (role.isActionSelect()) {
				success = true; // user has access to read element
			}
		} else if (access == AccessType.REMOVE) {
			// cannot do if facility acquired
			if (!object.isFacilityAcquiredSet()) {
				// check if element type is a root, if so check root remove permissions
				if (object instanceof Dataset || object instanceof Investigation && object instanceof Datafile) {
					log.trace("Create ID: " + object.getCreateId() + " " + userId.equals(object.getCreateId()) + " "
							+ icatAuthorisation.getRole());
					// to remove something, create Id must also be the same as your Id.
					if (role.isActionRootRemove() && userId.equals(object.getCreateId())) {
						success = true; // user has access to remove root element
					}
				} else if (role.isActionRemove() && userId.equals(object.getCreateId())) {
					success = true; // user has access to remove element
				}
			}
		} else if (access == AccessType.CREATE) { // ie INSERT
			// check if element type is a root, if so check root insert permissions
			if (object instanceof Dataset || object instanceof Investigation) {
				log.trace("Trying to create a root type: " + elementType);
				log.trace("Element Type: " + elementType + ", AUTH: " + icatAuthorisation + " for " + object);
				// if null in investigation id then can create investigations
				if (elementType == ElementType.INVESTIGATION && icatAuthorisation.getElementId() == null
						&& role.isActionRootInsert()) {
					success = true; // user has access to insert root root element
				} else if (elementType == ElementType.DATASET && icatAuthorisation.getElementId() == null
						&& icatAuthorisation.getParentElementType() == ElementType.INVESTIGATION
						&& icatAuthorisation.getParentElementId().equals(((Dataset) object).getInvestigation().getId())
						&& role.isActionRootInsert()) {
					success = true; // user has access to insert root root element
				} else if (elementType == ElementType.DATAFILE && icatAuthorisation.getElementId() == null
						&& icatAuthorisation.getParentElementType() == ElementType.DATASET
						&& icatAuthorisation.getParentElementId().equals(((Datafile) object).getDataset().getId())
						&& role.isActionRootInsert()) {
					success = true; // user has access to insert root root element
				}
			} else if (object instanceof Datafile && role.isActionRootInsert()) {
				success = true; // user has access to insert root root element
			} else if (role.isActionInsert()) {
				success = true; // user has access to insert element
			}
		} else if (access == AccessType.UPDATE) {
			// cannot do if facility acquired
			if (role.isActionUpdate() && !object.isFacilityAcquiredSet()) {
				success = true; // user has access to update element
			}
		} else if (access == AccessType.DELETE) {
			// cannot do if facility acquired
			if (role.isActionDelete() && !object.isFacilityAcquiredSet()) {
				success = true; // user has access to delete element
			}
		} else if (access == AccessType.DOWNLOAD) {
			if (role.isActionDownload()) {
				success = true; // user has access to download element
			}
		} else if (access == AccessType.SET_FA) {
			if (role.isActionFacilityAcquired()) {
				success = true; // user has access to set FA element
			}
		} else if (access == AccessType.MANAGE_USERS) {
			if (role.isActionManageUsers()) {
				success = true; // user has access to set manage users
			}
		}

		// now check if has permission and if not throw exception
		if (success) {
			// now append the role to the investigation, dataset or datafile
			if (elementType.isRootType()) {
				object.setIcatRole(role);
			}
			log.info("User: " + userId + " granted " + access + " permission on " + object + " with role " + role);
			return role;
		} else {
			log.warn("User: " + userId + " does not have permission to perform '" + access + "' operation on " + object);
			throw new InsufficientPrivilegesException("User: " + userId + " does not have permission to perform '"
					+ access + "' operation on " + object);
		}
	}

	/**
	 * This finds the IcatAuthorisation for the user, it first tries to find the one corresponding
	 * to the users fedId, if this is not found, it looks for the ANY as the fedId
	 * 
	 */
	private static IcatAuthorisation findIcatAuthorisation(String userId, ElementType type, Long id, Long parentId,
			EntityBaseBean object, EntityManager manager) throws InsufficientPrivilegesException {
		if (!type.isRootType()) {
			throw new RuntimeException("ElementType: " + type + " not supported");
		}
		log.trace("Looking for ICAT AUTHORISATION: UserId: " + userId + ", rootElementId: " + id + ", type: " + type
				+ ", rootElementsParentId: " + parentId);

		IcatAuthorisation icatAuthorisation = null;
		Query nullSearchQuery = null;

		if (id == null) {
			log.trace("user " + userId + " trying to create a " + type);

			if (type == ElementType.INVESTIGATION) {
				nullSearchQuery = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_CREATE_INVESTIGATION);
			} else if (type == ElementType.DATASET) {
				nullSearchQuery = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_CREATE_DATAFILE_DATASET);
				nullSearchQuery.setParameter("parentElementType", ElementType.INVESTIGATION);
				nullSearchQuery.setParameter("parentElementId", parentId);
			} else if (type == ElementType.DATAFILE) {
				nullSearchQuery = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_CREATE_DATAFILE_DATASET);
				nullSearchQuery.setParameter("parentElementType", ElementType.DATASET);
				nullSearchQuery.setParameter("parentElementId", parentId);
			}

			// try and find user with null as investigation
			nullSearchQuery.setParameter("elementType", type).setParameter("userId", userId);

			try {
				icatAuthorisation = (IcatAuthorisation) nullSearchQuery.getSingleResult();
				log.debug("Found stage 3 (nulls): " + icatAuthorisation);
			} catch (NoResultException nre3) {
				log.debug("None found for : UserId: " + userId + ", elementId: null, type: " + type
						+ ", elementId: null, throwing exception");
				throw new InsufficientPrivilegesException();
			}
		} else {

			// Find users one first as it takes prefs over the ANY row.
			Query query = null;

			if (type == ElementType.INVESTIGATION) {
				query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_INVESTIGATION);
			} else if (type == ElementType.DATASET) {
				query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_DATAFILE_DATASET);
				query.setParameter("parentElementType", ElementType.INVESTIGATION);
				query.setParameter("parentElementId", parentId);
			} else if (type == ElementType.DATAFILE) {
				query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_DATAFILE_DATASET);
				query.setParameter("parentElementType", ElementType.DATASET);
				query.setParameter("parentElementId", parentId);
			}

			query.setParameter("elementType", type).setParameter("elementId", id).setParameter("userId", userId);

			try {
				icatAuthorisation = (IcatAuthorisation) query.getSingleResult();
				log.debug("Found stage 1 (normal): " + icatAuthorisation);
			} catch (NoResultException nre) {

				// try find ANY
				log.trace("None found, searching for ANY in userId");
				query.setParameter("userId", "ANY");

				try {
					icatAuthorisation = (IcatAuthorisation) query.getSingleResult();
					log.debug("Found stage 2 (ANY): " + icatAuthorisation);
				} catch (NoResultException nre2) {
					log.trace("None found, searching for FacilityScientist table in userId");

					String instrument = "";
					// now look in the facility scientist table
					if (object instanceof Datafile) {
						instrument = ((Datafile) object).getDataset().getInvestigation().getInstrument();
					} else if (object instanceof Datafile) {
						instrument = ((Dataset) object).getInvestigation().getInstrument();
					} else if (object instanceof Investigation) {
						instrument = ((Investigation) object).getInstrument();
					}

					FacilityInstrumentScientist facilityInstrumentScientist = null;
					try {
						facilityInstrumentScientist = (FacilityInstrumentScientist) manager
								.createNamedQuery("FacilityInstrumentScientist.findByUserAndInstrument")
								.setParameter("federalId", userId).setParameter("instrumentName", instrument)
								.getSingleResult();
					} catch (NoResultException nre223) {
					}

					if (facilityInstrumentScientist == null) {
						log.debug("None found for : UserId: " + userId + ", type: " + type + ", elementId: " + id
								+ ", throwing exception");
						throw new InsufficientPrivilegesException();
					} else {
						log.debug("Found stage 3 (facility scientist) for instrument " + instrument);
						return null;
					}
				}
			}
		}

		// return the icat authoruisation
		return icatAuthorisation;

	}
}
