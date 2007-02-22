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
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
public class DatafileSearch {
    
    // Global class logger
    static Logger log = Logger.getLogger(DatafileSearch.class);
    
    /**
     *
     *
     * @param userId
     * @param instruments
     * @param startRun
     * @param endRun
     * @param startIndex
     * @param number_results
     * @param manager
     * @return
     */
    public static Collection<Datafile> searchByRunNumberImpl(String userId, Collection<String> instruments, Long startRun, Long endRun, int startIndex, int number_results, EntityManager manager){
        if(instruments == null) throw new IllegalArgumentException("Instrument collection cannot be null");
        log.trace("searchByRunNumber("+userId+", "+instruments.toArray()+", "+startRun+", "+endRun+", EntityManager)");
        
        if(number_results < 0){
            return  manager.createNamedQuery(Queries.DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER).setParameter("userId",userId).setParameter("instrument",instruments.iterator().next()).setParameter("lower",startRun).setParameter("upper",endRun).getResultList();
        } else {
            // return  manager.createNamedQuery(Queries.DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER).setParameter("userId",userId).setParameter("instrument",instruments.iterator().next()).setParameter("lower",startRun).setParameter("upper",endRun).setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            return  manager.createNamedQuery(Queries.DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER).setParameter("lower",startRun).setParameter("upper",endRun).setMaxResults(number_results).setFirstResult(startIndex).getResultList();
            
        }
        
    }
    
    /**
     *
     * @param userId
     * @param instruments
     * @param startRun
     * @param endRun
     * @param manager
     * @return
     */
    public static Collection<Datafile> searchByRunNumber(String userId, Collection<String> instruments, Long startRun, Long endRun, EntityManager manager){
        return searchByRunNumberImpl(userId, instruments, startRun, endRun, -1,-1, manager);
    }
    
    /**
     *
     * @param userId
     * @param instruments
     * @param startRun
     * @param endRun
     * @param startIndex
     * @param number_results
     * @param manager
     * @return
     */
    public static Collection<Datafile> searchByRunNumber(String userId, Collection<String> instruments, Long startRun, Long endRun, int startIndex, int number_results, EntityManager manager){
        return searchByRunNumberImpl(userId, instruments, startRun, endRun, startIndex, number_results, manager);
        
    }
}
