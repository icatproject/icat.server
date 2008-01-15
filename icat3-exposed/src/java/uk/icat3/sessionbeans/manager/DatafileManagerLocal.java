package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;

/**
 * This is the business interface for DatasetManager enterprise bean.
 */
@Local
public interface DatafileManagerLocal {

    Datafile getDatafile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Datafile createDataFile(String sessionId, Datafile dataFile, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Collection<Datafile> createDataFiles(String sessionId, Collection<Datafile> dataFiles, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void modifyDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void deleteDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    DatafileParameter addDataFileParameter(String sessionId, DatafileParameter dataFileParameter, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Collection<DatafileParameter> addDataFileParameters(String sessionId, Collection<DatafileParameter> dataFileParameters, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void modifyDataFileParameter(String sessionId, DatafileParameter dataFileParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void removeDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    //auth bit
    Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long datafileId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException;

    void deleteAuthorisation(String sessionId, Long authorisationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    void removeAuthorisation(String sessionId, Long authorisationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    void updateAuthorisation(String sessionId, String toChangetoRole, Long authorisationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException;
}
