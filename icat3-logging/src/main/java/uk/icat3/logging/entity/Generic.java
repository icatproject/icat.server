package uk.icat3.logging.entity;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
  @NamedQuery(name = "Generic.findAll", query = "select o from Generic o")
})
public class Generic implements Serializable {
    @Id
    @Column(name="GENERIC_ID", nullable = false)
    private Long genericId;
    @OneToMany(mappedBy = "generic")
    private List<BetweenComparison> betweenComparisonList;
    @OneToMany(mappedBy = "generic")
    private List<SimpleWhere> simpleWhereList;
    @OneToMany(mappedBy = "generic")
    private List<Comparison> comparisonList;

    public Generic() {
    }

    public Generic(Long genericId) {
        this.genericId = genericId;
    }

    public Long getGenericId() {
        return genericId;
    }

    public void setGenericId(Long genericId) {
        this.genericId = genericId;
    }

    public List<BetweenComparison> getBetweenComparisonList() {
        return betweenComparisonList;
    }

    public void setBetweenComparisonList(List<BetweenComparison> betweenComparisonList) {
        this.betweenComparisonList = betweenComparisonList;
    }

    public BetweenComparison addBetweenComparison(BetweenComparison betweenComparison) {
        getBetweenComparisonList().add(betweenComparison);
        betweenComparison.setGeneric(this);
        return betweenComparison;
    }

    public BetweenComparison removeBetweenComparison(BetweenComparison betweenComparison) {
        getBetweenComparisonList().remove(betweenComparison);
        betweenComparison.setGeneric(null);
        return betweenComparison;
    }

    public List<SimpleWhere> getSimpleWhereList() {
        return simpleWhereList;
    }

    public void setSimpleWhereList(List<SimpleWhere> simpleWhereList) {
        this.simpleWhereList = simpleWhereList;
    }

    public SimpleWhere addSimpleWhere(SimpleWhere simpleWhere) {
        getSimpleWhereList().add(simpleWhere);
        simpleWhere.setGeneric(this);
        return simpleWhere;
    }

    public SimpleWhere removeSimpleWhere(SimpleWhere simpleWhere) {
        getSimpleWhereList().remove(simpleWhere);
        simpleWhere.setGeneric(null);
        return simpleWhere;
    }

    public List<Comparison> getComparisonList() {
        return comparisonList;
    }

    public void setComparisonList(List<Comparison> comparisonList) {
        this.comparisonList = comparisonList;
    }

    public Comparison addComparison(Comparison comparison) {
        getComparisonList().add(comparison);
        comparison.setGeneric(this);
        return comparison;
    }

    public Comparison removeComparison(Comparison comparison) {
        getComparisonList().remove(comparison);
        comparison.setGeneric(null);
        return comparison;
    }
}
