/*
 * Datafile.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.ValidationException;

/**
 * Entity class Datafile
 *
 * @author gjd37
 */
@Entity
@Table(name = "DATAFILE")
@NamedQueries( {
    @NamedQuery(name = "Datafile.findById", query = "SELECT d FROM Datafile d WHERE d.id = :id"),
    @NamedQuery(name = "Datafile.findByName", query = "SELECT d FROM Datafile d WHERE d.name = :name"),
    @NamedQuery(name = "Datafile.findByDescription", query = "SELECT d FROM Datafile d WHERE d.description = :description"),
    @NamedQuery(name = "Datafile.findByDatafileVersion", query = "SELECT d FROM Datafile d WHERE d.datafileVersion = :datafileVersion"),
    @NamedQuery(name = "Datafile.findByDatafileVersionComment", query = "SELECT d FROM Datafile d WHERE d.datafileVersionComment = :datafileVersionComment"),
    @NamedQuery(name = "Datafile.findByLocation", query = "SELECT d FROM Datafile d WHERE d.location = :location"),
    @NamedQuery(name = "Datafile.findByDatafileCreateTime", query = "SELECT d FROM Datafile d WHERE d.datafileCreateTime = :datafileCreateTime"),
    @NamedQuery(name = "Datafile.findByDatafileModifyTime", query = "SELECT d FROM Datafile d WHERE d.datafileModifyTime = :datafileModifyTime"),
    @NamedQuery(name = "Datafile.findByFileSize", query = "SELECT d FROM Datafile d WHERE d.fileSize = :fileSize"),
    @NamedQuery(name = "Datafile.findByCommand", query = "SELECT d FROM Datafile d WHERE d.command = :command"),
    @NamedQuery(name = "Datafile.findByChecksum", query = "SELECT d FROM Datafile d WHERE d.checksum = :checksum"),
    @NamedQuery(name = "Datafile.findBySignature", query = "SELECT d FROM Datafile d WHERE d.signature = :signature"),
    @NamedQuery(name = "Datafile.findByModTime", query = "SELECT d FROM Datafile d WHERE d.modTime = :modTime"),
    @NamedQuery(name = "Datafile.findByModId", query = "SELECT d FROM Datafile d WHERE d.modId = :modId"),
    
    
    ////Added searches for ICAT3 API
    //@NamedQuery(name = "Datafile.findByRunNumber", query = "SELECT d FROM Datafile d WHERE d.datasetId.investigationId.investigatorCollection.investigatorPK.facilityUserId = :userId AND d.datasetId.investigationId.instrument.name = :instrument AND d.datafileParameterCollection.stringValue = 'run_number' AND d.datafileParameterCollection.numericValue BETWEEN :lower AND :upper")    
    @NamedQuery(name = "Datafile.findByRunNumber", query = "SELECT d FROM Datafile d WHERE   d.datafileParameterCollection.stringValue = 'run_number' AND d.datafileParameterCollection.numericValue BETWEEN :lower AND :upper")
})
@NamedNativeQueries({
    //Added searches for ICAT3 API
    // @NamedNativeQuery(name = Queries.DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER, query= Queries.DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_SQL, resultSetMapping="dataFileMapping")
    
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name="dataFileMapping",entities={@EntityResult(entityClass=Datafile.class)})
    
})
@XmlRootElement
@SequenceGenerator(name="DATAFILE_SEQ",sequenceName="DATAFILE_ID_SEQ",allocationSize=1)
public class Datafile extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="DATAFILE_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "DATAFILE_VERSION")
    private String datafileVersion;
    
    @Column(name = "DATAFILE_VERSION_COMMENT")
    private String datafileVersionComment;
    
    @Column(name = "LOCATION")
    private String location;
    
    @Column(name = "DATAFILE_CREATE_TIME")
    @Temporal(TemporalType.DATE)
    private Date datafileCreateTime;
    
    @Column(name = "DATAFILE_MODIFY_TIME")
    @Temporal(TemporalType.DATE)
    private Date datafileModifyTime;
    
    @Column(name = "FILE_SIZE")
    private BigInteger fileSize;
    
    @Column(name = "COMMAND")
    private String command;
    
    @Column(name = "CHECKSUM")
    private String checksum;
    
    @Column(name = "SIGNATURE")
    private String signature;
       
    @Column(name = "MOD_ID", nullable = false)
    private String modId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
    private Collection<RelatedDatafiles> relatedDatafilesCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile1")
    private Collection<RelatedDatafiles> relatedDatafilesCollection1;
    
    @JoinColumns(value =  {
        @JoinColumn(name = "DATAFILE_FORMAT", referencedColumnName = "NAME"),
@JoinColumn(name = "DATAFILE_FORMAT_VERSION", referencedColumnName = "VERSION")
    })
    @ManyToOne
    private DatafileFormat datafileFormat;
    
    @JoinColumn(name = "DATASET_ID", referencedColumnName = "ID")
    @ManyToOne
    @XmlTransient
    private Dataset datasetId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
    private Collection<DatafileParameter> datafileParameterCollection;
    
    /** Creates a new instance of Datafile */
    public Datafile() {
    }
    
    /**
     * Creates a new instance of Datafile with the specified values.
     * @param id the id of the Datafile
     */
    public Datafile(Long id) {
        this.id = id;
    }
    
    /**
     * Creates a new instance of Datafile with the specified values.
     * @param id the id of the Datafile
     * @param modTime the modTime of the Datafile
     * @param modId the modId of the Datafile
     */
    public Datafile(Long id, Date modTime, String modId) {
        this.id = id;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the id of this Datafile.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this Datafile to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the name of this Datafile.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this Datafile to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of this Datafile.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this Datafile to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the datafileVersion of this Datafile.
     * @return the datafileVersion
     */
    public String getDatafileVersion() {
        return this.datafileVersion;
    }
    
    /**
     * Sets the datafileVersion of this Datafile to the specified value.
     * @param datafileVersion the new datafileVersion
     */
    public void setDatafileVersion(String datafileVersion) {
        this.datafileVersion = datafileVersion;
    }
    
    /**
     * Gets the datafileVersionComment of this Datafile.
     * @return the datafileVersionComment
     */
    public String getDatafileVersionComment() {
        return this.datafileVersionComment;
    }
    
    /**
     * Sets the datafileVersionComment of this Datafile to the specified value.
     * @param datafileVersionComment the new datafileVersionComment
     */
    public void setDatafileVersionComment(String datafileVersionComment) {
        this.datafileVersionComment = datafileVersionComment;
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
     * Gets the datafileCreateTime of this Datafile.
     * @return the datafileCreateTime
     */
    public Date getDatafileCreateTime() {
        return this.datafileCreateTime;
    }
    
    /**
     * Sets the datafileCreateTime of this Datafile to the specified value.
     * @param datafileCreateTime the new datafileCreateTime
     */
    public void setDatafileCreateTime(Date datafileCreateTime) {
        this.datafileCreateTime = datafileCreateTime;
    }
    
    /**
     * Gets the datafileModifyTime of this Datafile.
     * @return the datafileModifyTime
     */
    public Date getDatafileModifyTime() {
        return this.datafileModifyTime;
    }
    
    /**
     * Sets the datafileModifyTime of this Datafile to the specified value.
     * @param datafileModifyTime the new datafileModifyTime
     */
    public void setDatafileModifyTime(Date datafileModifyTime) {
        this.datafileModifyTime = datafileModifyTime;
    }
    
    /**
     * Gets the fileSize of this Datafile.
     * @return the fileSize
     */
    public BigInteger getFileSize() {
        return this.fileSize;
    }
    
    /**
     * Sets the fileSize of this Datafile to the specified value.
     * @param fileSize the new fileSize
     */
    public void setFileSize(BigInteger fileSize) {
        this.fileSize = fileSize;
    }
    
    /**
     * Gets the command of this Datafile.
     * @return the command
     */
    public String getCommand() {
        return this.command;
    }
    
    /**
     * Sets the command of this Datafile to the specified value.
     * @param command the new command
     */
    public void setCommand(String command) {
        this.command = command;
    }
    
    /**
     * Gets the checksum of this Datafile.
     * @return the checksum
     */
    public String getChecksum() {
        return this.checksum;
    }
    
    /**
     * Sets the checksum of this Datafile to the specified value.
     * @param checksum the new checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /**
     * Gets the signature of this Datafile.
     * @return the signature
     */
    public String getSignature() {
        return this.signature;
    }
    
    /**
     * Sets the signature of this Datafile to the specified value.
     * @param signature the new signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }
           
    /**
     * Gets the modId of this Datafile.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }
    
    /**
     * Sets the modId of this Datafile to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }
    
    /**
     * Gets the relatedDatafilesCollection of this Datafile.
     * @return the relatedDatafilesCollection
     */
    public Collection<RelatedDatafiles> getRelatedDatafilesCollection() {
        return this.relatedDatafilesCollection;
    }
    
    /**
     * Sets the relatedDatafilesCollection of this Datafile to the specified value.
     * @param relatedDatafilesCollection the new relatedDatafilesCollection
     */
    public void setRelatedDatafilesCollection(Collection<RelatedDatafiles> relatedDatafilesCollection) {
        this.relatedDatafilesCollection = relatedDatafilesCollection;
    }
    
    /**
     * Gets the relatedDatafilesCollection1 of this Datafile.
     * @return the relatedDatafilesCollection1
     */
    public Collection<RelatedDatafiles> getRelatedDatafilesCollection1() {
        return this.relatedDatafilesCollection1;
    }
    
    /**
     * Sets the relatedDatafilesCollection1 of this Datafile to the specified value.
     * @param relatedDatafilesCollection1 the new relatedDatafilesCollection1
     */
    public void setRelatedDatafilesCollection1(Collection<RelatedDatafiles> relatedDatafilesCollection1) {
        this.relatedDatafilesCollection1 = relatedDatafilesCollection1;
    }
    
    /**
     * Gets the datafileFormat of this Datafile.
     * @return the datafileFormat
     */
    public DatafileFormat getDatafileFormat() {
        return this.datafileFormat;
    }
    
    /**
     * Sets the datafileFormat of this Datafile to the specified value.
     * @param datafileFormat the new datafileFormat
     */
    public void setDatafileFormat(DatafileFormat datafileFormat) {
        this.datafileFormat = datafileFormat;
    }
    
    /**
     * Gets the datasetId of this Datafile.
     * @return the datasetId
     */
     @XmlTransient
    public Dataset getDatasetId() {
        return this.datasetId;
    }
    
    /**
     * Sets the datasetId of this Datafile to the specified value.
     * @param datasetId the new datasetId
     */
    public void setDatasetId(Dataset datasetId) {
        this.datasetId = datasetId;
    }
    
    /**
     * Gets the datafileParameterCollection of this Datafile.
     * @return the datafileParameterCollection
     */
    public Collection<DatafileParameter> getDatafileParameterCollection() {
        return this.datafileParameterCollection;
    }
    
    /**
     * Sets the datafileParameterCollection of this Datafile to the specified value.
     * @param datafileParameterCollection the new datafileParameterCollection
     */
    public void setDatafileParameterCollection(Collection<DatafileParameter> datafileParameterCollection) {
        this.datafileParameterCollection = datafileParameterCollection;
    }
    
    /**
     * Sets deleted flag on all items owned by this datafiles
     *
     * @param isDeleted 
     */
    public void setCascadeDeleted(boolean isDeleted){
        log.trace("Setting: "+toString()+" to deleted? "+isDeleted);
        String deleted = (isDeleted) ? "Y" : "N";
        
        //data file parameters
        for(DatafileParameter datafileParameter : getDatafileParameterCollection()){
            datafileParameter.setDeleted(deleted);
        }
        
        //relatedDatafiles
        for(RelatedDatafiles relatedDatafile : getRelatedDatafilesCollection()){
            relatedDatafile.setDeleted(deleted);
        }
        
        this.setDeleted(deleted);       
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
     * Determines whether another object is equal to this Datafile.  The result is
     * <code>true</code> if and only if the argument is not null and is a Datafile object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Datafile)) {
            return false;
        }
        Datafile other = (Datafile)object;
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
        return "uk.icat3.entity.Datafile[id=" + id + "]";
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
        for (int i = 0; i < allFields.length; i++) {
            //get name of field
            String fieldName = allFields[i].getName();
            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //if this means its a none null column field
                if(a.annotationType().getName().equals(
                        javax.persistence.Column.class.getName()) && a.toString().contains("nullable=false") ){
                    
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
    public boolean isValid(EntityManager manager) throws ValidationException {
        if(manager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        
                //check all datafiles now
        for(DatafileParameter datafileParameter : getDatafileParameterCollection()){
            datafileParameter.isValid(manager);
        }
                      
        return isValid();
    }
}
