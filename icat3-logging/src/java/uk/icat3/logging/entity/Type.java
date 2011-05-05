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
  @NamedQuery(name = "Type.findAll", query = "select o from Type o")
})
public class Type implements Serializable {
    @Id
    @Column(nullable = false)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "type")
    private List<Logical> logicalList;

    public Type() {
    }

    public Type(Long id, String name) {
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

    public List<Logical> getLogicalList() {
        return logicalList;
    }

    public void setLogicalList(List<Logical> logicalList) {
        this.logicalList = logicalList;
    }

    public Logical addLogical(Logical logical) {
        getLogicalList().add(logical);
        logical.setType(this);
        return logical;
    }

    public Logical removeLogical(Logical logical) {
        getLogicalList().remove(logical);
        logical.setType(null);
        return logical;
    }
}
