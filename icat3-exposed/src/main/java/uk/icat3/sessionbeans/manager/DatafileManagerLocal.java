package uk.icat3.sessionbeans.manager;

import java.util.Collection;

import javax.ejb.Local;

import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;

/**
 * This is the business interface for DatasetManager enterprise bean.
 */
@Local
public interface DatafileManagerLocal {

    Datafile getDatafile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

}
