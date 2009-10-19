/*
 * Publication.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;

/**
 * Entity class Publication
 * 
 * @author gjd37
 */
@Entity
@Table(name = "PUBLICATION")
@NamedQueries( {
        @NamedQuery(name = "Publication.findById", query = "SELECT p FROM Publication p WHERE p.id = :id"),
        @NamedQuery(name = "Publication.findByFullReference", query = "SELECT p FROM Publication p WHERE p.fullReference = :fullReference"),
        @NamedQuery(name = "Publication.findByUrl", query = "SELECT p FROM Publication p WHERE p.url = :url"),
        @NamedQuery(name = "Publication.findByRepositoryId", query = "SELECT p FROM Publication p WHERE p.repositoryId = :repositoryId"),
        @NamedQuery(name = "Publication.findByRepository", query = "SELECT p FROM Publication p WHERE p.repository = :repository"),
        @NamedQuery(name = "Publication.findByModTime", query = "SELECT p FROM Publication p WHERE p.modTime = :modTime"),
        @NamedQuery(name = "Publication.findByModId", query = "SELECT p FROM Publication p WHERE p.modId = :modId")
    })
@SequenceGenerator(name="PUBLICATION_SEQ",sequenceName="PUBLICATION_ID_SEQ",allocationSize=1)
public class Publication extends EntityBaseBean implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="PUBLICATION_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "FULL_REFERENCE", nullable = false)
    private String fullReference;

    @Column(name = "URL")
    private String url;

    @Column(name = "REPOSITORY_ID")
    private String repositoryId;

    @Column(name = "REPOSITORY")
    private String repository;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne
    @XmlTransient
     @ICAT(merge=false)
    private Investigation investigationId;
    
    /** Creates a new instance of Publication */
    public Publication() {
    }

    /**
     * Creates a new instance of Publication with the specified values.
     * @param id the id of the Publication
     */
    public Publication(Long id) {
        this.id = id;
    }

    /**
     * Creates a new instance of Publication with the specified values.
     * @param id the id of the Publication
     * @param fullReference the fullReference of the Publication
     * @param modTime the modTime of the Publication
     * @param modId the modId of the Publication
     */
    public Publication(Long id, String fullReference, Date modTime, String modId) {
        this.id = id;
        this.fullReference = fullReference;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the id of this Publication.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id of this Publication to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the fullReference of this Publication.
     * @return the fullReference
     */
    public String getFullReference() {
        return this.fullReference;
    }

    /**
     * Sets the fullReference of this Publication to the specified value.
     * @param fullReference the new fullReference
     */
    public void setFullReference(String fullReference) {
        this.fullReference = fullReference;
    }

    /**
     * Gets the url of this Publication.
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the url of this Publication to the specified value.
     * @param url the new url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the repositoryId of this Publication.
     * @return the repositoryId
     */
    public String getRepositoryId() {
        return this.repositoryId;
    }

    /**
     * Sets the repositoryId of this Publication to the specified value.
     * @param repositoryId the new repositoryId
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * Gets the repository of this Publication.
     * @return the repository
     */
    public String getRepository() {
        return this.repository;
    }

    /**
     * Sets the repository of this Publication to the specified value.
     * @param repository the new repository
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }
   
    /**
     * Gets the investigationId of this Publication.
     * @return the investigationId
     */
     @XmlTransient
    public Investigation getInvestigationId() {
        return this.investigationId;
    }

    /**
     * Sets the investigationId of this Publication to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Investigation investigationId) {
        this.investigationId = investigationId;
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
     * Determines whether another object is equal to this Publication.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Publication object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Publication)) {
            return false;
        }
        Publication other = (Publication)object;
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
        return "Publication[id=" + id + "]";
    }
    
}
