/*
 * ICATSingleton.java
 *
 * Created on 25 June 2007, 10:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package uk.icat3.acctests.jmeter;

import javax.xml.ws.BindingProvider;
import uk.icat3.client.*;
import uk.icat3.client.admin.ICATAdmin;
import uk.icat3.client.admin.ICATAdminService;
import static uk.icat3.acctests.util.Constants.*;

/**
 *
 * @author gjd37
 */
public class ICATUtil {

    private static ICAT icatService = new ICATService().getICATPort();
    private static ICATAdmin icatAdmin = new ICATAdminService().getICATAdminPort();

    /** Creates a new instance of ICATSingleton */
    private ICATUtil() {
    }

    public static synchronized ICAT getPort() {
        return icatService;
    }

    public static synchronized ICATAdmin getAdminPort() {
        ((BindingProvider) icatAdmin).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, ICAT_ADMIN_USER);
        ((BindingProvider) icatAdmin).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, ICAT_ADMIN_PASSWORD);
        return icatAdmin;
    }
}
