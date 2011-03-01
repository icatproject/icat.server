package uk.icat3.sessionbeans.manager;

import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;

/**
 * This interface is a stateless session bean interface to managing the parameters
 * @author Mr. Srikanth Nagella
 */
public interface ParameterManagerLocal {

    public void createParameter(String sessionId, Parameter param) throws ValidationException, SessionException;

    public Parameter updateParameter(String sessionId, String name, String units, boolean isSearchable, boolean isDatasetParameter, boolean isDatafileParameter, boolean isSampleParameter) throws ValidationException, SessionException;

    public void removeParameter(String sessionId, String name, String units) throws ValidationException, SessionException;
}
