/*
 * Dataset.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 * Entity class Dataset
 *
 * @author gjd37
 */
@Entity
@Table(name = "DATASET", uniqueConstraints={@UniqueConstraint(columnNames={"SAMPLE_ID","INVESTIGATION_ID", "NAME","DATASET_TYPE"})})
@NamedQueries( {
    @NamedQuery(name = "Dataset.findById", query = "SELECT d FROM Dataset d WHERE d.id = :id"),
    @NamedQuery(name = "Dataset.findBySampleId", query = "SELECT d FROM Dataset d WHERE d.sampleId = :sampleId"),
    @NamedQuery(name = "Dataset.findByName", query = "SELECT d FROM Dataset d WHERE d.name = :name"),
    @NamedQuery(name = "Dataset.findByDescription", query = "SELECT d FROM Dataset d WHERE d.description = :description"),
    @NamedQuery(name = "Dataset.findByModTime", query = "SELECT d FROM Dataset d WHERE d.modTime = :modTime"),
    @NamedQuery(name = "Dataset.getBySampleId", query = "SELECT d FROM Dataset d where d.name = :sampleName"),
    @NamedQuery(name = "Dataset.findByModId", query = "SELECT d FROM Dataset d WHERE d.modId = :modId"),
    @NamedQuery(name = Queries.DATASET_FINDBY_UNIQUE, query = Queries.DATASET_FINDBY_UNIQUE_JPQL),
    @NamedQuery(name = Queries.DATASETS_BY_SAMPLES, query = Queries.DATASETS_BY_SAMPLES_JPQL),
    @NamedQuery(name = Queries.DATASET_FINDBY_NAME_NOTDELETED, query=Queries.DATASET_FINDBY_NAME_NOTDELETED_JPQL) 
})
        @XmlRootElement
        @SequenceGenerator(name="DATASET_SEQ",sequenceName="DATASET_ID_SEQ",allocationSize=1)
        public class Dataset extends EntityBaseBean implements Serializable {
    
    /**
     * Override logger
     */
    protected static Logger log = Logger.getLogger(Dataset.class);
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="DATASET_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "SAMPLE_ID")
    private Long sampleId;
           
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "LOCATION")
    private String location;
    
   /* @JoinColumn(name = "SAMPLE_ID", referencedColumnName = "ID", insertable=false, updatable=false)
    @ManyToOne
    private Sample sample;*/
     
   /* public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }*/
    
    @Column(name = "DATASET_STATUS")
    private String datasetStatus;
    
    public String getDatasetStatus() {
        return datasetStatus;
    }
    
    public void setDatasetStatus(String datasetStatus) {
        this.datasetStatus = datasetStatus;
    }
    
    /*@JoinColumn(name = "DATASET_STATUS", referencedColumnName = "NAME")
    @ManyToOne
    private DatasetStatus datasetStatus;*/
    
    @Column(name = "DATASET_TYPE", nullable = false)
    private String datasetType;
    
    public String getDatasetType() {
        return datasetType;
    }
    
    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }
    
    /*@JoinColumn(name = "DATASET_TYPE", referencedColumnName = "NAME", nullable=false)
    @ManyToOne
    private DatasetType datasetType;*/
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
    @ManyToOne(fetch=FetchType.LAZY)
    @XmlTransient
    @ICAT(merge=false)
    private Investigation investigation;
    
    @Transient
    @ICAT(merge=false, nullable=true)
    private transient Long investigationId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private Collection<DatasetParameter> datasetParameterCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private Collection<Datafile> datafileCollection;
    
    private transient DatasetInclude datasetInclude = DatasetInclude.NONE;
    
    /** Creates a new instance of Dataset */
    public Dataset() {
    }
    
    /**
     * Creates a new instance of Dataset with the specified values.
     * @param id the id of the Dataset
     */
    public Dataset(Long id) {
        this.id = id;
    }
    
    /**
     * Creates a new instance of Dataset with the specified values.
     * @param id the id of the Dataset
     * @param name the name of the Dataset
     * @param modTime the modTime of the Dataset
     * @param modId the modId of the Dataset
     */
    public Dataset(Long id, String name, Date modTime, String modId) {
        this.id = id;
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the id of this Dataset.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * 
     * @param id
     */
    public void setId(Long id) {
        this.id=id;
    }
    
    /**
     * Gets the sampleId of this Dataset.
     * @return the sampleId
     */
    public Long getSampleId() {
        return this.sampleId;
    }
    
    /**
     * Sets the sampleId of this Dataset to the specified value.
     * @param sampleId the new sampleId
     */
    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }
    
    /**
     * Gets the name of this Dataset.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this Dataset to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of this Dataset.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this Dataset to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the location of this Datafile.
     * @return the location
     */
    public String getLocation() {
        return this.location;
    }
    
    /**
     * Sets the location of this Datafile to the specified value.
     * @param location the new location
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Gets the datasetStatus of this Dataset.
     * @return the datasetStatus
     */
    /*public DatasetStatus getDatasetStatus() {
        return this.datasetStatus;
    }*/
    
    /**
     * Sets the datasetStatus of this Dataset to the specified value.
     * @param datasetStatus the new datasetStatus
     */
    /*public void setDatasetStatus(DatasetStatus datasetStatus) {
        this.datasetStatus = datasetStatus;
    }*/
    
    /**
     * Gets the datasetType of this Dataset.
     * @return the datasetType
     */
    /*public DatasetType getDatasetType() {
        return this.datasetType;
    }*/
    
    /**
     * Sets the datasetType of this Dataset to the specified value.
     * @param datasetType the new datasetType
     */
    /*public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }*/
    
    /**
     * Gets the investigationId of this Dataset.
     * @return the investigationId
     */
    public Long getInvestigationId() {
        return investigationId;
    }
    
    /**
     * Sets the investigationId of this Dataset to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }
    
    /**
     * Gets the investigation of this Dataset.
     * @return the investigation
     */
    @XmlTransient
    public Investigation getInvestigation() {
        return this.investigation;
    }
    
    /**
     * Sets the investigation of this Dataset to the specified value.
     * @param investigation the new investigation
     */
    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }
    
    /**
     * Gets the datasetParameterCollection of this Dataset.
     * @return the datasetParameterCollection
     */
    @XmlTransient
    public Collection<DatasetParameter> getDatasetParameterCollection() {
        return this.datasetParameterCollection;
    }
    
    /**
     * This method is used by JAXWS to map to datasetParameterCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="datasetParameterCollection")
    private Collection<DatasetParameter> getDatasetParameterCollection_() {
        if(datasetInclude.isDatasetParameters()){
            return this.datasetParameterCollection;
        }  else return null;
    }
    
    private void setDatasetParameterCollection_(Collection<DatasetParameter> datasetParameterCollection) {
        this.datasetParameterCollection = datasetParameterCollection;
    }
    
    /**
     * Sets the datasetParameterCollection of this Dataset to the specified value.
     * @param datasetParameterCollection the new datasetParameterCollection
     */
    public void setDatasetParameterCollection(Collection<DatasetParameter> datasetParameterCollection) {
        this.datasetParameterCollection = datasetParameterCollection;
    }
    
    /**
     * Adds a dataset parameter to the data set in both directions for model
     */
    public void addDataSetParamaeter(DatasetParameter datasetParameter){
        datasetParameter.setDataset(this);
        
        Collection<DatasetParameter> datasetParameters = this.getDatasetParameterCollection();
        if(datasetParameters == null) datasetParameters = new ArrayList<DatasetParameter>();
        datasetParameters.add(datasetParameter);
        
        this.setDatasetParameterCollection(datasetParameters);
    }
    
    /**
     * Gets the datafileCollection of this Dataset.
     * @return the datafileCollection
     */
    @XmlTransient
    public Collection<Datafile> getDatafileCollection() {
        return this.datafileCollection;
    }
    
    /**
     * This method is used by JAXWS to map to datafileCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="datafileCollection")
    private Collection<Datafile> getDatafileCollection_() {
        if(datasetInclude.isDatafiles()){
            return this.datafileCollection;
        }  else return null;
    }
    
    private void setDatafileCollection_(Collection<Datafile> datafileCollection) {
        this.datafileCollection = datafileCollection;
    }
    
    /**
     * Sets the datafileCollection of this Dataset to the specified value.
     * @param datafileCollection the new datafileCollection
     */
    public void setDatafileCollection(Collection<Datafile> datafileCollection) {
        this.datafileCollection = datafileCollection;
    }
    
    /**
     * Adds a DataFile to the DataSet,
     * also adds the DataSet to the DataFile.
     */
    public void addDataFile(Datafile dataFile){
        dataFile.setDataset(this);
        
        Collection<Datafile> datafiles = this.getDatafileCollection();
        if(datafiles == null) datafiles = new ArrayList<Datafile>();
        datafiles.add(dataFile);
        
        this.setDatafileCollection(datafiles);
    }
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.DATASET;
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
            facilityAcquired = deleted;
        }
        
        if(type == Cascade.REMOVE_DELETED_ITEMS){
            log.trace("Remove from: ");
            log.trace("Datafiles? "+datasetInclude.isDatafiles());
            log.trace("Datasets parameters? "+datasetInclude.isDatasetParameters());
        }
        
        //data set parameters
        //if REMOVE_DELETED_ITEMS, check if investigationInclude wants keywords
        if(type == Cascade.REMOVE_DELETED_ITEMS && datasetInclude.isDatasetParameters() ){
            //create new collection if remove deleted items
            Collection<DatasetParameter> datasetparameters = new ArrayList<DatasetParameter>();
            
            for(DatasetParameter datasetParameter : getDatasetParameterCollection()){
                if(!datasetParameter.isDeleted()) datasetparameters.add(datasetParameter);
            }
            
            log.trace("Setting new datasetparameters of size: "+datasetparameters.size()+" because of deleted items from original size: "+getDatasetParameterCollection().size());
            this.setDatasetParameterCollection(datasetparameters);
        }  else  if(type != Cascade.REMOVE_DELETED_ITEMS &&  getDatasetParameterCollection() != null){
            
            for(DatasetParameter datasetParameter : getDatasetParameterCollection()){
                if(type == Cascade.DELETE) {
                    datasetParameter.setMarkedDeleted(deleted);
                    datasetParameter.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) datasetParameter.setModId(cascadeValue.toString());
                else if(type == Cascade.FACILITY_ACQUIRED) datasetParameter.setFacilityAcquired(facilityAcquired);
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    datasetParameter.setModId(cascadeValue.toString());
                    datasetParameter.setCreateId(cascadeValue.toString());
                }
            }
        }
        
        //datafiles
        if(type == Cascade.REMOVE_DELETED_ITEMS && datasetInclude.isDatafiles() ){
            //create new collection if remove deleted items
            Collection<Datafile> datafiles = new ArrayList<Datafile>();
            
            for(Datafile datafile : getDatafileCollection()){
                if(!datafile.isDeleted()) {
                    datafiles.add(datafile);
                    //cascade to datafile items
                    if(((Boolean)cascadeValue).booleanValue()) datafile.setCascade(Cascade.REMOVE_DELETED_ITEMS, cascadeValue);
                }
            }
            
            log.trace("Setting new datafileCollection of size: "+datafiles.size()+" because of deleted items from original size: "+getDatafileCollection().size());
            this.setDatafileCollection(datafiles);
        } else  if(type != Cascade.REMOVE_DELETED_ITEMS && getDatafileCollection() != null){
            
            for(Datafile datafile : getDatafileCollection()){
                datafile.setCascade(type, cascadeValue, manager, managerValue);
            }
        }
        
        //TODO need to do it for the icat authorisation entires (delete)
        //check if manager is null
        /*if(manager != null && type == Cascade.DELETE){
            Query query = manager.createNamedQuery("IcatAuthorisation.findByDatasetId").
                    setParameter("elementType", ElementType.DATASET.toString()).
                    setParameter("elementId", this.getId()).
                    setParameter("investigationId",this.getInvestigation().getId());
            Collection<IcatAuthorisation> icatAuthorisations = (Collection<IcatAuthorisation>)query.getResultList();
         
            //now mark them all as delete
            for (IcatAuthorisation icatAuthorisation : icatAuthorisations) {
                log.trace("Marking: "+icatAuthorisation+" as "+cascadeValue);
                icatAuthorisation.setMarkedDeleted(deleted);
                icatAuthorisation.setModId(managerValue.toString());
            }
        }*/
        
        if(type == Cascade.DELETE) {
            //need to check if use has permission to delete this
            //only check if the value of deleted is different the one wanting to be changed to
            if(this.isDeleted() != ((Boolean)cascadeValue).booleanValue()){
                GateKeeper.performAuthorisation(managerValue.toString(), this, AccessType.DELETE, manager);
                this.setMarkedDeleted(deleted);
                this.setModId(managerValue.toString());
            }
        } else if(type == Cascade.MOD_ID) this.setModId(cascadeValue.toString());
        else if(type == Cascade.FACILITY_ACQUIRED) this.setFacilityAcquired(facilityAcquired);
        else if(type == Cascade.MOD_AND_CREATE_IDS) {
            this.setModId(cascadeValue.toString());
            this.setCreateId(cascadeValue.toString());
        }
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
     * Determines whether another object is equal to this Dataset.  The result is
     * <code>true</code> if and only if the argument is not null and is a Dataset object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dataset)) {
            return false;
        }
        Dataset other = (Dataset)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Dataset[id=" + id + "]";
    }
    
    /**
     * Overrides the isValid function, checks each of the datafiles and datafile parameters are valid
     *
     * @throws ValidationException
     * @return
     */
    @Override
    public boolean isValid(EntityManager manager, boolean deepValidation) throws ValidationException {
        if(manager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        
        //check sample info, sample id must be a part of an investigations aswell
        outer: if(sampleId != null){
            //check valid sample id
            
            Sample sampleRef = manager.find(Sample.class,sampleId);
            if(sampleRef == null)
                throw new ValidationException("Sample[id="+sampleId+"] is not a valid sample id");
            
            Collection<Sample> samples = investigation.getSampleCollection();
            for(Sample sample : samples){
                log.trace("Sample for Investigation is: "+sample);
                if(sample.getId().equals(sampleId) && !sample.isDeleted()){
                    //invest has for this sample in
                    break outer;
                }
            }
            //if here not got sample in
            throw new ValidationException("Sample[id="+sampleId+"] is not associated with Dataset[id="+id+ "]'s investigation.");
        }
        
        if(deepValidation){
            //check all datafiles now
            if(getDatafileCollection() != null){
                for(Datafile datafile : getDatafileCollection()){
                    datafile.isValid(manager);
                }
            }
            
            //check all datasetParameter now
            if(getDatasetParameterCollection() != null){
                for(DatasetParameter datasetParameter : getDatasetParameterCollection()){
                    datasetParameter.isValid(manager);
                }
            }
        }
        
        //check is valid status
        if(datasetStatus != null){
            //datasetStatus.isValid(manager);
            
            //check datafile format is valid
            DatasetStatus status = manager.find(DatasetStatus.class, datasetStatus);
            if(status == null)  throw new ValidationException(datasetStatus+ " is not a valid DatasetStatus");
        }
        
        //check is valid status
        if(datasetType != null){
            //datasetType.isValid(manager);
            
            //check datafile format is valid
            DatasetType type = manager.find(DatasetType.class, datasetType);
            if(type == null)  throw new ValidationException(datasetType+ " is not a valid DatasetType");
        }
        
        //check if unique
        if(!isUnique(manager)) throw new ValidationException(this+" is not unique.");
        
        return isValid();
    }
    
    /**
     * Checks weather the dataset is unique in the database.
     */
    private boolean isUnique(EntityManager manager) throws ValidationException{
        
        Query query =  manager.createNamedQuery("Dataset.findbyUnique");
        query = query.setParameter("sampleId",sampleId);
        query = query.setParameter("investigation", investigation);
        query = query.setParameter("datasetType",datasetType);
        query = query.setParameter("name",name);
        
        try {
            log.trace("Looking for: sampleId: "+ sampleId);
            log.trace("Looking for: investigation: "+ investigation);
            log.trace("Looking for: datasetType: "+datasetType);
            log.trace("Looking for: name: "+name);
            
            Dataset datasetFound = (Dataset)query.getSingleResult();
            log.trace("Returned: "+datasetFound);
            if(datasetFound.getId() != null && datasetFound.getId().equals(this.getId())) {
                log.trace("Dataset found is this dataset");
                return true;
            } else {
                log.trace("Dataset found is not this dataset, so not unique");
                throw new ValidationException(this+" is not unique. Same unique key as "+datasetFound);
            }
        } catch(NoResultException nre) {
            log.trace("No results so unique");
            //means it is unique
            return true;
        } catch(Throwable ex) {
            log.warn(ex);
            //means it is unique
            throw new ValidationException(this+" is not unique.");
        }
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
     * This loads the investigation id from the investigation
     */
    @PostLoad
    @PostPersist
    //@Override
    public void postLoad(){
        if(investigationId == null) investigationId = getInvestigation().getId();
        //super.postLoad();
    }
    
    /**
     * See InvestigationIncude.getInvestigatorCollection_()
     */
    public void setDatasetInclude(DatasetInclude datasetInclude) {
        this.datasetInclude = datasetInclude;
    }

    
}
