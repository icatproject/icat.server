/*
 * ICATSingleton.java
 *
 * Created on 25 June 2007, 10:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

import uk.icat3.client.*;
import javax.xml.ws.BindingProvider;
import static icat3wstest.Constants.*;

/**
 *
 * @author gjd37
 */
public class ICATSingleton {
    
    private static ICAT icatPort = new ICATService().getICATPort();
    
    /** Creates a new instance of ICATSingleton */
    private ICATSingleton() {
    }
    
    public static ICAT getInstance(){
        ((BindingProvider)icatPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, END_POINT_ADDRESS);        
        System.out.println("Using WSDL: "+((BindingProvider)icatPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
        return icatPort;
    }    
}
