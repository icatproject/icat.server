/*
 * Concurrent.java
 * 
 * Created on 24-Oct-2007, 10:50:10
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.download;

/**
 *
 * @author gjd37
 */
public class Concurrent {

    private int concurrent = 0;
    private int max = 0;

    public synchronized  void add() {
         concurrent++;
         if(concurrent > max ) max = concurrent;
    }

    public synchronized void minus() {
        this.concurrent--;
    }
    
    public int getMax(){
        return max;
    }
    
     public int get(){
        return concurrent;
    }
    
    public Concurrent() {
    }

}
