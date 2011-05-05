package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
  @NamedQuery(name = "Comparison.findAll", query = "select o from Comparison o")
})
public class Comparison implements Serializable {
    private String attribute;
    @Id
    @Column(name="COMP_ID", nullable = false)
    private Long compId;
    @ManyToOne
    @JoinColumn(name = "OBJECT_ID")
    private CompObject compObject;
    @ManyToOne
    @JoinColumn(name = "GENERIC_ID")
    private Generic generic;
    @ManyToOne
    @JoinColumn(name = "OPERATOR_ID")
    private CompOperator compOperator;

    public Comparison() {
    }

    public Comparison(String attribute, Long compId, Generic generic,
                      CompObject compObject, CompOperator compOperator) {
        this.attribute = attribute;
        this.compId = compId;
        this.generic = generic;
        this.compObject = compObject;
        this.compOperator = compOperator;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Long getCompId() {
        return compId;
    }

    public void setCompId(Long compId) {
        this.compId = compId;
    }


    public CompObject getCompObject() {
        return compObject;
    }

    public void setCompObject(CompObject compObject) {
        this.compObject = compObject;
    }

    public Generic getGeneric() {
        return generic;
    }

    public void setGeneric(Generic generic) {
        this.generic = generic;
    }

    public CompOperator getCompOperator() {
        return compOperator;
    }

    public void setCompOperator(CompOperator compOperator) {
        this.compOperator = compOperator;
    }
}
