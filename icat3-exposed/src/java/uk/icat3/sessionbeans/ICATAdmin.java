/*
 * AllOperationsBean.java
 *
 * Created on 17 May 2007, 10:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.SampleParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.user.UserDetails;

/**
 * This adds all the Admin methods to the ICAT methods and protects them with the role 'admin'
 *  
 * @author gjd37
 */
@Stateless()
//this is proteted from normal use because it has admin functions in it
@RolesAllowed("admin")
@WebService(/*serviceName="ICATService", name="ICATServices",*/ targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ICATAdmin extends ICAT implements ICATLocal {
    
    static Logger log = Logger.getLogger(ICATAdmin.class);
       
    /** Creates a new instance of AllOperationsBean */
    public ICATAdmin() {
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   Session  ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////     UserSession methods  /////////////////////////////////////////       
    //@WebMethod(operationName="loginAdmin")
    @ExcludeClassInterceptors
    //@RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginAdmin")
    //@ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginAdminResponse")
    public String loginAdmin(/*@WebParam(name="adminUsername") String adminUsername, @WebParam(name="AdminPassword") String AdminPassword, */ @WebParam(name="runAsUser") String runAsUser) throws SessionException{
        //Admin protected by GF authentication
        //return user.login(adminUsername, AdminPassword, runAsUser);
        return user.login("admin", "password" , runAsUser);
    }
    
    //TODO not yet supported
    /*ebMethod(operationName="loginCredentials")
    @RequestWrapper(className="uk.icat3.sessionbeans.jaxws.loginCredentials")
    @ResponseWrapper(className="uk.icat3.sessionbeans.jaxws.loginCredentialsResponse")
    public String login(@WebParam(name="credential") String credential) throws SessionException{
        return user.login(credential);
    }*/
      
    @WebMethod()
    public UserDetails getUserDetails(@WebParam(name="sessionId") String sessionId, @WebParam(name="usersName") String usersName) throws SessionException, NoSuchUserException{
        return this.user.getUserDetails(sessionId, usersName);
    }
    ///////////////////////////     End of UserSession methods  //////////////////////////////////
       
      
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////   All Manager   ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////     Investigation Manager methods  /////////////////////////////////////////
      
    /**
     * Removes the keyword from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keywordPK {@link KeywordPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeKeyword(@WebParam(name="sessionId") String sessionId, @WebParam(name="keywordPK") KeywordPK keywordPK) throws SessionException,  InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeKeyword(sessionId, keywordPK);
    }
           
    /**
     * Removes the investigator from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigatorPK {@link InvestigatorPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeInvestigator(@WebParam(name="sessionId") String sessionId, @WebParam(name="investigatorPK") InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeInvestigator(sessionId, investigatorPK);
    }
           
    /**
     * Removes the sample from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleId primary key object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeSample(@WebParam(name="sessionId") String sessionId, @WebParam(name="sampleId") Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeSample(sessionId, sampleId);
    }
       
    /**
     * Deletes the sample parameter from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameterPK {@link SampleParameterPK} primary key object to be deleted
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeSampleParameter(@WebParam(name="sessionId") String sessionId, @WebParam(name="sampleParameterPK") SampleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException{
        investigationManagerLocal.removeSampleParameter(sessionId, sampleParameterPK);
    }
        
    ///////////////////////////     End of Investigation Manager methods  /////////////////////////////////////////
    
    ///////////////////////////     Dataset Manager methods  /////////////////////////////////////////
     
    /**
     * Removes (from the database) the data set, and its dataset paramters and data files for a user depending if the
     * users id has remove permissions to delete the data set from the data set ID.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataSet(@WebParam(name="sessionId") String sessionId, @WebParam(name="dataSetId") Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.removeDataSet(sessionId, dataSetId);
    }
       
    /**
     * Removes the data set paramter, depending if the users has access to delete the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod
    public void removeDataSetParameter(@WebParam(name="sessionId") String sessionId, @WebParam(name="datasetParameterPK") DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datasetManagerLocal.removeDataSetParameter(sessionId, datasetParameterPK);
    }
           ///////////////////////////     End of Dataset Manager methods  /////////////////////////////////////////
    
    ///////////////////////////     Datafile Manager methods  /////////////////////////////////////////
       /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param datafileId id be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeDataFile(@WebParam(name="sessionId") String sessionId, @WebParam(name="datafileId") Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFile(sessionId, datafileId);
    }
    
    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod(operationName="removeDataFileObject")
    @RequestWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObject")
    @ResponseWrapper(className="uk.icat3.sessionbeans.manager.removeDataFileObjectResponse")
    public void removeDataFile(@WebParam(name="sessionId") String sessionId, @WebParam(name="dataFile") Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFile(sessionId, dataFile);
    }
              
       /**
     * Removes (from the database) a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be removed
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    @WebMethod()
    public void removeDataFileParameter(@WebParam(name="sessionId") String sessionId, @WebParam(name="datafileParameterPK") DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException {
        datafileManagerLocal.removeDataFileParameter(sessionId, datafileParameterPK);
    }
   
    ///////////////////////////     End of Datafile Manager methods  /////////////////////////////////////////
    
}
