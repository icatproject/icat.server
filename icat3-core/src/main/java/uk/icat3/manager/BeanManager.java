package uk.icat3.manager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.Shift;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.security.parser.GetQuery;
import uk.icat3.security.parser.Input;
import uk.icat3.security.parser.LexerException;
import uk.icat3.security.parser.ParserException;
import uk.icat3.security.parser.Token;
import uk.icat3.security.parser.Tokenizer;
import uk.icat3.util.AccessType;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.InvestigationInclude;

public class BeanManager {

	// Global class logger
	private static Logger log = Logger.getLogger(BeanManager.class);

	// Used to protect against someone making an object with the same primary key
	private static final Object lock = new Object();

	public static Object create(String userId, EntityBaseBean bean, EntityManager manager)
			throws InsufficientPrivilegesException, ObjectAlreadyExistsException, ValidationException,
			NoSuchObjectFoundException, IcatInternalException {

		bean.preparePersistTop(userId, manager);
	
		synchronized (lock) {
			bean.isUnique(manager);
			try {
				log.trace(bean + " prepared for persist.");
				manager.persist(bean);
				log.trace(bean + " persisted.");
				manager.flush();
				log.trace(bean + " flushed.");
			} catch (EntityExistsException e) {
				throw new ObjectAlreadyExistsException(e.getMessage());
			} catch (Throwable e) {
				manager.clear();
				bean.preparePersistTop(userId, manager);
				bean.isValid(manager, true);
				e.printStackTrace(System.err);
				throw new IcatInternalException("Unexpected DB response " + e.getMessage());
			}
		}
		// Check authz now everything persisted
		GateKeeper.performAuthorisation(userId, bean, AccessType.CREATE, manager);
		return bean.getPK();

	}

	public static void delete(String userId, EntityBaseBean bean, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException,
			IcatInternalException {
		EntityBaseBean beanManaged = find(bean, manager);
		GateKeeper.performAuthorisation(userId, beanManaged, AccessType.DELETE, manager);
		try {
			manager.remove(beanManaged);
			manager.flush();
			log.trace("Deleted bean " + bean  + " flushed.");
		} catch (Throwable e) {
			manager.clear();
			bean.isValid(manager, false);
			throw new IcatInternalException("Unexpected DB response " + e.getMessage());
		}
	}

	public static void update(String userId, EntityBaseBean bean, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException,
			IcatInternalException {
		EntityBaseBean beanManaged = find(bean, manager);
		GateKeeper.performAuthorisation(userId, beanManaged, AccessType.UPDATE, manager);
		beanManaged.setModId(userId);
		beanManaged.merge(bean, manager);
		
		try {
			manager.flush();
			log.trace("Updated bean " + bean  + " flushed.");
		} catch (Throwable e) {
			manager.clear();
			bean.isValid(manager, false);
			throw new IcatInternalException("Unexpected DB response " + e);
		}
	}

	public static EntityBaseBean get(String userId, String query, Object primaryKey, EntityManager manager)
			throws NoSuchObjectFoundException, InsufficientPrivilegesException, BadParameterException,
			IcatInternalException {

		List<Token> tokens = null;
		try {
			tokens = Tokenizer.getTokens(query);
		} catch (LexerException e) {
			throw new BadParameterException(e.getMessage());
		}
		Input input = new Input(tokens);
		GetQuery q;
		try {
			q = new GetQuery(input);
		} catch (ParserException e) {
			throw new BadParameterException(e.getMessage());
		}

		Class<? extends EntityBaseBean> entityClass = q.getFirstEntity();
		if (primaryKey == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + " has null primary key.");
		}
		EntityBaseBean beanManaged = manager.find(entityClass, primaryKey);
		if (beanManaged == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}

		Set<Class<? extends EntityBaseBean>> includes = q.getIncludes();
		if (includes.size() > 0) {
			processIncludes(beanManaged, includes);
		}

		GateKeeper.performAuthorisation(userId, beanManaged, AccessType.READ, manager);
		log.debug("got " + entityClass.getSimpleName() + "[id:" + primaryKey + "]");
		return beanManaged;
	}

	@SuppressWarnings("unchecked")
	public
	static void processIncludes(EntityBaseBean beanManaged,
			Set<Class<? extends EntityBaseBean>> includes) throws BadParameterException {
		/* This is a nasty temporary hack before doing it properly TODO */

		 Class<? extends EntityBaseBean> entityClass = beanManaged.getClass();
		
		if (entityClass.equals(Dataset.class)) {
			if (includes.containsAll(Arrays.asList(Datafile.class, DatasetParameter.class))) {
				includeDataset((Dataset) beanManaged, DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS);
			} else if (includes.contains(Datafile.class)) {
				includeDataset((Dataset) beanManaged, DatasetInclude.DATASET_AND_DATAFILES_ONLY);
			} else if (includes.contains(DatasetParameter.class)) {
				includeDataset((Dataset) beanManaged, DatasetInclude.DATASET_PARAMETERS_ONLY);
			} else {
				throw new BadParameterException("Unable to deal with specified includes");
			}

		} else if (entityClass.equals(Investigation.class)) {

			if (includes.containsAll(Arrays.asList(Dataset.class, Datafile.class, DatasetParameter.class,
					Investigator.class, Keyword.class, Shift.class, Sample.class, Publication.class))) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.ALL);

			} else if (includes.containsAll(Arrays.asList(Investigator.class, Keyword.class, Shift.class, Sample.class,
					Publication.class))) {
				includeInvestigation((Investigation) beanManaged,
						InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES);

			} else if (includes.containsAll(Arrays.asList(Dataset.class, Datafile.class, DatasetParameter.class))) {
				includeInvestigation((Investigation) beanManaged,
						InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS);

			} else if (includes.containsAll(Arrays.asList(Dataset.class, Datafile.class))) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.DATASETS_AND_DATAFILES);

			} else if (includes.containsAll(Arrays.asList(Dataset.class, DatasetParameter.class))) {
				includeInvestigation((Investigation) beanManaged,
						InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY);

			} else if (includes.contains(Dataset.class)) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.DATASETS_ONLY);

			} else if (includes.containsAll(Arrays.asList(Investigator.class, Shift.class, Sample.class,
					Publication.class))) {
				includeInvestigation((Investigation) beanManaged,
						InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS);

			} else if (includes.containsAll(Arrays.asList(Investigator.class, Shift.class, Sample.class))) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES);

			} else if (includes.containsAll(Arrays.asList(Investigator.class, Shift.class))) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.INVESTIGATORS_AND_SHIFTS);

			} else if (includes.containsAll(Arrays.asList(Investigator.class, Sample.class))) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.INVESTIGATORS_AND_KEYWORDS);

			} else if (includes.contains(Investigator.class)) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.INVESTIGATORS_ONLY);

			} else if (includes.contains(Keyword.class)) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.KEYWORDS_ONLY);

			} else if (includes.contains(Publication.class)) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.PUBLICATIONS_ONLY);

			} else if (includes.contains(Sample.class)) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.SAMPLES_ONLY);

			} else if (includes.contains(Shift.class)) {
				includeInvestigation((Investigation) beanManaged, InvestigationInclude.SHIFT_ONLY);

			} else {
				throw new BadParameterException("Unable to deal with specified includes");
			}
		} else {
			throw new BadParameterException("Unable to deal with specified includes");
		}
	}

	public static EntityBaseBean find(EntityBaseBean bean, EntityManager manager) throws NoSuchObjectFoundException,
			IcatInternalException {
		Object primaryKey = bean.getPK();
		Class<? extends EntityBaseBean> entityClass = bean.getClass();
		if (primaryKey == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + " has null primary key.");
		}
		EntityBaseBean object = null;
		try {
			object = manager.find(entityClass, primaryKey);
		} catch (Throwable e) {
			throw new IcatInternalException("Unexpected DB response " + e);
		}

		if (object == null) {
			throw new NoSuchObjectFoundException(entityClass.getSimpleName() + "[id:" + primaryKey + "] not found.");
		}
		return object;
	}

	private static void includeDataset(Dataset dataset, DatasetInclude include) {
		dataset.setDatasetInclude(include);
		ManagerUtil.log.trace("Setting data sets to include: " + include);

		if (include.isDatafiles()) {
			// size invokes the JPA to get the information, other wise the collections are null
			dataset.getDatafileCollection().size();
		}

		for (Datafile datafile : dataset.getDatafileCollection()) {
			if (include.isDatafilesAndParameters()) {
				datafile.setDatafileInclude(DatafileInclude.ALL);
			} else {
				datafile.setDatafileInclude(DatafileInclude.NONE);
			}
		}
	}

	private static void includeInvestigation(Investigation investigation, InvestigationInclude include) {
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

		if (include.isDatasetsAndDatafiles()) {
			ManagerUtil.log.trace("Including Datasets And Datafiles");
			investigation.getDatasetCollection().size();

		} else if (include.isDatasets()) {
			ManagerUtil.log.trace("Including Datasets only");
			investigation.getDatasetCollection().size();

		}

		if (include == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY) {
			for (Dataset dataset : investigation.getDatasetCollection()) {
				ManagerUtil.log.trace("Setting data sets to include: " + DatasetInclude.DATASET_PARAMETERS_ONLY);
				dataset.setDatasetInclude(DatasetInclude.DATASET_PARAMETERS_ONLY);
			}
		} else if (include.isDatasetsAndDatafiles() && !include.isDatasetsDatafilesAndParameters()) {
			for (Dataset dataset : investigation.getDatasetCollection()) {
				ManagerUtil.log.trace("Setting data sets to include: " + DatasetInclude.DATASET_AND_DATAFILES_ONLY);
				dataset.setDatasetInclude(DatasetInclude.DATASET_AND_DATAFILES_ONLY);
				for (Datafile datafile : dataset.getDatafileCollection()) {
					ManagerUtil.log.trace("Setting data file to include: " + DatafileInclude.NONE);
					datafile.setDatafileInclude(DatafileInclude.NONE);
				}
			}
		} else if (include.isDatasetsDatafilesAndParameters()) {
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