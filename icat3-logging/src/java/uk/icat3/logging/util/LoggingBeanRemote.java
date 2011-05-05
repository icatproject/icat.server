/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.util;

import java.util.List;
import javax.ejb.Remote;
import uk.icat3.logging.entity.DatasetInclude;
import uk.icat3.logging.entity.LogInstrument;
import uk.icat3.logging.entity.InvInclude;
import uk.icat3.logging.entity.LogInvestigator;
import uk.icat3.logging.entity.LogKeyword;
import uk.icat3.logging.entity.LoggedDownload;

/**
 *
 * @author scb24683
 */
@Remote
public interface LoggingBeanRemote {

    public String getUserIdFromSessionId(String sessionId);

    public InvInclude getInvIncludeByName(String name);

    public LogKeyword findKeyword(String keyword);

    public LogInstrument getInstrumentByName(String name);

    public LogInvestigator searchForInvestigator(String name);

    public DatasetInclude getDatasetIncByName(String name);

    public List<LoggedDownload> getDataDownloadsByInvestigation(String sessionId, Long investigationId);

    public List<LoggedDownload> getDataDownloadsByDatafile(String sessionId, Long datafileId);

    public List<LoggedDownload> getWhoDownloadedMyData(String sessionId);
}
