/*
 * TestALl.java
 *
 * Created on 15-Aug-2007, 13:39:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;
import client.AdvancedSearchDetails;
import client.KeywordType;
import client.*;
import java.util.ArrayList;
import java.util.Collection;
import static icat3wstest.Constants.*;
/**
 *
 * @author gjd37
 */
public class TestAll {
    
    /** Creates a new instance of TestALl */
    public TestAll() throws Exception {
        
        //Login
        //SessionUtil.login("gjd37", System.getProperty("user.password"));
        
        //Investiagtion searches
        InvestigationSearch.searchKeyword(SID,KEYWORD);
        
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
            for (Sample sample : samples) {
                DatasetSearch.searchDatasetsBySample(SID, sample);
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
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALL);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALPHA);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALPHA_NUMERIC);
        
        //Datafile
        DatafileManager.getDatafile(SID, DATAFILE_ID);
        DatafileManager.getDatafiles(SID, DATAFILE_ID);
        
        Datafile df = DatafileManager.createDatafile(SID, "name for sid "+SID);
        DatafileManager.updateDatafile(SID, df, "new name of "+SID);  //this should fail with ICAT_ADMIN user
        DatafileManager.delete_undeleteDatafile(SID, df.getId());
        DatafileManager.delete_undeleteDatafile(SID, df.getId());
        DatafileManager.removeDatafile(SID, df.getId()); //should be false for none ICAT_ADMIN user
        
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
        DatasetManager.updateDataset(SID, ds, "new name of "+SID);  //this should fail with ICAT_ADMIN user
        DatasetManager.delete_undeleteDataset(SID, ds.getId());
        DatasetManager.delete_undeleteDataset(SID, ds.getId());
        DatasetManager.removeDataset(SID, ds.getId()); //should be false for none ICAT_ADMIN user
        
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
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        new TestAll();
    }
    
}
