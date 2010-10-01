package uk.icat3.sessionbeans.search;

import java.util.Collection;
import java.util.List;
import javax.ejb.Local;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;

/**
 * This is the business interface for DatafileSearch enterprise bean.
 */
@Local
public interface DatafileSearchLocal {

    Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun) throws SessionException;

    Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results) throws SessionException;

    Collection<DatafileFormat> listDatafileFormats(String sessionId) throws SessionException;

    Collection<Datafile> searchDatafilesByParameterOperable (String sessionId, ParameterCondition parameterOperable) throws SessionException, ParameterSearchException;

    Collection<Datafile> searchDatafilesByParameter(String sessionId, List<ParameterComparisonCondition> parameterComparator) throws SessionException, ParameterSearchException;

}
