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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
        @NamedQuery(name = "Sample.findByProposalSampleId", query = "SELECT s FROM Sample s WHERE s.proposalSampleId = :proposalSampleId")
    })
public class Sample extends EntityBaseBean implements Serializable {

    @Id
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

    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;

    @Column(name = "PROPOSAL_SAMPLE_ID")
    private BigInteger proposalSampleId;

    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
    @ManyToOne
    private Investigation investigationId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
    private Collection<SampleParameter> sampleParameterCollection;
    
    /** Creates a new instance of Sample */
    public Sample() {
    }

    /**
     * Creates a new instance of Sample with the specified values.
     * @param id the id of the Sample
     */
    public Sample(Long id) {
        this.id = id;
    }

    /**
     * Creates a new instance of Sample with the specified values.
     * @param id the id of the Sample
     * @param name the name of the Sample
     * @param safetyInformation the safetyInformation of the Sample
     * @param modTime the modTime of the Sample
     * @param modId the modId of the Sample
     */
    public Sample(Long id, String name, String safetyInformation, Date modTime, String modId) {
        this.id = id;
        this.name = name;
        this.safetyInformation = safetyInformation;
        this.modTime = modTime;
        this.modId = modId;
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
     * Gets the modTime of this Sample.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }

    /**
     * Sets the modTime of this Sample to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * Gets the modId of this Sample.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this Sample to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }

    /**
     * Gets the proposalSampleId of this Sample.
     * @return the proposalSampleId
     */
    public BigInteger getProposalSampleId() {
        return this.proposalSampleId;
    }

    /**
     * Sets the proposalSampleId of this Sample to the specified value.
     * @param proposalSampleId the new proposalSampleId
     */
    public void setProposalSampleId(BigInteger proposalSampleId) {
        this.proposalSampleId = proposalSampleId;
    }

    /**
     * Gets the investigationId of this Sample.
     * @return the investigationId
     */
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
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.Sample[id=" + id + "]";
    }
    
}
