/*
 * JAXBTranslate.java
 * 
 * Created on 25-May-2007, 13:38:06
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.ingest;

/**
 *
 * @author df01
 */
public class JAXBTranslate {

    
    
    public static uk.icat3.entity.Investigation translate(uk.icat3.jaxb.gen.Investigation investigation) {
        uk.icat3.entity.Investigation inv = new uk.icat3.entity.Investigation();
        inv.setBcatInvStr(investigation.getBcatInvStr());
        //inv.setFacilityCycle(facilityCycle);
        //inv.setInstrument(investigation.getInstrument());
        inv.setInvAbstract(investigation.getInvAbstract());
        inv.setInvNumber(investigation.getInvNumber());
        inv.setPrevInvNumber(investigation.getPrevInvNumber());
        //inv.setInvType(investigation.getInvType());
        //inv.setInvestigationInclude(investigation.get); --what is this?
       // inv.setPrevInvNumber(prevInvNumber);
        return inv;
    }

}
