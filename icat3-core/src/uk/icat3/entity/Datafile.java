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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.DatafileInclude;
import uk.icat3.util.ElementType;

/**
 * Entity class Datafile
 *
 * @author gjd37
 * Modification:
 * 02-Sep-2009 (SN): Removed commented code and redundant code
 */
@Entity
@Table(name = "DATAFILE")
@NamedQueries({
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
    @NamedQuery(name = "Datafile.findByModId", query = "SELECT d FROM Datafile d WHERE d.modId = :modId")////Added searches for ICAT3 API
})

@XmlRootElement
@SequenceGenerator(name = "DATAFILE_SEQ", sequenceName = "DATAFILE_ID_SEQ", allocationSize = 1)
public class Datafile extends EntityBaseBean implements Serializable {

    /**
     * Override logger
     */
    protected static Logger log = Logger.getLogger(Datafile.class);
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATAFILE_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "NAME", nullable = false)
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date datafileCreateTime;
    
    @Column(name = "DATAFILE_MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datafileModifyTime;
    
    @Column(name = "FILE_SIZE")
    private Integer fileSize;
    
    @Column(name = "COMMAND")
    private String command;
    
    @Column(name = "CHECKSUM")
    private String checksum;
    
    @Column(name = "SIGNATURE")
    private String signature;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
    private Collection<RelatedDatafiles> relatedDatafilesCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile1")
    private Collection<RelatedDatafiles> relatedDatafilesCollection1;
    
    @JoinColumns(value = {
        @JoinColumn(name = "DATAFILE_FORMAT", referencedColumnName = "NAME"),
        @JoinColumn(name = "DATAFILE_FORMAT_VERSION", referencedColumnName = "VERSION")
    })
    @ManyToOne
    private DatafileFormat datafileFormat;
    
    @JoinColumn(name = "DATASET_ID", referencedColumnName = "ID")
    @ManyToOne
    @XmlTransient
    @ICAT(merge = false)
    private Dataset dataset;
    
    @Transient
    @ICAT(merge = false, nullable = true)
    private transient Long datasetId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datafile")
    private Collection<DatafileParameter> datafileParameterCollection;
    
    /**
     * What to include within the datafile for searches or gets
     */
    private transient DatafileInclude datafileInclude = DatafileInclude.ALL;

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
     * 
     * @param id
     */
    public void setId(Long id) {
        this.id=id;
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
    public Integer getFileSize() {
        return this.fileSize;
    }

    /**
     * Sets the fileSize of this Datafile to the specified value.
     * @param fileSize the new fileSize
     */
    public void setFileSize(Integer fileSize) {
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
     * Gets the relatedDatafilesCollection of this Datafile.
     * @return the relatedDatafilesCollection
     */
    @XmlTransient
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
     * This method is used by JAXWS to map to datasetParameterCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name = "relatedDatafilesCollection")
    private Collection<RelatedDatafiles> getRelatedDatafilesCollection_() {
        if (datafileInclude.isRelatedDatafiles()) {
            return this.relatedDatafilesCollection;
        } else {
            return null;
        }
    }

    private void setRelatedDatafilesCollection_(Collection<RelatedDatafiles> relatedDatafilesCollection) {
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
     * Gets the dataset of this Datafile.
     * @return the dataset
     */
    @XmlTransient
    public Dataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset of this Datafile to the specified value.
     * @param dataset the new dataset
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Gets the datasetId of this Datafile.
     * @return the datasetId
     */
    public Long getDatasetId() {
        return this.datasetId;
    }

    /**
     * Sets the datasetId of this Datafile to the specified value.
     * @param datasetId the new datasetId
     */
    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Gets the datafileParameterCollection of this Datafile.
     * @return the datafileParameterCollection
     */
    @XmlTransient
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
     * This method is used by JAXWS to map to datasetParameterCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name = "datafileParameterCollection")
    private Collection<DatafileParameter> getDatafileParameterCollection_() {
        if (datafileInclude.isDatafileParameters()) {
            return this.datafileParameterCollection;
        } else {
            return null;
        }
    }

    private void setDatafileParameterCollection_(Collection<DatafileParameter> datafileParameterCollection) {
        this.datafileParameterCollection = datafileParameterCollection;
    }

    /**
     * Adds a datafile parameter to the data file in both directions for model
     */
    public void addDataFileParameter(DatafileParameter dataFileParameter) {
        dataFileParameter.setDatafile(this);

        Collection<DatafileParameter> datafileParameters = this.getDatafileParameterCollection();
        if (datafileParameters == null) {
            datafileParameters = new ArrayList<DatafileParameter>();
        }
        datafileParameters.add(dataFileParameter);

        this.setDatafileParameterCollection(datafileParameters);
    }

    /**
     * Gets the root element type of the bean
     */
    public ElementType getRootElementType() {
        return ElementType.DATAFILE;
    }

    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param value value of the cascade type
     */
    public void setCascade(Cascade type, Object value) throws InsufficientPrivilegesException {
        setCascade(type, value, null, null);
    }

    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param value value of the cascade type
     */
    public void setCascade(Cascade type, Object value, EntityManager manager) throws InsufficientPrivilegesException {
        setCascade(type, value, manager, null);
    }

    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS, FACILITY_ACQUIRED and REMOVE_DELETED_ITEMS
     * @param cascadeValue value of the cascade type
     * @param manager entity manager to  connect to DB
     * @param managerValue value of the EntityManager value
     */
    public void setCascade(Cascade type, Object cascadeValue, EntityManager manager, Object managerValue) throws InsufficientPrivilegesException {
        log.trace("Cascading: " + toString() + " from type: " + type + " to :" + cascadeValue + " EntityManager: " + (manager == null ? "null" : "manager") + ", managerValue: " + managerValue);

        String deleted = "Y";
        String facilityAcquired = "Y";
        if (type == Cascade.DELETE || type == Cascade.FACILITY_ACQUIRED) {
            deleted = (((Boolean) cascadeValue).booleanValue()) ? "Y" : "N";
            facilityAcquired = deleted;
        }

        //data file parameters
        if (type == Cascade.REMOVE_DELETED_ITEMS && datafileInclude.isDatafileParameters()) {
            //create new collection if remove deleted items
            Collection<DatafileParameter> datafileparameters = new ArrayList<DatafileParameter>();

            for (DatafileParameter datafileParameter : getDatafileParameterCollection()) {
                if (!datafileParameter.isDeleted()) {
                    datafileparameters.add(datafileParameter);
                }
            }
            //now set the new dataset collection
            log.trace("Setting new datafileparameters of size: " + datafileparameters.size() + " because of deleted items from original size: " + getDatafileParameterCollection().size());
            this.setDatafileParameterCollection(datafileparameters);
        }
        if (type != Cascade.REMOVE_DELETED_ITEMS && getDatafileParameterCollection() != null) {

            for (DatafileParameter datafileParameter : getDatafileParameterCollection()) {
                if (type == Cascade.DELETE) {
                    datafileParameter.setMarkedDeleted(deleted);
                    datafileParameter.setModId(managerValue.toString());
                } else if (type == Cascade.MOD_ID) {
                    datafileParameter.setModId(cascadeValue.toString());
                } else if (type == Cascade.FACILITY_ACQUIRED) {
                    datafileParameter.setFacilityAcquired(facilityAcquired);
                } else if (type == Cascade.MOD_AND_CREATE_IDS) {
                    datafileParameter.setModId(cascadeValue.toString());
                    datafileParameter.setCreateId(cascadeValue.toString());
                }
            }
        }

        //relatedDatafiles
        if (type == Cascade.REMOVE_DELETED_ITEMS && datafileInclude.isRelatedDatafiles()) {

            //create new collection if remove deleted items
            Collection<RelatedDatafiles> relatedDatafiles = new ArrayList<RelatedDatafiles>();

            for (RelatedDatafiles relatedDatafile : getRelatedDatafilesCollection()) {
                //remove all deleted items from the collection, ie only add ones that are not deleted
                if (!relatedDatafile.isDeleted()) {
                    relatedDatafiles.add(relatedDatafile);
                }
            }
            //now set the new dataset collection
            log.trace("Setting new relatedDatafiles of size: " + relatedDatafiles.size() + " because of deleted items from original size: " + getRelatedDatafilesCollection().size());
            this.setRelatedDatafilesCollection(relatedDatafiles);
        }
        if (type != Cascade.REMOVE_DELETED_ITEMS && getRelatedDatafilesCollection() != null) {

            for (RelatedDatafiles relatedDatafile : getRelatedDatafilesCollection()) {
                if (type == Cascade.DELETE) {
                    relatedDatafile.setMarkedDeleted(deleted);
                    relatedDatafile.setModId(managerValue.toString());
                } else if (type == Cascade.MOD_ID) {
                    relatedDatafile.setModId(cascadeValue.toString());
                } else if (type == Cascade.FACILITY_ACQUIRED) {
                    relatedDatafile.setFacilityAcquired(facilityAcquired);
                } else if (type == Cascade.MOD_AND_CREATE_IDS) {
                    relatedDatafile.setModId(cascadeValue.toString());
                    relatedDatafile.setCreateId(cascadeValue.toString());
                }
            }
        }

        //TODO need to do it for the icat authorisation entires (delete)
        //check if manager is null
        /*if(manager != null && type == Cascade.DELETE){
        Query query = manager.createNamedQuery("IcatAuthorisation.findByDatafileId").
        setParameter("elementType", ElementType.DATAFILE.toString()).
        setParameter("elementId", this.getId()).
        setParameter("investigationId",this.getDataset().getInvestigation().getId());
        Collection<IcatAuthorisation> icatAuthorisations = (Collection<IcatAuthorisation>)query.getResultList();
        
        //now mark them all as delete
        for (IcatAuthorisation icatAuthorisation : icatAuthorisations) {
        log.trace("Marking: "+icatAuthorisation+" as "+cascadeValue);
        icatAuthorisation.setMarkedDeleted(deleted);
        icatAuthorisation.setModId(managerValue.toString());
        }
        }*/

        if (type == Cascade.DELETE) {
            //need to check if use has permission to delete this
            //only check if the value of deleted is different the one wanting to be changed to
            if (this.isDeleted() != ((Boolean) cascadeValue).booleanValue()) {
                GateKeeper.performAuthorisation(managerValue.toString(), this, AccessType.DELETE, manager);
                this.setMarkedDeleted(deleted);
                this.setModId(managerValue.toString());
            }
        } else if (type == Cascade.MOD_ID) {
            this.setModId(cascadeValue.toString());
        } else if (type == Cascade.FACILITY_ACQUIRED) {
            this.setFacilityAcquired(facilityAcquired);
        } else if (type == Cascade.MOD_AND_CREATE_IDS) {
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
        Datafile other = (Datafile) object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Datafile[id=" + id + "]";
    }

    /**
     * Overrides the isValid function, checks each of the datafiles and datafile parameters are valid
     *
     * @throws ValidationException
     * @return
     */
    @Override
    public boolean isValid(EntityManager manager) throws ValidationException {
        if (manager == null) {
            throw new IllegalArgumentException("EntityManager cannot be null");        //check all datafiles now
        }
        if (getDatafileParameterCollection() != null) {
            for (DatafileParameter datafileParameter : getDatafileParameterCollection()) {
                datafileParameter.isValid(manager);
            }
        }

        if (datafileFormat != null) {
            datafileFormat.isValid(manager);

            //check datafile format is valid
            DatafileFormat format = manager.find(DatafileFormat.class, datafileFormat.getDatafileFormatPK());
            if (format == null) {
                throw new ValidationException(datafileFormat + " is not a valid DatafileFormat");
            }
        }

        return isValid();
    }

    /**
     * This method removes all the ids when persist is called.
     * This is so you cannot attach an Id when creating a dataset
     * that is not valid, ie auto generated
     */
    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
    }

    /**
     * This loads the investigation id from the investigation
     */
    @PostLoad
    @PostPersist
    //@Override
    public void postLoad() {
        if (datasetId == null) {
            datasetId = getDataset().getId();
        // super.postLoad();
        }
    }

    public DatafileInclude getDatafileInclude() {
        return datafileInclude;
    }

    public void setDatafileInclude(DatafileInclude datafileInclude) {
        this.datafileInclude = datafileInclude;
    }
}
