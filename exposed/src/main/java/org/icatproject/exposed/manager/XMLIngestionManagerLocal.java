/*
 * XMLIngestionManagerLocal.java
 *
 * Created on 15-Aug-2007, 10:10:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.icatproject.exposed.manager;

import javax.ejb.Local;

import org.icatproject.core.IcatException;

/**
 * 
 * @author gjd37
 */
@Local
public interface XMLIngestionManagerLocal {

	Long[] ingestMetadata(String sessionId, String xml) throws IcatException;
}
