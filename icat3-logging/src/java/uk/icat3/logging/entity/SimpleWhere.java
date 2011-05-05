package uk.icat3.logging.entity;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@NamedQueries({
  @NamedQuery(name = "SimpleWhere.findAll", query = "select o from SimpleWhere o")
})
@Table(name = "SIMPLE_WHERE")
public class SimpleWhere implements Serializable {

    @Id
    @Column(name="WHERE_ID", nullable = false)
    private Long whereId;
    @OneToMany(mappedBy = "simpleWhere")
    private List<Logical> logicalList;
    @ManyToOne
    @JoinColumn(name = "LOGICAL_ID")
    private Logical logical;
    @OneToMany(mappedBy = "simpleWhere")
    private List<ParamSearch> paramSearchList;
    @ManyToOne
    @JoinColumn(name = "GENERIC_ID")
    private Generic generic;
    @OneToMany(mappedBy = "simpleWhere1")
    private List<Logical> logicalList1;

    public SimpleWhere() {
    }

    public SimpleWhere(Generic generic, Logical logical, Long whereId) {
        this.generic = generic;
        this.logical = logical;
        this.whereId = whereId;
    }


    public Long getWhereId() {
        return whereId;
    }

    public void setWhereId(Long whereId) {
        this.whereId = whereId;
    }

    public List<Logical> getLogicalList() {
        return logicalList;
    }

    public void setLogicalList(List<Logical> logicalList) {
        this.logicalList = logicalList;
    }

    public Logical addLogical(Logical logical) {
        getLogicalList().add(logical);
        logical.setSimpleWhere(this);
        return logical;
    }

    public Logical removeLogical(Logical logical) {
        getLogicalList().remove(logical);
        logical.setSimpleWhere(null);
        return logical;
    }

    public Logical getLogical() {
        return logical;
    }

    public void setLogical(Logical logical) {
        this.logical = logical;
    }

    public List<ParamSearch> getParamSearchList() {
        return paramSearchList;
    }

    public void setParamSearchList(List<ParamSearch> paramSearchList) {
        this.paramSearchList = paramSearchList;
    }

    public ParamSearch addParamSearch(ParamSearch paramSearch) {
        getParamSearchList().add(paramSearch);
        paramSearch.setSimpleWhere(this);
        return paramSearch;
    }

    public ParamSearch removeParamSearch(ParamSearch paramSearch) {
        getParamSearchList().remove(paramSearch);
        paramSearch.setSimpleWhere(null);
        return paramSearch;
    }

    public Generic getGeneric() {
        return generic;
    }

    public void setGeneric(Generic generic) {
        this.generic = generic;
    }

    public List<Logical> getLogicalList1() {
        return logicalList1;
    }

    public void setLogicalList1(List<Logical> logicalList1) {
        this.logicalList1 = logicalList1;
    }

    public Logical addLogical1(Logical logical) {
        getLogicalList1().add(logical);
        logical.setSimpleWhere1(this);
        return logical;
    }

    public Logical removeLogical1(Logical logical) {
        getLogicalList1().remove(logical);
        logical.setSimpleWhere1(null);
        return logical;
    }
}
