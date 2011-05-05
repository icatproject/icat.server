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
  @NamedQuery(name = "InvInclude.findAll", query = "select o from InvInclude o")
})
@Table(name = "INV_INCLUDE")
public class InvInclude implements Serializable {
    @Id
    @Column(nullable = false)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "invInclude1")
    private List<AdvancedSearch> advancedSearchList;
    @OneToMany(mappedBy = "invInclude")
    private List<InvestigationView> investigationViewList;

    public InvInclude() {
    }

    public InvInclude(Long id, String name) {
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

    public List<AdvancedSearch> getAdvancedSearchList() {
        return advancedSearchList;
    }

    public void setAdvancedSearchList(List<AdvancedSearch> advancedSearchList) {
        this.advancedSearchList = advancedSearchList;
    }

    public AdvancedSearch addAdvancedSearch(AdvancedSearch advancedSearch) {
        getAdvancedSearchList().add(advancedSearch);
        advancedSearch.setInvInclude1(this);
        return advancedSearch;
    }

    public AdvancedSearch removeAdvancedSearch(AdvancedSearch advancedSearch) {
        getAdvancedSearchList().remove(advancedSearch);
        advancedSearch.setInvInclude1(null);
        return advancedSearch;
    }

    public List<InvestigationView> getInvestigationViewList() {
        return investigationViewList;
    }

    public void setInvestigationViewList(List<InvestigationView> investigationViewList) {
        this.investigationViewList = investigationViewList;
    }

    public InvestigationView addInvestigationView(InvestigationView investigationView) {
        getInvestigationViewList().add(investigationView);
        investigationView.setInvInclude(this);
        return investigationView;
    }

    public InvestigationView removeInvestigationView(InvestigationView investigationView) {
        getInvestigationViewList().remove(investigationView);
        investigationView.setInvInclude(null);
        return investigationView;
    }
}
