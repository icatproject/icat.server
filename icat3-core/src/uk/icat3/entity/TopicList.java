/*
 * TopicList.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;

/**
 * Entity class TopicList
 *
 * @author gjd37
 */
@Entity
@Table(name = "TOPIC_LIST")
@NamedQueries( {
    @NamedQuery(name = "TopicList.findByInvestigationId", query = "SELECT t FROM TopicList t WHERE t.topicListPK.investigationId = :investigationId"),
    @NamedQuery(name = "TopicList.findByTopicId", query = "SELECT t FROM TopicList t WHERE t.topicListPK.topicId = :topicId"),
    @NamedQuery(name = "TopicList.findByModTime", query = "SELECT t FROM TopicList t WHERE t.modTime = :modTime"),
    @NamedQuery(name = "TopicList.findByModId", query = "SELECT t FROM TopicList t WHERE t.modId = :modId")
})
public class TopicList extends EntityBaseBean implements Serializable {
    
    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected TopicListPK topicListPK;
     
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private Investigation investigation;
    
    @JoinColumn(name = "TOPIC_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    @XmlTransient
    private Topic topic;
    
    /** Creates a new instance of TopicList */
    public TopicList() {
    }
    
    /**
     * Creates a new instance of TopicList with the specified values.
     * @param topicListPK the topicListPK of the TopicList
     */
    public TopicList(TopicListPK topicListPK) {
        this.topicListPK = topicListPK;
    }
    
    /**
     * Creates a new instance of TopicList with the specified values.
     * @param topicListPK the topicListPK of the TopicList
     * @param modId the modId of the TopicList
     */
    public TopicList(TopicListPK topicListPK, String modId) {
        this.topicListPK = topicListPK;
        this.modId = modId;
    }
    
    /**
     * Creates a new instance of TopicListPK with the specified values.
     * @param topicId the topicId of the TopicListPK
     * @param investigationId the investigationId of the TopicListPK
     */
    public TopicList(Long topicId, Long investigationId) {
        this.topicListPK = new TopicListPK(topicId, investigationId);
    }
    
    /**
     * Gets the topicListPK of this TopicList.
     * @return the topicListPK
     */
    public TopicListPK getTopicListPK() {
        return this.topicListPK;
    }
    
    /**
     * Sets the topicListPK of this TopicList to the specified value.
     * @param topicListPK the new topicListPK
     */
    public void setTopicListPK(TopicListPK topicListPK) {
        this.topicListPK = topicListPK;
    }
       
    
    /**
     * Gets the investigation of this TopicList.
     * @return the investigation
     */
    @XmlTransient
    public Investigation getInvestigation() {
        return this.investigation;
    }
    
    /**
     * Sets the investigation of this TopicList to the specified value.
     * @param investigation the new investigation
     */
    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }
    
    /**
     * Gets the topic of this TopicList.
     * @return the topic
     */
    @XmlTransient
    public Topic getTopic() {
        return this.topic;
    }
    
    /**
     * Sets the topic of this TopicList to the specified value.
     * @param topic the new topic
     */
    public void setTopic(Topic topic) {
        this.topic = topic;
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
        hash += (this.topicListPK != null ? this.topicListPK.hashCode() : 0);
        return hash;
    }
    
    /**
     * Determines whether another object is equal to this TopicList.  The result is
     * <code>true</code> if and only if the argument is not null and is a TopicList object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TopicList)) {
            return false;
        }
        TopicList other = (TopicList)object;
        if (this.topicListPK != other.topicListPK && (this.topicListPK == null || !this.topicListPK.equals(other.topicListPK))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "TopicList[topicListPK=" + topicListPK + "]";
    }
    
}
