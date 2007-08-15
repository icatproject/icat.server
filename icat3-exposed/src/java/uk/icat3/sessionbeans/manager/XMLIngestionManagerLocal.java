/*
 * XMLIngestionManagerLocal.java
 *
 * Created on 15-Aug-2007, 10:10:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import javax.ejb.Local;

/**
 *
 * @author gjd37
 */
@Local
public interface XMLIngestionManagerLocal {
    
    //put method here
    public void ingestXML();
}
