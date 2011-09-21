package uk.icat3.sessionbeans.manager;

import java.util.Collection;

import javax.ejb.Local;

import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.DatasetInclude;

/**
 * This is the business interface for DatasetManager enterprise bean.
 */
@Local
public interface DatasetManagerLocal {

	Dataset getDataset(String sessionId, Long datasetId) throws SessionException, InsufficientPrivilegesException,
			NoSuchObjectFoundException;

	Collection<Dataset> getDatasets(String sessionId, Collection<Long> datasetIds) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException;

	Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes) throws SessionException,
			InsufficientPrivilegesException, NoSuchObjectFoundException;

}
