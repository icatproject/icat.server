package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Subject, keyword, classification code, or key phrase describing a data publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "NAME" }) })
public class Subject extends EntityBaseBean implements Serializable {

    @Comment("The data publication this subject is associated with")
    @JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DataPublication dataPublication;

    @Comment("The name of the subject or keyword")
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Comment("Persistent identifier for the subject")
    private String pid;
    
    @Comment("Name of the subject scheme or controlled vocabulary")
    private String subjectScheme;
    
    @Comment("URI of the subject scheme or controlled vocabulary")
    private String schemeURI;
    
    @Comment("URI for the specific subject term")
    private String valueURI;
    
    @Comment("Classification code for the subject")
    private String classificationCode;
    
    /* Needed for JPA */
    public Subject() {
    }

    public DataPublication getDataPublication() {
        return dataPublication;
    }
    
    public String getName() {
        return name;
    }

    public String getPid() {
        return pid;
    }

    public String getSubjectScheme() {
        return subjectScheme;
    }

    public String getSchemeURI() {
        return schemeURI;
    }

    public String getValueURI() {
        return valueURI;
    }

    public String getClassificationCode() {
        return classificationCode;
    }

    public void setDataPublication(DataPublication dataPublication) {
        this.dataPublication = dataPublication;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPid(String pid) {
        this.pid = pid;
    }
    
    public void setSubjectScheme(String subjectScheme) {
        this.subjectScheme = subjectScheme;
    }
    
    public void setSchemeURI(String schemeURI) {
        this.schemeURI = schemeURI;
    }
    
    public void setValueURI(String valueURI) {
        this.valueURI = valueURI;
    }
    
    public void setClassificationCode(String classificationCode) {
        this.classificationCode = classificationCode;
    }
}
