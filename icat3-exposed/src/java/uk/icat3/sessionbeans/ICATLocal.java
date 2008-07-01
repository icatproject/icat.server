/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans;

import java.util.Collection;
import uk.icat3.data.DownloadInfo;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.KeywordDetails;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;

/**
 *
 * @author gjd37
 */
public interface ICATLocal {

    IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long elementId, ElementType elementType)
            throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException;

    DatafileParameter addDataFileParameter(String sessionId, DatafileParameter dataFileParameter, Long datafileId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Collection<DatafileParameter> addDataFileParameters(String sessionId, Collection<DatafileParameter> dataFileParameters, Long datafileId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    DatasetParameter addDataSetParameter(String sessionId, DatasetParameter dataSetParameter, Long datasetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Collection<DatasetParameter> addDataSetParameters(String sessionId, Collection<DatasetParameter> dataSetParameters, Long datasetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Investigator addInvestigator(String sessionId, Investigator investigator, Long investigationId)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Keyword addKeyword(String sessionId, Keyword keyword, Long investigationId)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Publication addPublication(String sessionId, Publication publication, Long investigationId)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Sample addSample(String sessionId, Sample sample, Long investigationId)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    SampleParameter addSampleParameter(String sessionId, SampleParameter sampleParameter, Long investigationId)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    DownloadInfo checkDatafileDownloadAccess(String sessionId, Collection<Long> datafileIds)
            throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    DownloadInfo checkDatasetDownloadAccess(String sessionId, Long datasetId)
            throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    Datafile createDataFile(String sessionId, Datafile dataFile, Long datasetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Collection<Datafile> createDataFiles(String sessionId, Collection<Datafile> dataFiles, Long datasetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Dataset createDataSet(String sessionId, Dataset dataSet, Long investigationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Collection<Dataset> createDataSets(String sessionId, Collection<Dataset> dataSets, Long investigationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    Investigation createInvestigation(String sessionId, Investigation investigation)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void deleteAuthorisation(String sessionId, Long authorisationId)
            throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException;

    void deleteDataFile(String sessionId, Long datafileId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteDataSet(String sessionId, Long dataSetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteInvestigation(String sessionId, Long investigationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteKeyword(String sessionId, KeywordPK keywordPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deletePublication(String sessionId, Long publicationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteSample(String sessionId, Long sampleId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void deleteSampleParameter(String sessionId, SampleParameterPK sampleParameterPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    String downloadDatafile(String sessionId, Long datafileId)
            throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    String downloadDatafiles(String sessionId, Collection<Long> datafileIds)
            throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    String downloadDataset(String sessionId, Long datasetId)
            throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    Collection<String> getAllKeywords(String sessionId, KeywordType type)
            throws SessionException;

    Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long elementId, ElementType elementType)
            throws InsufficientPrivilegesException, NoSuchObjectFoundException, SessionException;

    Datafile getDatafile(String sessionId, Long datafileId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Datafile> getDatafiles(String sessionId, Collection<Long> datafileIds)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Dataset getDataset(String sessionId, Long datasetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Dataset> getDatasets(String sessionId, Collection<Long> datasetIds)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Investigation getInvestigation(String sessionId, Long investigationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, InvestigationInclude includes)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<String> getKeywordsForUser(String sessionId)
            throws SessionException;

    Collection<String> getKeywordsForUser(String sessionId, String startKeyword, int numberReturned)
            throws SessionException;

    Collection<String> getKeywordsForUser(String sessionId, int numberReturned)
            throws SessionException;

    Collection<String> getKeywordsForUser(String sessionId, KeywordType type)
            throws SessionException;

    Collection<Investigation> getMyInvestigations(String sessionId)
            throws SessionException;

    Collection<Investigation> getMyInvestigations(String sessionId, InvestigationInclude include)
            throws SessionException;

    Collection<Investigation> getMyInvestigations(String sessionId, InvestigationInclude include, int startIndex, int number_results)
            throws SessionException;

    Long[] ingestMetadata(String sessionId, String xml)
            throws SessionException, ValidationException, InsufficientPrivilegesException, ICATAPIException;

    Collection<DatafileFormat> listDatafileFormats(String sessionId)
            throws SessionException;

    Collection<String> listDatasetStatus(String sessionId)
            throws SessionException;

    Collection<String> listDatasetTypes(String sessionId)
            throws SessionException;

    Collection<String> listInstruments(String sessionId)
            throws SessionException;

    Collection<String> listInvestigationTypes(String sessionId)
            throws SessionException;

    Collection<Parameter> listParameters(String sessionId)
            throws SessionException;

    Collection<IcatRole> listRoles(String sessionId)
            throws SessionException;

    String login(String username, String password)
            throws SessionException;

    String login(String username, String password, int lifetime)
            throws SessionException;

    boolean logout(String sessionId);

    void modifyDataFile(String sessionId, Datafile dataFile)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void modifyDataFileParameter(String sessionId, DatafileParameter dataFileParameter)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void modifyDataSet(String sessionId, Dataset dataSet)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void modifyDataSetParameter(String sessionId, DatasetParameter dataSetParameter)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void modifyInvestigation(String sessionId, Investigation investigaion)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void modifyInvestigator(String sessionId, Investigator investigator)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void modifyPublication(String sessionId, Publication publication)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void modifySample(String sessionId, Sample sample)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void modifySampleParameter(String sessionId, SampleParameter sampleParameter)
            throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeAuthorisation(String sessionId, Long authorisationId)
            throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException;

    void removeDataFile(String sessionId, Long datafileId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeDataSet(String sessionId, Long dataSetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeInvestigation(String sessionId, Long investigationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeInvestigator(String sessionId, InvestigatorPK investigatorPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeKeyword(String sessionId, KeywordPK keywordPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removePublication(String sessionId, Long publicationId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeSample(String sessionId, Long sampleId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    void removeSampleParameter(String sessionId, SampleParameterPK sampleParameterPK)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Investigation> searchByAdvanced(String sessionId, AdvancedSearchDetails advancedSearch)
            throws SessionException;

    Collection<Investigation> searchByAdvanced(String sessionId, AdvancedSearchDetails advancedSearch, int startIndex, int numberOfResults)
            throws SessionException;

    Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords)
            throws SessionException;

    Collection<Investigation> searchByKeywords(String sessionId, KeywordDetails details, int startIndex, int numberOfResults)
            throws SessionException;

    Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun)
            throws SessionException;

    Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results)
            throws SessionException;

    Collection<Investigation> searchByUserID(String sessionId, String userSearch)
            throws SessionException;

    Collection<Investigation> searchByUserID(String sessionId, String userSearch, int startIndex, int number_results)
            throws SessionException;

    Collection<Investigation> searchByUserSurname(String sessionId, String surname)
            throws SessionException;

    Collection<Investigation> searchByUserSurname(String sessionId, String surname, int startIndex, int number_results)
            throws SessionException;

    Collection<Dataset> searchDatasetsBySample(String sessionId, Sample sample)
            throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName)
            throws SessionException;

    void setDataSetSample(String sessionId, Long sampleId, Long datasetId)
            throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;

    void updateAuthorisation(String sessionId, String toChangetoRole, Long authorisationId)
            throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException;
}
