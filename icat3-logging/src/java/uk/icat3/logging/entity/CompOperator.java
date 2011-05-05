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
  @NamedQuery(name = "CompOperator.findAll", query = "select o from CompOperator o")
})
@Table(name = "COMP_OPERATOR")
public class CompOperator implements Serializable {
    @Id
    @Column(nullable = false)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "compOperator")
    private List<Comparison> comparisonList;

    public CompOperator() {
    }

    public CompOperator(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Comparison> getComparisonList() {
        return comparisonList;
    }

    public void setComparisonList(List<Comparison> comparisonList) {
        this.comparisonList = comparisonList;
    }

    public Comparison addComparison(Comparison comparison) {
        getComparisonList().add(comparison);
        comparison.setCompOperator(this);
        return comparison;
    }

    public Comparison removeComparison(Comparison comparison) {
        getComparisonList().remove(comparison);
        comparison.setCompOperator(null);
        return comparison;
    }
}
