/*
 * XMLIngestionManagerBean.java
 *
 * Created on 15-Aug-2007, 10:10:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.icatproject.exposed.manager;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.exposed.EJBObject;

import uk.icat3.jaxb.MetadataIngest;

/**
 * This web service exposes the functions that are needed on investigation
 * 
 * @author gjd37
 */
@Stateless()
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class XMLIngestionManagerBean extends EJBObject implements XMLIngestionManagerLocal {

	static Logger log = Logger.getLogger(XMLIngestionManagerBean.class);

	/**
	 * Method that accepts XML document in the form of a String for ingestion
	 * into ICAT Spawns insert off to asynchronous MessageDrivenBean for
	 * efficiency
	 * 
	 * @param sessionId
	 * @param xml
	 * @throws java.lang.Exception
	 */
	public Long[] ingestMetadata(String sessionId, String xml) throws IcatException {

		// for user bean get userId
		String userId = user.getUserIdFromSessionId(sessionId);

		return MetadataIngest.ingestMetadata(userId, xml, manager);
	}
}
