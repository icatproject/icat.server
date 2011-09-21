package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "AdvInvestigator.findAll", query = "select o from AdvInvestigator o")
})
@Table(name = "ADV_INVESTIGATOR")
@IdClass(AdvInvestigatorPK.class)
public class AdvInvestigator implements Serializable {
    @Id
    @Column(name="ADV_ID", nullable = false, insertable = false, updatable = false)
    private Long advId;
    @Id
    @Column(name="INVESTIGATOR_ID", nullable = false, insertable = false,
            updatable = false)
    private Long investigatorId;
    @ManyToOne
    @JoinColumn(name = "INVESTIGATOR_ID")
    private LogInvestigator logInvestigator;
    @ManyToOne
    @JoinColumn(name = "ADV_ID")
    private AdvancedSearch advancedSearch;

    public AdvInvestigator() {
    }

    public AdvInvestigator(AdvancedSearch advancedSearch, LogInvestigator logInvestigator) {
        this.advancedSearch = advancedSearch;
        this.logInvestigator = logInvestigator;
    }

    public Long getAdvId() {
        return advId;
    }

    public void setAdvId(Long advId) {
        this.advId = advId;
    }

    public Long getInvestigatorId() {
        return investigatorId;
    }

    public void setInvestigatorId(Long investigatorId) {
        this.investigatorId = investigatorId;
    }

    public LogInvestigator getLogInvestigator() {
        return logInvestigator;
    }

    public void setLogInvestigator(LogInvestigator logInvestigator) {
        this.logInvestigator = logInvestigator;
        if (logInvestigator != null) {
            this.investigatorId = logInvestigator.getId();
        }
    }

    public AdvancedSearch getAdvancedSearch() {
        return advancedSearch;
    }

    public void setAdvancedSearch(AdvancedSearch advancedSearch) {
        this.advancedSearch = advancedSearch;
        if (advancedSearch != null) {
            this.advId = advancedSearch.getAdvId();
        }
    }
}
