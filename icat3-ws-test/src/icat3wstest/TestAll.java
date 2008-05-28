/*
 * TestALl.java
 *
 * Created on 15-Aug-2007, 13:39:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import uk.icat3.client.*;
import java.util.Collection;
import java.util.Random;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class TestAll {
    
    /** Creates a new instance of TestALl */
    public TestAll() throws Exception {
        
        long time  = System.currentTimeMillis();
         
        //Login
        String sid = SessionUtil.login(System.getProperty("user.name"), System.getProperty("usersso.password"));
        
        //Investiagtion searches
        InvestigationSearch.searchKeyword(SID,KEYWORD);
        
        //InvestigationSearch.searchKeywordAll(SID,KEYWORD.substring(0, 4));
        
        InvestigationSearch.searchUserId(SID,USER_ID);
        
        InvestigationSearch.searchSurname(SID,SURNAME);
        
        InvestigationSearch.searchMyInvestigations(SID);
        
        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        asd.getKeywords().add(KEYWORD);
        asd.getInvestigators().add(SURNAME);
        
        InvestigationSearch.searchAdvanced(SID, asd);
        
        //Dataset searches
        Collection<Sample> samples = DatasetSearch.searchBySampleName(SID, KEYWORD);
        if(samples != null){
            int i = 0;
            for (Sample sample : samples) {
                DatasetSearch.searchDatasetsBySample(SID, sample);
                i++;
                if(i == 3) break;
            }
        }
        
        //Datafile searches
        DatafileSearch.searchByRunNumber(SID, INSTRUMENT, 0f, 1300f);
        
        //List searches
        ListSearch.listDatasetStatus(SID);
        ListSearch.listParameters(SID);
        ListSearch.listInvestigationTypes(SID);
        ListSearch.listInstruments(SID);
        ListSearch.listDatasetType(SID);
        ListSearch.listRoles(SID);
        ListSearch.listDatafileFormats(SID);
        
        //keyword searches
        KeywordSearch.searchKeyword(SID);
        KeywordSearch.searchKeywordType(SID, KeywordType.ALPHA);
        KeywordSearch.searchKeywordType(SID, KeywordType.ALPHA_NUMERIC);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALL);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALPHA);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALPHA_NUMERIC);
        
        //Datafile
        DatafileManager.getDatafile(SID, DATAFILE_ID);
        DatafileManager.getDatafiles(SID, DATAFILE_ID);
        
        Datafile df = DatafileManager.createDatafile(SID, "name for sid "+SID);
        if(df != null) {
            DatafileManager.updateDatafile(SID, df, "new name of "+SID);  //this should fail with ICAT_ADMIN user
            DatafileManager.delete_undeleteDatafile(SID, df.getId());
            DatafileManager.delete_undeleteDatafile(SID, df.getId());
            DatafileManager.removeDatafile(SID, df.getId()); //should be false for none ICAT_ADMIN user
        }
  
        DatafileParameter dfp = DatafileManager.addParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID);
        if(dfp !=null) {
            DatafileManager.updateDatafileParameter(SID, dfp, "new description for dfp");
            DatafileManager.delete_undeleteParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID); //delete it
            DatafileManager.delete_undeleteParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID); //un delete it
            DatafileManager.removeDatafileParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID);  //should be false for none ICAT_ADMIN user
        }
        
        //Dataset
        DatasetManager.getDataset(SID, DATASET_ID);
        DatasetManager.getDatasets(SID, DATASET_ID);
        
        Dataset ds = DatasetManager.createDataset(SID, "name for sid "+SID);
        if(ds != null){
            DatasetManager.updateDataset(SID, ds, "new name of "+SID);  //this should fail with ICAT_ADMIN user
            DatasetManager.delete_undeleteDataset(SID, ds.getId());
            DatasetManager.delete_undeleteDataset(SID, ds.getId());
            DatasetManager.removeDataset(SID, ds.getId()); //should be false for none ICAT_ADMIN user
        }
        
        DatasetParameter dsp = DatasetManager.addParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATASET_ID);
        if(dsp != null) {
            DatasetManager.updateDatasetParameter(SID, dsp, "new description for dfp");
            DatasetManager.delete_undeleteParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATASET_ID); //delete it
            DatasetManager.delete_undeleteParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATASET_ID); //un delete it
            DatasetManager.removeDatasetParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATASET_ID);  //should be false for none ICAT_ADMIN user
        }
        
        //Investigation
        InvestigationManager.getInvestigation(SID, INVESTIGATION_ID);
        InvestigationManager.getInvestigations(SID, INVESTIGATION_ID);
        
        
        Investigation investigation = InvestigationManager.createInvestigation(SID, "investigation for "+SID);
        if(investigation != null) {
            InvestigationManager.updateInvestigation(SID, investigation, "new investigation for "+SID);
            InvestigationManager.delete_undeleteInvestigation(SID, investigation.getId()); //deletes investigation
            InvestigationManager.delete_undeleteInvestigation(SID, investigation.getId()); //undeletes investigation
            InvestigationManager.removeInvestigation(SID, investigation.getId());
        }
        
        //Investigation keyword
        Keyword keyword = InvestigationKeywordManager.addKeyword(SID, "new keyword "+new Random().nextInt(), INVESTIGATION_ID);
        if(keyword !=null){
            InvestigationKeywordManager.delete_undeleteKeyword(SID, keyword.getKeywordPK()); //deleted keyword
            InvestigationKeywordManager.delete_undeleteKeyword(SID, keyword.getKeywordPK()); //undeleted keyword
            InvestigationKeywordManager.removeKeyword(SID, keyword.getKeywordPK());
        }
        
        //Investigation publication
        Publication publication = InvestigationPublicationManager.addPublication(SID, "new Publication "+new Random().nextInt(), INVESTIGATION_ID);
        if(publication !=null){
            InvestigationPublicationManager.updatePublication(SID, publication, "http://newUrl.com");
            InvestigationPublicationManager.delete_undeletePublication(SID, publication.getId()); //deleted Publication
            InvestigationPublicationManager.delete_undeletePublication(SID, publication.getId()); //undeleted Publication
            InvestigationPublicationManager.removePublication(SID, publication.getId());
        }
        
        //Investigation investigator
        Investigator investigator = InvestigationInvestigatorManager.addInvestigator(SID, INVESTIGATOR, INVESTIGATION_ID);
        if(investigator !=null){
            InvestigationInvestigatorManager.updateInvestigator(SID, investigator, "new role");
            InvestigationInvestigatorManager.delete_undeleteInvestigator(SID, investigator.getInvestigatorPK()); //deleted Investigator
            InvestigationInvestigatorManager.delete_undeleteInvestigator(SID, investigator.getInvestigatorPK()); //undeleted Investigator
            InvestigationInvestigatorManager.removeInvestigator(SID, investigator.getInvestigatorPK());
        }
        
        //Investigation sample
        Sample sample = InvestigationSampleManager.addSample(SID, PARAMETER_NAME, PARAMETER_UNITS+new Random().nextInt(), INVESTIGATION_ID);
        if(sample !=null){
            InvestigationSampleManager.updateSample(SID, sample, "new safety");
            InvestigationSampleManager.delete_undeleteSample(SID, sample.getId()); //deleted Sample
            InvestigationSampleManager.delete_undeleteSample(SID, sample.getId()); //undeleted Sample
            InvestigationSampleManager.removeSample(SID, sample.getId());
        }
        
        //Investigation sample parameter
        SampleParameter sampleParameter = InvestigationSampleParameterManager.addSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID);
        if(sampleParameter !=null){
            InvestigationSampleParameterManager.updateSampleParameter(SID, sampleParameter, "new description");
            InvestigationSampleParameterManager.delete_undeleteSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID); //deleted SampleParameter
            InvestigationSampleParameterManager.delete_undeleteSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID); //undeleted SampleParameter
            InvestigationSampleParameterManager.removeSampleParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, SAMPLE_ID);
        }
        
        //Manager users
        ManagerUsers.listInvestigationAuthorisations(SID, INVESTIGATION_ID);
        ManagerUsers.listDatafileAuthorisations(SID, DATAFILE_ID);
        
        IcatAuthorisation icatAuthorisation = ManagerUsers.addDatasetAuthorisations(SID, DATASET_ID, "added"+new Random().nextInt(), "DOWNLOADER");
        if(icatAuthorisation != null){
            ManagerUsers.updateDatasetAuthorisations(SID, icatAuthorisation, "READER");
            ManagerUsers.deleteDatasetAuthorisations(SID, icatAuthorisation); //delete
            ManagerUsers.deleteDatasetAuthorisations(SID, icatAuthorisation); //undelete
            ManagerUsers.listDatasetAuthorisations(SID, DATASET_ID);
            ManagerUsers.removeDatasetAuthorisations(SID, icatAuthorisation);
        }
        
        SessionUtil.logout(sid);
        
        float totalTime = (System.currentTimeMillis() - time) / 1000f;
        System.out.println("Time taken to do all tests: " + totalTime + " seconds");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        new TestAll();
    }
    
}
