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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
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
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.Cascade;
import uk.icat3.util.DatasetInclude;

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
    @NamedQuery(name = "Dataset.findbyUnique", query = "SELECT d FROM Dataset d WHERE d.sampleId = :sampleId AND d.name = :name AND d.investigationId = :investigationId AND d.datasetType = :datasetType")
})
@XmlRootElement
@SequenceGenerator(name="DATASET_SEQ",sequenceName="DATASET_ID_SEQ",allocationSize=1)
public class Dataset extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="DATASET_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "SAMPLE_ID")
    private Long sampleId;
    
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @JoinColumn(name = "DATASET_STATUS", referencedColumnName = "NAME")
    @ManyToOne
    private DatasetStatus datasetStatus;
    
    @JoinColumn(name = "DATASET_TYPE", referencedColumnName = "NAME", nullable=false)
    @ManyToOne
    private DatasetType datasetType;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
    @ManyToOne
    @XmlTransient
    @ICAT(merge=false)
    private Investigation investigationId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private Collection<DatasetParameter> datasetParameterCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetId")
    private Collection<Datafile> datafileCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetId")
    private Collection<DatasetLevelPermission> datasetLevelPermissionCollection;
      
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
     * Sets the id of this Dataset to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
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
     * Gets the datasetStatus of this Dataset.
     * @return the datasetStatus
     */
    public DatasetStatus getDatasetStatus() {
        return this.datasetStatus;
    }
    
    /**
     * Sets the datasetStatus of this Dataset to the specified value.
     * @param datasetStatus the new datasetStatus
     */
    public void setDatasetStatus(DatasetStatus datasetStatus) {
        this.datasetStatus = datasetStatus;
    }
    
    /**
     * Gets the datasetType of this Dataset.
     * @return the datasetType
     */
    public DatasetType getDatasetType() {
        return this.datasetType;
    }
    
    /**
     * Sets the datasetType of this Dataset to the specified value.
     * @param datasetType the new datasetType
     */
    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }
    
    /**
     * Gets the investigationId of this Dataset.
     * @return the investigationId
     */
    @XmlTransient
    public Investigation getInvestigationId() {
        return this.investigationId;
    }
    
    /**
     * Sets the investigationId of this Dataset to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Investigation investigationId) {
        this.investigationId = investigationId;
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
        if(datasetInclude.toString().equals(DatasetInclude.DATASET_FILES_AND_PARAMETERS.toString())){
            return this.datasetParameterCollection;
        } else if(datasetInclude.toString().equals(DatasetInclude.DATASET_PARAMETERS_ONY.toString())){
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
        if(datasetInclude.toString().equals(DatasetInclude.DATASET_FILES_AND_PARAMETERS.toString())){
            return this.datafileCollection;
        } else if(datasetInclude.toString().equals(DatasetInclude.DATASET_FILES_ONLY.toString())){
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
        dataFile.setDatasetId(this);
        
        Collection<Datafile> datafiles = this.getDatafileCollection();
        if(datafiles == null) datafiles = new ArrayList<Datafile>();
        datafiles.add(dataFile);
        
        this.setDatafileCollection(datafiles);
    }
    
    /**
     * Gets the datasetLevelPermissionCollection of this Dataset.
     * @return the datasetLevelPermissionCollection
     */
    @XmlTransient
    public Collection<DatasetLevelPermission> getDatasetLevelPermissionCollection() {
        return this.datasetLevelPermissionCollection;
    }
    
    /**
     * Sets the datasetLevelPermissionCollection of this Dataset to the specified value.
     * @param datasetLevelPermissionCollection the new datasetLevelPermissionCollection
     */
    public void setDatasetLevelPermissionCollection(Collection<DatasetLevelPermission> datasetLevelPermissionCollection) {
        this.datasetLevelPermissionCollection = datasetLevelPermissionCollection;
    }
    
    /**
     * Sets deleted flag on all items owned by this datasets
     *
     * @param isDeleted
     */
    public void setCascade(Cascade type, Object value){
        log.trace("Setting: "+toString()+" from type: "+type+" to :"+value);
        String deleted = "Y";
        if(type == Cascade.DELETE){
            deleted = (((Boolean)value).booleanValue()) ? "Y" : "N";
        }
        
        //data set parameters
        if(getDatasetParameterCollection() != null){
            for(DatasetParameter datasetParameter : getDatasetParameterCollection()){
                if(type == Cascade.DELETE) datasetParameter.setDeleted(deleted);
                else if(type == Cascade.MOD_ID) datasetParameter.setModId(value.toString());
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    datasetParameter.setModId(value.toString());
                    datasetParameter.setCreateId(value.toString());
                }
            }
        }
        
        //datafiles
        if(getDatafileCollection() != null){
            for(Datafile datafile : getDatafileCollection()){
                if(type == Cascade.DELETE) datafile.setDeleted(deleted);
                else if(type == Cascade.MOD_ID) datafile.setModId(value.toString());
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    datafile.setModId(value.toString());
                    datafile.setCreateId(value.toString());
                }
            }
        }
        
        //access groups
        if(getDatasetLevelPermissionCollection() != null){
            for(DatasetLevelPermission datasetLevelPermission : getDatasetLevelPermissionCollection()){
                datasetLevelPermission.setDeleted(deleted);
                for(AccessGroupDlp agdlp : datasetLevelPermission.getAccessGroupDlpCollection()){
                    if(type == Cascade.DELETE) agdlp.setDeleted(deleted);
                }
            }
            
        }
        
        if(type == Cascade.DELETE) this.setDeleted(deleted);
        else if(type == Cascade.MOD_ID) this.setModId(value.toString());
        else if(type == Cascade.MOD_AND_CREATE_IDS) {
            this.setModId(value.toString());
            this.setCreateId(value.toString());
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
        return "uk.icat3.entity.Dataset[id=" + id + "]";
    }
    
    /**
     * Method to be overridden if needed to check if the data held in the entity is valid.
     * This method checks whether all the fields which are marked as not null are not null
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
    @Override
    public boolean isValid() throws ValidationException {
        
        //get public the fields in class
        Field[] allFields = this.getClass().getDeclaredFields();
        //all subclasses should use this line below
        //Field[] allFields = getClass().getDeclaredFields();
        outer:
            for (int i = 0; i < allFields.length; i++) {
            //get name of field
            String fieldName = allFields[i].getName();
            
            //check if field is labeled id and generateValue (primary key, then it can be null)
            boolean id = false;
            boolean generateValue = false;
            
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                if(a.annotationType().getName().equals(javax.persistence.Id.class.getName())){
                    id = true;     }
                if(a.annotationType().getName().equals(javax.persistence.GeneratedValue.class.getName())){
                    generateValue = true;
                }
                if(generateValue && id) {
                    log.trace(getClass().getSimpleName()+": "+fieldName+" is auto generated id value, no need to check.");
                    continue outer;
                }
            }
            
            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //if this means its a none null column field
                if((a.annotationType().getName().equals(
                        javax.persistence.Column.class.getName()) ||
                        a.annotationType().getName().equals(
                        javax.persistence.JoinColumn.class.getName())) && a.toString().contains("nullable=false") ){
                    
                    //now check if it is null, if so throw error
                    try {
                        //get value
                        if(allFields[i].get(this) == null){
                            throw new ValidationException(getClass().getSimpleName()+": "+fieldName+" cannot be null.");
                        } else {
                            log.trace(getClass().getSimpleName()+": "+fieldName+" is valid");
                        }
                    } catch (IllegalAccessException ex) {
                        log.warn(getClass().getSimpleName()+": "+fieldName+" cannot be accessed.");
                    }
                }
            }
            }
        //ok here
        return super.isValid();
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
        
        
        //check sample info, sample id must be a part of in investigations aswell
        outer: if(sampleId != null){
            //check valid sample id
            
            Sample sampleRef = manager.find(Sample.class,sampleId);
            if(sampleRef == null)
                throw new ValidationException("Sample[id="+sampleId+"] is not a valid sample id");
            
            Collection<Sample> samples = investigationId.getSampleCollection();
            for(Sample sample : samples){
                if(sample.getId().equals(sampleId)){
                    //invest has for this sample in
                    break outer;
                }
            }
            //if here not got sample in
            throw new ValidationException("Sample[id="+sampleId+"] is not associated with Dataset[id="+id+ "]'s invesigation.");
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
            datasetStatus.isValid(manager);
            
            //check datafile format is valid
            DatasetStatus status = manager.find(DatasetStatus.class, datasetStatus.getName());
            if(status == null)  throw new ValidationException(datasetStatus+ " is not a valid Dataset Status");
        }
        
        //check is valid status
        if(datasetType != null){
            datasetType.isValid(manager);
            
            //check datafile format is valid
            DatasetType type = manager.find(DatasetType.class, datasetType.getName());
            if(type == null)  throw new ValidationException(datasetType+ " is not a valid Dataset Type");
        }
        
        //check if unique
        if(!isUnique(manager)) throw new ValidationException(this+" is not unique.");
        
        return isValid();
    }
    
    private boolean isUnique(EntityManager manager){
        
        Query query =  manager.createNamedQuery("Dataset.findbyUnique");
        query = query.setParameter("sampleId",sampleId);
        query = query.setParameter("investigationId", investigationId);
        query = query.setParameter("datasetType",datasetType);
        query = query.setParameter("name",name);
        
        try {
            Dataset dataset = (Dataset)query.getSingleResult();
            //if found id is this id then it is unique
            if(dataset.getId().equals(id)) return true;
            else return false;
        } catch(NoResultException nre) {
            //means it is unique
            return true;
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
        if(this.id != null){
            log.warn("Attempting to save a dataset: "+id +" when it should be auto generated, nulling id");
            this.id = null;
        }
        super.prePersist();
    }
    
    /**
     * See InvestigationIncude.getInvestigatorCollection_()
     */
    public void setDatasetInclude(DatasetInclude datasetInclude) {
        this.datasetInclude = datasetInclude;
    }
}
