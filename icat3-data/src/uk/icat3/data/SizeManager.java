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

/**
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
    /**
     * Max size of all downloads
     */
    private static final long MAXIMUM_SIZE = 1024*1024*100; //= 100M

    public synchronized long getMaximumSingleSize() {
        return MAXIMUM_SINGLE_SIZE;
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
            log.trace("Current download size is "+CURRENT_SIZE);       
    }
    
    public synchronized boolean add(long addedBytes) throws TotalSizeExceededException{
        if(addedBytes > MAXIMUM_SINGLE_SIZE) throw new TotalSizeExceededException("Download size of "+addedBytes+ " is greater than maximum allowed of "+MAXIMUM_SINGLE_SIZE+" bytes");
        else if(CURRENT_SIZE + addedBytes < MAXIMUM_SIZE) {
            CURRENT_SIZE += addedBytes;
            log.trace("Current download size is "+CURRENT_SIZE);
            return true;
        }
        else return false;
    }

    public SizeManager() {
    }

}
