/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import uk.icat3.entity.ParameterPK;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import uk.icat3.entity.Parameter;
import uk.icat3.exposed.util.BaseTestClassTX;
import uk.icat3.exposed.util.TestUserLocal;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.ParameterValueType;
import static uk.icat3.exposed.util.TestConstants.*;

/**
 * This is test class for Parameter Manager bean
 * @author Mr. Srikanth Nagella
 */
public class TestParameterManagerBean extends BaseTestClassTX {

    private static Logger log = Logger.getLogger(TestParameterManagerBean.class);
    private static UserSessionLocal testUserSession = new TestUserLocal();


    @Before
    public void setUpPM(){
    }
    @After
    public void tearDownPM(){

    }
    
    /**
     * Test of createParameter method, of class ParameterManagerBean.
     */
    @Test
    public void testCreateParameter() throws Exception {
        log.info("Testing createParameter");
        String sessionId = VALID_SESSION;
        Parameter param = new Parameter();
        param.setParameterPK(new ParameterPK("meters","height"));
        param.setDatafileParameter(true);
        param.setDatasetParameter(true);
        param.setSampleParameter(false);
        param.setSearchable("Y");
        param.setValueType(ParameterValueType.NUMERIC);
        ParameterManagerBean instance = new ParameterManagerBean();
        instance.setUserSession(testUserSession);//This is required for the session checking
        instance.setEntityManager(em);
        instance.createParameter(sessionId, param);
    }

    /**
     * Test of updateParameter method, of class ParameterManagerBean.
     */
    @Test
    public void testUpdateParameter() throws Exception {
        log.info("Testing updateParameter");
        String sessionId = VALID_SESSION;
        String name = "height";
        String units = "meters";
        boolean isSearchable = false;
        boolean isDatasetParameter = true;
        boolean isDatafileParameter = true;
        boolean isSampleParameter = true;
        ParameterManagerBean instance = new ParameterManagerBean();
        instance.setUserSession(testUserSession);
        instance.setEntityManager(em);
        Parameter result = instance.updateParameter(sessionId, name, units, isSearchable, isDatasetParameter, isDatafileParameter, isSampleParameter);
        assertEquals(result.isDatasetParameter(), isDatasetParameter);
        assertEquals(result.isDatafileParameter(),isDatafileParameter);
        assertEquals(result.isSampleParameter(),isSampleParameter);
        assertEquals(result.getSearchable(),"N");
        assertEquals(name,"height");
        assertEquals(units,"meters");
    }

    /**
     * Test of removeParameter method, of class ParameterManagerBean.
     */
    @Test
    public void testRemoveParameter() throws Exception {
        log.info("Testing removeParameter");
        String sessionId = VALID_SESSION;
        String name = "height";
        String units = "meters";
        ParameterManagerBean instance = new ParameterManagerBean();
        instance.setUserSession(testUserSession);
        instance.setEntityManager(em);
        instance.removeParameter(sessionId, name, units);
    }
}