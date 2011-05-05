package uk.icat3.logging.entity;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "CompObject.findAll", query = "select o from CompObject o")
})
@Table(name = "COMP_OBJECT")
public class CompObject implements Serializable {
    @Column(name="BOOL_VAL", length = 20)
    private String boolVal;
    @Column(name="NUM_VAL")
    private Long numVal;
    @Id
    @Column(name="OBJECT_ID", nullable = false)
    private Long objectId;
    @Column(name="STRING_VAL")
    private String stringVal;
    @OneToMany(mappedBy = "compObject")
    private List<Comparison> comparisonList;
    @OneToMany(mappedBy = "compObject")
    private List<BetweenComparison> betweenComparisonList;
    @OneToMany(mappedBy = "compObject1")
    private List<BetweenComparison> betweenComparisonList1;

    public CompObject() {
    }

    public CompObject(String boolVal, Long numVal, Long objectId,
                      String stringVal) {
        this.boolVal = boolVal;
        this.numVal = numVal;
        this.objectId = objectId;
        this.stringVal = stringVal;
    }

    public String getBoolVal() {
        return boolVal;
    }

    public void setBoolVal(String boolVal) {
        this.boolVal = boolVal;
    }

    public Long getNumVal() {
        return numVal;
    }

    public void setNumVal(Long numVal) {
        this.numVal = numVal;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public List<Comparison> getComparisonList() {
        return comparisonList;
    }

    public void setComparisonList(List<Comparison> comparisonList) {
        this.comparisonList = comparisonList;
    }

    public Comparison addComparison(Comparison comparison) {
        getComparisonList().add(comparison);
        comparison.setCompObject(this);
        return comparison;
    }

    public Comparison removeComparison(Comparison comparison) {
        getComparisonList().remove(comparison);
        comparison.setCompObject(null);
        return comparison;
    }

    public List<BetweenComparison> getBetweenComparisonList() {
        return betweenComparisonList;
    }

    public void setBetweenComparisonList(List<BetweenComparison> betweenComparisonList) {
        this.betweenComparisonList = betweenComparisonList;
    }

    public BetweenComparison addBetweenComparison(BetweenComparison betweenComparison) {
        getBetweenComparisonList().add(betweenComparison);
        betweenComparison.setCompObject(this);
        return betweenComparison;
    }

    public BetweenComparison removeBetweenComparison(BetweenComparison betweenComparison) {
        getBetweenComparisonList().remove(betweenComparison);
        betweenComparison.setCompObject(null);
        return betweenComparison;
    }

    public List<BetweenComparison> getBetweenComparisonList1() {
        return betweenComparisonList1;
    }

    public void setBetweenComparisonList1(List<BetweenComparison> betweenComparisonList1) {
        this.betweenComparisonList1 = betweenComparisonList1;
    }

    public BetweenComparison addBetweenComparison1(BetweenComparison betweenComparison) {
        getBetweenComparisonList1().add(betweenComparison);
        betweenComparison.setCompObject1(this);
        return betweenComparison;
    }

    public BetweenComparison removeBetweenComparison1(BetweenComparison betweenComparison) {
        getBetweenComparisonList1().remove(betweenComparison);
        betweenComparison.setCompObject1(null);
        return betweenComparison;
    }
}
