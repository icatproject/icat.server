/*
 * DatafileSearch.java
 *
 * Created on 22 February 2007, 08:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.util.Queries;
import static uk.icat3.util.Queries.*;
/**
 *
 * @author gjd37
 */
public class DatafileSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(DatafileSearch.class);
    
    /**
     *
     * @param userId userId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database
     * @return
     */
    private static Collection<Datafile> searchByRunNumberImpl(String userId, Collection<String> instruments, Long startRun, Long endRun, int startIndex, int number_results, EntityManager manager){
        if(instruments == null) throw new IllegalArgumentException("Instrument collection cannot be null");
        log.trace("searchByRunNumber("+userId+", "+instruments.toArray()+", "+startRun+", "+endRun+", EntityManager)");
        
        Collection<Datafile> datafiles = null;
        
        //dynamically create the SQL
        String SQL = DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_SQL_PART_1 +" (";
        
        int i = 0;
        for(String instrument : instruments){
            if(i == 0) SQL = SQL + " t5.NAME LIKE ?instrument"+(i++);
            else  SQL = SQL +" OR t5.NAME LIKE ?instrument"+(i++);
            
        }
        
        SQL = ") "+ DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_SQL_PART_2;
        
        //set query with investigation as entity object
        Query query = manager.createNativeQuery(SQL, Datafile.class);
        
        query = query.setParameter("userId",userId);
        
        //set keywords
        int j = 0;
        for(String instrument : instruments){
            query = query.setParameter("instrument"+j++,"%"+instrument+"%");
        }
        
        if(number_results < 0){
            //get all, maybe should limit this to 500?
            datafiles =  query.setMaxResults(MAX_QUERY_RESULTSET).getResultList();
        } else {
            datafiles = query.setMaxResults(number_results).setFirstResult(startIndex).getResultList();
        }
        
        return datafiles;
    }
    
    /**
     * Searches by run number range and list of instruments.
     *
     * @param userId userId of the user.
     * @param instruments
     * @return
     */
    public static Collection<Datafile> searchByRunNumber(String userId, Collection<String> instruments, Long startRun, Long endRun, EntityManager manager){
        return searchByRunNumberImpl(userId, instruments, startRun, endRun, -1,-1, manager);
    }
    
    /**
     * Searches by run number range and list of instruments.
     *
     * @param userId userId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @param manager manager object that will facilitate interaction with underlying database     *
     * @return
     */
    public static Collection<Datafile> searchByRunNumber(String userId, Collection<String> instruments, Long startRun, Long endRun, int startIndex, int number_results, EntityManager manager){
        return searchByRunNumberImpl(userId, instruments, startRun, endRun, startIndex, number_results, manager);
    }
}
