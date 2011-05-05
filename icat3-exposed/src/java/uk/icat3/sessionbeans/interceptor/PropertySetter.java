/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.sessionbeans.interceptor;

import java.sql.Timestamp;
import javax.jms.Message;
import uk.icat3.logging.util.PropertyNames;

/**
 *
 * @author scb24683
 * This class sets standard properties that apply for all messages- sessionId, userId, method, time
 */
public class PropertySetter {


    public static Message setProperties(Message msg, String sessionId, String userId, String method, Timestamp time) {
        try {
            msg.setStringProperty(PropertyNames.USER_ID, userId);
            msg.setStringProperty(PropertyNames.SESSION_ID, sessionId);
            msg.setStringProperty(PropertyNames.TIME, time.toString());
            msg.setStringProperty(PropertyNames.METHOD, method);
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
