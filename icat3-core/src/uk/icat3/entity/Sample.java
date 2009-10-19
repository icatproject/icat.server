/*
 * Sample.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.Cascade;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 * Entity class Sample
 *
 * @author gjd37
 */
@Entity
@Table(name = "SAMPLE")
@NamedQueries( {
    @NamedQuery(name = "Sample.findById", query = "SELECT s FROM Sample s WHERE s.id = :id"),
    @NamedQuery(name = "Sample.findByName", query = "SELECT s FROM Sample s WHERE s.name = :name"),
    @NamedQuery(name = "Sample.findByInstance", query = "SELECT s FROM Sample s WHERE s.instance = :instance"),
    @NamedQuery(name = "Sample.findByChemicalFormula", query = "SELECT s FROM Sample s WHERE s.chemicalFormula = :chemicalFormula"),
    @NamedQuery(name = "Sample.findBySafetyInformation", query = "SELECT s FROM Sample s WHERE s.safetyInformation = :safetyInformation"),
    @NamedQuery(name = "Sample.findByModTime", query = "SELECT s FROM Sample s WHERE s.modTime = :modTime"),
    @NamedQuery(name = "Sample.findByModId", query = "SELECT s FROM Sample s WHERE s.modId = :modId"),
    @NamedQuery(name = "Sample.findByUnique", query = "SELECT s FROM Sample s WHERE s.name = :name AND s.instance = :instance AND s.investigationId = :investigationId"),
    @NamedQuery(name = "Sample.findByProposalSampleId", query = "SELECT s FROM Sample s WHERE s.proposalSampleId = :proposalSampleId"),
    @NamedQuery(name = Queries.SAMPLES_BY_NAME, query = Queries.SAMPLES_BY_NAME_JPQL)
    
})
        @SequenceGenerator(name="SAMPLE_SEQ",sequenceName="SAMPLE_ID_SEQ",allocationSize=1)
        public class Sample extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="SAMPLE_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "INSTANCE")
    private String instance;
    
    @Column(name = "CHEMICAL_FORMULA")
    private String chemicalFormula;
    
    @Column(name = "SAFETY_INFORMATION", nullable = false)
    private String safetyInformation;
    
    @Column(name = "PROPOSAL_SAMPLE_ID")
    private Integer proposalSampleId;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
    @ManyToOne(fetch=FetchType.LAZY)
    @XmlTransient
    @ICAT(merge=false)
    private Investigation investigationId;
    
    /*@Column(name = "INVESTIGATION_ID", nullable = false)
    private Long investigationId;*/
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
    private Collection<SampleParameter> sampleParameterCollection;
    
    /** Creates a new instance of Sample */
    public Sample() {
    }
    
    /**
     * Creates a new instance of Sample with the specified values.
     * @param id the id of the Sample
     * @param name the name of the Sample
     * @param safetyInformation the safetyInformation of the Sample
     * @param modTime the modTime of the Sample
     * @param modId the modId of the Sample
     */
    public Sample(String name, String safetyInformation) {
        this.name = name;
        this.safetyInformation = safetyInformation;
    }
    
    /**
     * Gets the id of this Sample.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this Sample to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the name of this Sample.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this Sample to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the instance of this Sample.
     * @return the instance
     */
    public String getInstance() {
        return this.instance;
    }
    
    /**
     * Sets the instance of this Sample to the specified value.
     * @param instance the new instance
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }
    
    /**
     * Gets the chemicalFormula of this Sample.
     * @return the chemicalFormula
     */
    public String getChemicalFormula() {
        return this.chemicalFormula;
    }
    
    /**
     * Sets the chemicalFormula of this Sample to the specified value.
     * @param chemicalFormula the new chemicalFormula
     */
    public void setChemicalFormula(String chemicalFormula) {
        this.chemicalFormula = chemicalFormula;
    }
    
    /**
     * Gets the safetyInformation of this Sample.
     * @return the safetyInformation
     */
    public String getSafetyInformation() {
        return this.safetyInformation;
    }
    
    /**
     * Sets the safetyInformation of this Sample to the specified value.
     * @param safetyInformation the new safetyInformation
     */
    public void setSafetyInformation(String safetyInformation) {
        this.safetyInformation = safetyInformation;
    }
    
    /**
     * Gets the proposalSampleId of this Sample.
     * @return the proposalSampleId
     */
    public Integer getProposalSampleId() {
        return this.proposalSampleId;
    }
    
    /**
     * Sets the proposalSampleId of this Sample to the specified value.
     * @param proposalSampleId the new proposalSampleId
     */
    public void setProposalSampleId(Integer proposalSampleId) {
        this.proposalSampleId = proposalSampleId;
    }
    
    /**
     * Gets the investigationId of this Sample.
     * @return the investigationId
     */
    @XmlTransient
    public Investigation getInvestigationId() {
        return this.investigationId;
    }
    
    /**
     * Sets the investigationId of this Sample to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Investigation investigationId) {
        this.investigationId = investigationId;
    }
    
    /**
     * Gets the sampleParameterCollection of this Sample.
     * @return the sampleParameterCollection
     */
    public Collection<SampleParameter> getSampleParameterCollection() {
        return this.sampleParameterCollection;
    }
    
    /**
     * Sets the sampleParameterCollection of this Sample to the specified value.
     * @param sampleParameterCollection the new sampleParameterCollection
     */
    public void setSampleParameterCollection(Collection<SampleParameter> sampleParameterCollection) {
        this.sampleParameterCollection = sampleParameterCollection;
    }
    
    /**
     * Adds a SampleParameter to the sample,
     * also adds the sample to the SampleParameter.
     */
    public void addSampleParameter(SampleParameter sampleParameter){
        sampleParameter.setSample(this);
        
        Collection<SampleParameter> sampleParameters = this.getSampleParameterCollection();
        if(sampleParameters == null) sampleParameters = new ArrayList<SampleParameter>();
        sampleParameters.add(sampleParameter);
        
        this.setSampleParameterCollection(sampleParameters);
    }
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    /**
     * Used to see if there is a unique sample already in Db so that it can be merged or set as deleted
     */
    public Sample find(EntityManager manager) {
        return (Sample)manager.createNamedQuery("Sample.findByUnique").setParameter("name", name).setParameter("instance", instance).setParameter("investigationId", investigationId).getSingleResult();
    }
    
    /**
     * Checks weather the sample is unique in the database.
     */
    private boolean isUnique(EntityManager manager) throws ValidationException{
        try {
            Sample sample = (Sample)manager.createNamedQuery("Sample.findByUnique").setParameter("name", name).setParameter("instance", instance).setParameter("investigationId", investigationId).getSingleResult();
            if(id != null && sample.getId().equals(id)) return true;
            throw new ValidationException(this+" is not unique. Same unique key as "+sample);
        } catch(NoResultException nre) {
            return true;
        }
    }
    
    /**
     * Checks weather the sample is linked to any datasets
     */
    public boolean isLinked(EntityManager manager) throws InsufficientPrivilegesException{
        try {
            Collection<Dataset> datasets = (Collection<Dataset>)manager.createNamedQuery(Queries.DATASETS_BY_SAMPLES).setParameter("sampleId", this.id).getResultList();
            if(datasets != null && datasets.size() == 0) return true;
            else throw new InsufficientPrivilegesException(this+" is linked to "+datasets+" and therefore you cannot REMOVE/DELETE it.");
        } catch(NoResultException nre) {
            return false;
        }
    }
    
    
    
    /**
     * Overrides the isValid function, checks each of the sampleparameters are valid
     *
     * @throws ValidationException
     * @return
     */
    @Override
    public boolean isValid(EntityManager manager, boolean deepValidation) throws ValidationException {
        if(manager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        
        if(deepValidation){
            if(getSampleParameterCollection() != null){
                for(SampleParameter sampleParameter : getSampleParameterCollection()){
                    sampleParameter.isValid(manager);
                }
            }
        }
        //check if unique
        isUnique(manager);
        
        
        //once here then its valid
        return isValid();
    }
    
    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param value value of the cascade type
     */
    public void setCascade(Cascade type, Object value) throws InsufficientPrivilegesException{
        setCascade(type, value, null, null);
    }
    
    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param value value of the cascade type
     */
    public void setCascade(Cascade type, Object value, EntityManager manager) throws InsufficientPrivilegesException{
        setCascade(type, value, manager, null);
    }
    
    
    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param cascadeValue value of the cascade type
     * @param manager entity manager to  connect to DB
     * @param managerValue value of the EntityManager value
     */
    public void setCascade(Cascade type, Object cascadeValue, EntityManager manager, Object managerValue) throws InsufficientPrivilegesException{
        log.trace("Cascading: "+toString()+" from type: "+type+" to :"+cascadeValue+" EntityManager: "+(manager == null ? "null" : "manager")+", managerValue: "+ managerValue);
        
        String deleted = "Y";
        String facilityAcquired = "Y";
        if(type == Cascade.DELETE || type == Cascade.FACILITY_ACQUIRED){
            deleted = (((Boolean)cascadeValue).booleanValue()) ? "Y" : "N";
            if(managerValue == null && type == Cascade.DELETE) throw new RuntimeException("Manager Value needs to be set aswell if Cascade.DELETE");
        }
        
        if(getSampleParameterCollection() != null){
            //create new collection if remove deleted items
            Collection<SampleParameter> sampleParameters = new ArrayList<SampleParameter>();
            
            for(SampleParameter sp : getSampleParameterCollection()){
                if(type == Cascade.DELETE)  sp.setMarkedDeleted(deleted);
                else if(type == Cascade.MOD_ID) sp.setModId(cascadeValue.toString());
                else if(type == Cascade.FACILITY_ACQUIRED) sp.setFacilityAcquired(facilityAcquired);
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    sp.setModId(cascadeValue.toString());
                    sp.setCreateId(cascadeValue.toString());
                }  else if(type == Cascade.REMOVE_DELETED_ITEMS){
                    //remove all deleted items from the collection, ie only add ones that are not deleted
                    if(!sp.isDeleted()) sampleParameters.add(sp);
                }
            }
            //now set the new dataset collection
            log.trace("Setting new sampleParameterCollection of size: "+sampleParameters.size()+" because of deleted items from original size: "+getSampleParameterCollection().size());
            this.setSampleParameterCollection(sampleParameters);
        }
        
        if(type == Cascade.DELETE) this.setMarkedDeleted(deleted);
        else if(type == Cascade.MOD_ID) this.setModId(cascadeValue.toString());
        else if(type == Cascade.FACILITY_ACQUIRED) this.setFacilityAcquired(facilityAcquired);
        else if(type == Cascade.MOD_AND_CREATE_IDS) {
            this.setModId(cascadeValue.toString());
            this.setCreateId(cascadeValue.toString());
        }
    }
    
    /**
     * Determines whether another object is equal to this Sample.  The result is
     * <code>true</code> if and only if the argument is not null and is a Sample object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Sample)) {
            return false;
        }
        Sample other = (Sample)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }
    
    /**
     * This method removes all the ids when persist is called.
     * This is so you cannot attach an Id when creating a dataset
     * that is not valid, ie auto generated
     */
    @PrePersist
    @Override
    public void prePersist(){
        super.prePersist();
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Sample[id=" + id + "]";
    }
}
