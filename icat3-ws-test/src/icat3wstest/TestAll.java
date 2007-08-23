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
import client.Sample;
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
        //SessionUtil.login("gjd37", "kkkkkk");
        
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
        
        //keyword searches
        KeywordSearch.searchKeyword(SID);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALL);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALPHA);
        KeywordSearch.searchAllKeywords(SID, KeywordType.ALPHA_NUMERIC);
        
        //Datafile
        DatafileManager.getDatafile(SID, DATAFILE_ID);       
        DatafileManager.getDatafiles(SID, DATAFILE_ID);
        
        //Dataset
        DatasetManager.getDataset(SID, DATASET_ID);     
        DatasetManager.getDatasets(SID, DATASET_ID);
        
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
