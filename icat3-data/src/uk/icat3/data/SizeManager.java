/*
 * SizeManager.java
 *
 * Created on 18-Oct-2007, 09:11:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.data;

import org.apache.log4j.Logger;
import uk.icat3.data.exceptions.TotalSizeExceededException;

/** 12154880
 *
 * @author gjd37
 */
public class SizeManager {
    
    static Logger log = Logger.getLogger(SizeManager.class);
    /**
     * Max size of any download
     */
    private static final long MAXIMUM_SINGLE_SIZE = 1024*1024*30; // == 30M
    /**
     * Current running size;
     */
    private static long CURRENT_SIZE = 0;
    private static long CONCURRENT = 0;
    private static long MAX_CONCURRENT = 0;
    private static long MAX_CONCURRENT_SIZE = 0;
    
    /**
     * Max size of all downloads
     */
    private static final long MAXIMUM_SIZE = 1024*1024*100; //= 100M
    
    public synchronized long getMaximumSingleSize() {
        return MAXIMUM_SINGLE_SIZE;
    }
    
    public long getConcurrent() {
        return CONCURRENT;
    }
    
    public synchronized long getCurrentSize() {
        return CURRENT_SIZE;
    }
    
    public synchronized long getMaximumSize() {
        return MAXIMUM_SIZE;
    }
    
    public synchronized void setCurrentSize(long current) {
        this.CURRENT_SIZE += current;
    }
    
    public synchronized void minus(long minusBytes) {
        CURRENT_SIZE -= minusBytes;
        CONCURRENT--;
        log.info("Current download size is going down to: "+CURRENT_SIZE+" bytes, MAX SIZE "+MAXIMUM_SIZE+", number Concurent "+CONCURRENT);
    }
    
    public synchronized boolean add(long addedBytes) throws TotalSizeExceededException{
        if(addedBytes > MAXIMUM_SINGLE_SIZE) throw new TotalSizeExceededException("Download size of "+addedBytes+ " is greater than maximum allowed of "+MAXIMUM_SINGLE_SIZE+" bytes");
        else if(CURRENT_SIZE + addedBytes < MAXIMUM_SIZE) {
            CURRENT_SIZE += addedBytes;
            CONCURRENT++;
            if(CONCURRENT > MAX_CONCURRENT) MAX_CONCURRENT = CONCURRENT;
            if(CURRENT_SIZE > MAX_CONCURRENT_SIZE) MAX_CONCURRENT_SIZE = CURRENT_SIZE;
            log.info("Current download size is going up to: "+CURRENT_SIZE/(1024f*1024f)+" Mb, MAX SIZE "+MAXIMUM_SIZE/(1024f*1024f)+" Mb, MAX CON (RUNNING) SIZE "+MAX_CONCURRENT_SIZE/(1024f*1024f)+" Mb");
            log.info("Concurent downloads "+CONCURRENT+", MAX CONCURRENT "+MAX_CONCURRENT);            
            return true;
        } else return false;
    }
    
    public SizeManager() {
    }
    
}
