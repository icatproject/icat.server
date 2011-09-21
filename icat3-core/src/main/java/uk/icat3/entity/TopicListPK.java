/*
 * TopicListPK.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary Key class TopicListPK for entity class TopicList
 * 
 * @author gjd37
 */
@Embeddable
public class TopicListPK  extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "INVESTIGATION_ID", nullable = false)
    private Long investigationId;

    @Column(name = "TOPIC_ID", nullable = false)
    private Long topicId;
    
    /** Creates a new instance of TopicListPK */
    public TopicListPK() {
    }

    /**
     * Creates a new instance of TopicListPK with the specified values.
     * @param topicId the topicId of the TopicListPK
     * @param investigationId the investigationId of the TopicListPK
     */
    public TopicListPK(Long topicId, Long investigationId) {
        this.topicId = topicId;
        this.investigationId = investigationId;
    }

    /**
     * Gets the investigationId of this TopicListPK.
     * @return the investigationId
     */
    public Long getInvestigationId() {
        return this.investigationId;
    }

    /**
     * Sets the investigationId of this TopicListPK to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    /**
     * Gets the topicId of this TopicListPK.
     * @return the topicId
     */
    public Long getTopicId() {
        return this.topicId;
    }

    /**
     * Sets the topicId of this TopicListPK to the specified value.
     * @param topicId the new topicId
     */
    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.topicId != null ? this.topicId.hashCode() : 0);
        hash += (this.investigationId != null ? this.investigationId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this TopicListPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a TopicListPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TopicListPK)) {
            return false;
        }
        TopicListPK other = (TopicListPK)object;
        if (this.topicId != other.topicId && (this.topicId == null || !this.topicId.equals(other.topicId))) return false;
        if (this.investigationId != other.investigationId && (this.investigationId == null || !this.investigationId.equals(other.investigationId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "TopicListPK[topicId=" + topicId + ", investigationId=" + investigationId + "]";
    }
    
}
