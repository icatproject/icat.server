package uk.icat3.logging.entity;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
  @NamedQuery(name = "Logical.findAll", query = "select o from Logical o")
})
public class Logical implements Serializable {
    @Id
    @Column(name="LOGICAL_ID", nullable = false)
    private Long logicalId;
    @ManyToOne
    @JoinColumn(name = "RH_WHERE_ID")
    private SimpleWhere simpleWhere;
    @OneToMany(mappedBy = "logical")
    private List<SimpleWhere> simpleWhereList;
    @ManyToOne
    @JoinColumn(name = "LH_WHERE_ID")
    private SimpleWhere simpleWhere1;
    @ManyToOne
    @JoinColumn(name = "TYPE_ID")
    private Type type;

    public Logical() {
    }

    public Logical(SimpleWhere simpleWhere1, Long logicalId, SimpleWhere simpleWhere, Type type) {
        this.simpleWhere1 = simpleWhere1;
        this.logicalId = logicalId;
        this.simpleWhere = simpleWhere;
        this.type = type;
    }


    public Long getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(Long logicalId) {
        this.logicalId = logicalId;
    }


    public SimpleWhere getSimpleWhere() {
        return simpleWhere;
    }

    public void setSimpleWhere(SimpleWhere simpleWhere) {
        this.simpleWhere = simpleWhere;
    }

    public List<SimpleWhere> getSimpleWhereList() {
        return simpleWhereList;
    }

    public void setSimpleWhereList(List<SimpleWhere> simpleWhereList) {
        this.simpleWhereList = simpleWhereList;
    }

    public SimpleWhere addSimpleWhere(SimpleWhere simpleWhere) {
        getSimpleWhereList().add(simpleWhere);
        simpleWhere.setLogical(this);
        return simpleWhere;
    }

    public SimpleWhere removeSimpleWhere(SimpleWhere simpleWhere) {
        getSimpleWhereList().remove(simpleWhere);
        simpleWhere.setLogical(null);
        return simpleWhere;
    }

    public SimpleWhere getSimpleWhere1() {
        return simpleWhere1;
    }

    public void setSimpleWhere1(SimpleWhere simpleWhere1) {
        this.simpleWhere1 = simpleWhere1;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
