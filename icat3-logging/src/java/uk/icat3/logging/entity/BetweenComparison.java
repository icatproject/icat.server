package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "BetweenComparison.findAll", query = "select o from BetweenComparison o")
})
@Table(name = "BETWEEN_COMPARISON")
public class BetweenComparison implements Serializable {
    private String attribute;
    @Id
    @Column(name="BETWEEN_ID", nullable = false)
    private Long betweenId;
    @ManyToOne
    @JoinColumn(name = "RH_OBJECT_ID")
    private CompObject compObject;
    @ManyToOne
    @JoinColumn(name = "LH_OBJECT_ID")
    private CompObject compObject1;
    @ManyToOne
    @JoinColumn(name = "GENERIC_ID")
    private Generic generic;

    public BetweenComparison() {
    }

    public BetweenComparison(String attribute, Long betweenId, Generic generic,
                             CompObject compObject1, CompObject compObject) {
        this.attribute = attribute;
        this.betweenId = betweenId;
        this.generic = generic;
        this.compObject1 = compObject1;
        this.compObject = compObject;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Long getBetweenId() {
        return betweenId;
    }

    public void setBetweenId(Long betweenId) {
        this.betweenId = betweenId;
    }


    public CompObject getCompObject() {
        return compObject;
    }

    public void setCompObject(CompObject compObject) {
        this.compObject = compObject;
    }

    public CompObject getCompObject1() {
        return compObject1;
    }

    public void setCompObject1(CompObject compObject1) {
        this.compObject1 = compObject1;
    }

    public Generic getGeneric() {
        return generic;
    }

    public void setGeneric(Generic generic) {
        this.generic = generic;
    }
}
