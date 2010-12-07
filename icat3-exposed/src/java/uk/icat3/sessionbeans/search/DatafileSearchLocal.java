package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.DatafileInclude;

/**
 * This is the business interface for DatafileSearch enterprise bean.
 */
@Local
public interface DatafileSearchLocal extends ParameterSearchInterface {

    Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun) throws SessionException;

    Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results) throws SessionException;

    Collection<DatafileFormat> listDatafileFormats(String sessionId) throws SessionException;
}
