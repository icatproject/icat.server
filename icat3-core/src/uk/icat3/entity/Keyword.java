/*
 * Keyword.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.ColumnResult;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityResult;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 * Entity class Keyword
 *
 * @author gjd37
 */
@Entity
@Table(name = "KEYWORD")
@NamedQueries( {
    @NamedQuery(name = "Keyword.findByInvestigationId", query = "SELECT k FROM Keyword k WHERE k.keywordPK.investigationId = :investigationId"),
    @NamedQuery(name = "Keyword.findByName", query = "SELECT k FROM Keyword k WHERE k.keywordPK.name = :name"),
    @NamedQuery(name = "Keyword.findByModTime", query = "SELECT k FROM Keyword k WHERE k.modTime = :modTime"),
    @NamedQuery(name = "Keyword.findByModId", query = "SELECT k FROM Keyword k WHERE k.modId = :modId"),
    
    //ICAT 3 queries
    @NamedQuery(name = Queries.ALLKEYWORDS, query = Queries.ALLKEYWORDS_JPQL),
    @NamedQuery(name = Queries.KEYWORDS_FOR_USER, query = Queries.KEYWORDS_FOR_USER_JPQL)
})
        
        @NamedNativeQueries({
    //Added searches for ICAT3 API
    @NamedNativeQuery(name = Queries.KEYWORDS_FOR_USER_ALPHA, query = Queries.KEYWORDS_FOR_USER_ALPHA_SQL, resultSetMapping="keywordsNameMapping"),
    @NamedNativeQuery(name = Queries.KEYWORDS_FOR_USER_ALPHA_NUMERIC, query = Queries.KEYWORDS_FOR_USER_ALPHA_NUMERIC_SQL, resultSetMapping="keywordsNameMapping"),
    
    @NamedNativeQuery(name = Queries.ALLKEYWORDS_NATIVE_ALPHA, query = Queries.ALLKEYWORDS_ALPHA_SQL, resultSetMapping="keywordsNameMapping"),
    @NamedNativeQuery(name = Queries.ALLKEYWORDS_NATIVE_ALPHA_NUMERIC, query = Queries.ALLKEYWORDS_ALPHA_NUMERIC_SQL, resultSetMapping="keywordsNameMapping")
    
})
        @SqlResultSetMappings({
    @SqlResultSetMapping(name="keywordsNameMapping",columns={@ColumnResult(name="NAME")}),
    @SqlResultSetMapping(name="keywordsMapping",entities={@EntityResult(entityClass=String.class)})
    
})
        public class Keyword extends EntityBaseBean implements Serializable {
    
    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected KeywordPK keywordPK;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    @XmlTransient
    @ICAT(merge=false)
    private Investigation investigation;
    
    /** Creates a new instance of Keyword */
    public Keyword() {
    }
    
    /**
     * Creates a new instance of Keyword with the specified values.
     * @param keywordPK the keywordPK of the Keyword
     */
    public Keyword(KeywordPK keywordPK) {
        this.keywordPK = keywordPK;
    }
    
    /**
     * Creates a new instance of Keyword with the specified values.
     * @param keywordPK the keywordPK of the Keyword
     * @param modTime the modTime of the Keyword
     * @param modId the modId of the Keyword
     */
    public Keyword(KeywordPK keywordPK, Date modTime, String modId) {
        this.keywordPK = keywordPK;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Creates a new instance of KeywordPK with the specified values.
     * @param name the name of the KeywordPK
     * @param investigationId the investigationId of the KeywordPK
     */
    public Keyword(String name, Long investigationId) {
        this.keywordPK = new KeywordPK(name, investigationId);
    }
    
    /**
     * Gets the keywordPK of this Keyword.
     * @return the keywordPK
     */
    public KeywordPK getKeywordPK() {
        return this.keywordPK;
    }
    
    /**
     * Sets the keywordPK of this Keyword to the specified value.
     * @param keywordPK the new keywordPK
     */
    public void setKeywordPK(KeywordPK keywordPK) {
        this.keywordPK = keywordPK;
    }
    
    /**
     * Gets the investigation of this Keyword.
     * @return the investigation
     */
    @XmlTransient
    public Investigation getInvestigation() {
        return this.investigation;
    }
    
    /**
     * Sets the investigation of this Keyword to the specified value.
     * @param investigation the new investigation
     */
    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
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
        hash += (this.keywordPK != null ? this.keywordPK.hashCode() : 0);
        return hash;
    }
    
    /**
     * Overrides the isValid function, checks each of the datafiles and datafile parameters are valid
     *
     * @throws ValidationException
     * @return
     */
    @Override
    public boolean isValid(EntityManager manager) throws ValidationException {
        if(keywordPK == null) throw new ValidationException(this +" private key cannot be null");
        return keywordPK.isValid();
        
    }
    
    /**
     * Determines whether another object is equal to this Keyword.  The result is
     * <code>true</code> if and only if the argument is not null and is a Keyword object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Keyword)) {
            return false;
        }
        Keyword other = (Keyword)object;
        if (this.keywordPK != other.keywordPK && (this.keywordPK == null || !this.keywordPK.equals(other.keywordPK))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Keyword[keywordPK=" + keywordPK + "]";
    }
    
}
