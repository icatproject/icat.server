/*
 * Topic.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import uk.icat3.util.ElementType;

/**
 * Entity class Topic
 * 
 * @author gjd37
 */
@Entity
@Table(name = "TOPIC")
@NamedQueries( {
        @NamedQuery(name = "Topic.findById", query = "SELECT t FROM Topic t WHERE t.id = :id"),
        @NamedQuery(name = "Topic.findByName", query = "SELECT t FROM Topic t WHERE t.name = :name"),
        @NamedQuery(name = "Topic.findByParentId", query = "SELECT t FROM Topic t WHERE t.parentId = :parentId"),
        @NamedQuery(name = "Topic.findByTopicLevel", query = "SELECT t FROM Topic t WHERE t.topicLevel = :topicLevel"),
        @NamedQuery(name = "Topic.findByModTime", query = "SELECT t FROM Topic t WHERE t.modTime = :modTime"),
        @NamedQuery(name = "Topic.findByModId", query = "SELECT t FROM Topic t WHERE t.modId = :modId")
    })
public class Topic extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "TOPIC_LEVEL")
    private Long topicLevel;
 
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "topic")
    private Collection<TopicList> topicListCollection;
    
    /** Creates a new instance of Topic */
    public Topic() {
    }

    /**
     * Creates a new instance of Topic with the specified values.
     * @param id the id of the Topic
     */
    public Topic(Long id) {
        this.id = id;
    }

    /**
     * Creates a new instance of Topic with the specified values.
     * @param id the id of the Topic
     * @param modTime the modTime of the Topic
     * @param modId the modId of the Topic
     */
    public Topic(Long id, Date modTime, String modId) {
        this.id = id;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the id of this Topic.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id of this Topic to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of this Topic.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this Topic to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the parentId of this Topic.
     * @return the parentId
     */
    public Long getParentId() {
        return this.parentId;
    }

    /**
     * Sets the parentId of this Topic to the specified value.
     * @param parentId the new parentId
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * Gets the topicLevel of this Topic.
     * @return the topicLevel
     */
    public Long getTopicLevel() {
        return this.topicLevel;
    }

    /**
     * Sets the topicLevel of this Topic to the specified value.
     * @param topicLevel the new topicLevel
     */
    public void setTopicLevel(Long topicLevel) {
        this.topicLevel = topicLevel;
    }

    /**
     * Gets the topicListCollection of this Topic.
     * @return the topicListCollection
     */
    public Collection<TopicList> getTopicListCollection() {
        return this.topicListCollection;
    }

    /**
     * Sets the topicListCollection of this Topic to the specified value.
     * @param topicListCollection the new topicListCollection
     */
    public void setTopicListCollection(Collection<TopicList> topicListCollection) {
        this.topicListCollection = topicListCollection;
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
     * Determines whether another object is equal to this Topic.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Topic object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Topic)) {
            return false;
        }
        Topic other = (Topic)object;
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
        return "Topic[id=" + id + "]";
    }
    
}
