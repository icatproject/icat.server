/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import uk.icat3.client.*;
import java.util.ArrayList;
import java.util.List;
import static icat3wstest.Constants.*;

/**
 *
 * @author gjd37
 */
public class DatafileManager {
    
    /** Creates a new instance of SearchKeyword */
    public static void getDatafile(String sid, Long id) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            Datafile datafile = ICATSingleton.getInstance().getDatafile(sid, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("  ID: "+ datafile.getId()+", TITLE: "+ datafile.getName() +", ds id "+datafile.getDatasetId());
            System.out.println("     Datafile Parameters: "+ datafile.getDatafileParameterCollection().size());
            System.out.println("     ----------------------");
            for (DatafileParameter datafileParameter :  datafile.getDatafileParameterCollection()) {
                System.out.println("       "+datafileParameter.getDatafileParameterPK().getName());
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to getDatafile with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void getDatafiles(String sid, Long id) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            List<Long> ids = new ArrayList<Long>();
            ids.add(id);
            //ids.add(id);
            
            // TODO process result here
            List<Datafile> datafiles = ICATSingleton.getInstance().getDatafiles(sid, ids);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Number of getDatafiles is "+datafiles.size());
            System.out.println("Results:");
            for (Datafile datafile : datafiles) {
                System.out.println("  ID: "+datafile.getId()+", TITLE: "+datafile.getName());
                
            }
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to getDatafiles with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static Datafile createDatafile(String sid, String name) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            Datafile df = new Datafile();
            
            /*DatafileFormat dff = new DatafileFormat();
            DatafileFormatPK PK = new DatafileFormatPK();
            PK.setName(DATAFILE_FORMAT_NAME);
            PK.setVersion(DATAFILE_FORMAT_VERSION);
            dff.setDatafileFormatPK(PK);
            df.setDatafileFormat(dff);*/
            
            //   Should be done with something like:
            List<DatafileFormat> formats = ICATSingleton.getInstance().listDatafileFormats(SID);
            df.setDatafileFormat(formats.iterator().next());
            
            df.setName(name);                                    
            
            Datafile result = ICATSingleton.getInstance().createDataFile(sid, df, DATASET_ID);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: createDatafile: ID: "+result.getId());
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return result;
        } catch (Exception ex) {
            System.out.println("Unable to createDatafile with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
            
            // TODO handle custom exceptions here
        }
    }
    
    public static void delete_undeleteDatafile(String sid, Long id){
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deleteDataFile(sid, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: delete_undeleteDatafile");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to delete_undeleteDatafile with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    public static void removeDatafile(String sid, Long id){
        try {
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removeDataFile(sid, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeDatafile");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeDatafile with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    public static void updateDatafile(String sid, Datafile df, String newName) throws Exception {
        
        try {
            long time = System.currentTimeMillis();
            
            df.setName(newName);
            
            ICATSingleton.getInstance().modifyDataFile(sid, df);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Results: updateDatafile");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updateDatafile with SID "+sid);
            System.out.println(ex);
            assert false;
            
            // TODO handle custom exceptions here
        }
    }
    
    
    
    public static DatafileParameter addParameter(String sid, String name, String units, Long id){
        try {
            DatafileParameterPK PK = new DatafileParameterPK();
            PK.setDatafileId(id);
            PK.setName(name);
            PK.setUnits(units);
            DatafileParameter dfp = new DatafileParameter();
            dfp.setDatafileParameterPK(PK);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            DatafileParameter dfpRetured = ICATSingleton.getInstance().addDataFileParameter(sid, dfp, id);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: addDataFileParameter: "+dfpRetured);
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
            return dfpRetured;
        } catch (Exception ex) {
            System.out.println("Unable to addDataFileParameter with SID "+sid);
            System.out.println(ex);
            assert false;
            return null;
            // TODO handle custom exceptions here
        }
    }
    
    public static void delete_undeleteParameter(String sid, String name, String units, Long id){
        try {
            DatafileParameterPK PK = new DatafileParameterPK();
            PK.setDatafileId(id);
            PK.setName(name);
            PK.setUnits(units);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().deleteDataFileParameter(sid, PK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: deleteDataFileParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to deleteDataFileParameter with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    public static void removeDatafileParameter(String sid, String name, String units, Long id){
        try {
            DatafileParameterPK PK = new DatafileParameterPK();
            PK.setDatafileId(id);
            PK.setName(name);
            PK.setUnits(units);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().removeDataFileParameter(sid, PK);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: removeDatafileParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to removeDatafileParameter with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    public static void updateDatafileParameter(String sid, DatafileParameter dfp, String newDesc){
        try {
            dfp.setDescription(newDesc);
            
            long time = System.currentTimeMillis();
            
            // TODO process result here
            ICATSingleton.getInstance().modifyDataFileParameter(sid, dfp);
            
            float totalTime = (System.currentTimeMillis() - time)/1000f;
            
            System.out.println("Result: updateDatafileParameter");
            
            System.out.println("\nTime taken: "+totalTime+" seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to updateDatafileParameter with SID "+sid);
            System.out.println(ex);
            assert false;
        }
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        getDatafile(SID, INVESTIGATION_ID);
        //getDatafiles(SID, INVESTIGATION_ID);
        
       // Datafile df = createDatafile(SID, "nexus.w"+SID);
      //   delete_undeleteDatafile(SID, df.getId());
      /*  updateDatafile(SID, df, "new name of "+SID);  //this should fail with ICAT_ADMIN user
        delete_undeleteDatafile(SID, df.getId());
        delete_undeleteDatafile(SID, df.getId());
        removeDatafile(SID, df.getId()); //should be false for none ICAT_ADMIN user
       
        DatafileParameter dfp = addParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID);
        if(dfp !=null) {
       updateDatafileParameter(SID, dfp, "new description for dfp");
        delete_undeleteParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID); //delete it
        delete_undeleteParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID); //un delete it
        removeDatafileParameter(SID, PARAMETER_NAME, PARAMETER_UNITS, DATAFILE_ID);  //should be false for none ICAT_ADMIN user
        }
       */
    }
    
}