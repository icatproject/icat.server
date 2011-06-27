/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.Date;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.ParameterValueType;

/**
 * This is unit test for parameter manager.
 * @author Mr. Srikanth Nagella
 */
public class TestParameterManager extends BaseTestClassTX{
    private Parameter paramMts;
    public TestParameterManager() {
    }

    @Before
    public void setUpManager() throws Exception {
        //Find the parameter if it exists use it or else create a new
        try{
            paramMts = (Parameter) em.createNamedQuery("Parameter.findByNameAndUnits").setParameter("name", "length").setParameter("units", "meters").getSingleResult();
            return;
        }catch(Exception ex){
        }
        //Create a new parameter
        ParameterPK  pk = new ParameterPK("meters", "length");
        paramMts = new Parameter(pk, "Y", ParameterValueType.NUMERIC.getValue(), "N", "Y", "Y", "SUPER", new Date());
        paramMts.setCreateId("SUPER");
        paramMts.setCreateTime(new Date());
        paramMts.setDeleted(false);
        paramMts.setFacilityAcquiredSet(false);
        paramMts.setVerified(true);
        em.persist(paramMts);
        em.flush();
    }

    @After
    public void tearDownManager(){
        try{
            if(!em.getTransaction().getRollbackOnly()){
                em.remove(paramMts);
                em.flush();
            }
        }catch(Exception ex){
            System.out.println("Caught exception");
        }
    }

    /**
     * Test of getParameter method, of class ParameterManager.
     */
    @Test
    public void testGetParameter() {
        String name = "length";
        String units = "meters";
        Parameter expResult = paramMts;
        Parameter result = ParameterManager.getParameter(name, units, em);
        assertEquals(expResult, result);
    }

    /**
     * Test of getParameter method for non existing parameter name
     */
    @Test
    public void testGetParameterNonvalidParameterName(){
        String name = "lengths";
        String units = "meters";
        Parameter expResult = null;
        Parameter result = ParameterManager.getParameter(name, units, em);
        assertEquals(expResult,result);
    }

    /**
     * Test of createParameter method, of class ParameterManager.
     */
    @Test
    public void testCreateParameter() throws ValidationException{
        String userId = "test";
        String name = "distance";
        String units = "meters";
        ParameterValueType valueType = ParameterValueType.NUMERIC;
        boolean isSearchable = true;
        boolean isDatasetParameter = true;
        boolean isDatafileParameter = true;
        boolean isSampleParameter = false;
        Parameter result = ParameterManager.createParameter(userId, name, units, valueType, isSearchable, isDatasetParameter, isDatafileParameter, isSampleParameter, em);
        assertEquals(result.getParameterPK().getName(),name);
        assertEquals(result.getParameterPK().getUnits(),units);
        assertEquals(result.isSampleParameter(),isSampleParameter);
        assertEquals(result.isDatasetParameter(),isDatasetParameter);
        assertEquals(result.isDatafileParameter(),isDatafileParameter);
        em.remove(result);
    }

    /**
     * Test of updateParameter method, of class ParameterManager.
     */
    @Test
    public void testUpdateParameter() throws ValidationException {
        System.out.println("updateParameter");
        Parameter param = ParameterManager.updateParameter(paramMts.getCreateId(), paramMts.getParameterPK().getName(), paramMts.getParameterPK().getUnits(),true, paramMts.isDatasetParameter(), paramMts.isDatafileParameter(), !paramMts.isSampleParameter(), em);
        assertEquals(param.isDatasetParameter(),paramMts.isDatasetParameter());
        assertEquals(param.isDatafileParameter(),paramMts.isDatafileParameter());
        assertEquals(param.isSampleParameter(),true);
    }

    /**
     * Test of removeParameter method, of class ParameterManager.
     */
    @Test
    public void testRemoveParameter() throws ValidationException {
        //Create a new parameter
        ParameterPK  pk = new ParameterPK("kg", "weight");
        Parameter param = new Parameter(pk, "Y", ParameterValueType.NUMERIC.getValue(), "N", "Y", "Y", "SUPER", new Date());
        param.setCreateId("SUPER");
        param.setCreateTime(new Date());
        param.setDeleted(false);
        param.setFacilityAcquiredSet(false);
        param.setVerified(false);
        em.persist(param);
        em.flush();

        String userId = "SUPER";
        String name = "weight";
        String units = "kg";
        EntityManager manager = em;
        ParameterManager.removeParameter(userId, name, units, manager);
    }


    /**
     * Test of removeParameter method, of class ParameterManager.
     */
    @Test(expected = ValidationException.class)
    public void testRemoveParameterWithVerified() throws ValidationException {
        //Create a new parameter
        ParameterPK  pk = new ParameterPK("kg", "weight");
        Parameter param = new Parameter(pk, "Y", ParameterValueType.NUMERIC.getValue(), "N", "Y", "Y", "SUPER", new Date());
        param.setCreateId("SUPER");
        param.setCreateTime(new Date());
        param.setDeleted(false);
        param.setFacilityAcquiredSet(false);
        param.setVerified(true);
        em.persist(param);
        em.flush();

        String userId = "SUPER";
        String name = "weight";
        String units = "kg";
        EntityManager manager = em;
        ParameterManager.removeParameter(userId, name, units, manager);
    }

    /**
     * Test of updateParameter method, of class ParameterManager.
     */
    @Test(expected = ValidationException.class)
    public void testUpdateParameterNonExisting() throws ValidationException {
        System.out.println("updateParameter");
        Parameter param = ParameterManager.updateParameter(paramMts.getCreateId(), paramMts.getParameterPK().getName(), "kg",true, paramMts.isDatasetParameter(), paramMts.isDatafileParameter(), !paramMts.isSampleParameter(), em);
        fail("Shouldn't update as the parameter doesn't exist");
    }
    
    /**
     * Test of createParameter method, of class ParameterManager.
     */
    @Test(expected = ValidationException.class)
    public void testCreateParameterWithDuplicate() throws ValidationException{
        String userId = "test";
        String name = "length";
        String units = "meters";
        ParameterValueType valueType = ParameterValueType.NUMERIC;
        boolean isSearchable = true;
        boolean isDatasetParameter = true;
        boolean isDatafileParameter = true;
        boolean isSampleParameter = false;
        Parameter result = ParameterManager.createParameter(userId, name, units, valueType, isSearchable, isDatasetParameter, isDatafileParameter, isSampleParameter, em);
        fail("Shouldn't create a duplicate parameter");
    }
}