package uk.icat3.sessionbeans.data;

import java.util.Collection;
import javax.ejb.Local;

import org.icatproject.core.IcatException;

import uk.icat3.data.DownloadInfo;

@Local
public interface DownloadManagerLocal {

	String downloadDatafile(String sessionId, Long datafileId) throws IcatException;

	String downloadDataset(String sessionId, Long datasetId) throws IcatException;

	String downloadDatafiles(String sessionId, Collection<Long> datafileIds) throws IcatException;

	DownloadInfo checkDatasetDownloadAccess(String sessionId, Long datasetId) throws IcatException;

	DownloadInfo checkDatafileDownloadAccess(String sessionId, Collection<Long> datafileIds) throws IcatException;
}
