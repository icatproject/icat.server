package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@NamedQueries({
  @NamedQuery(name = "InvestigationView.findAll", query = "select o from InvestigationView o")
})
@Table(name = "INVESTIGATION_VIEW")
public class InvestigationView implements Serializable {
        @TableGenerator(name = "INVESTIGATIONVIEW_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "INVESTIGATION_VIEW_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "INVESTIGATIONVIEW_GENERATOR")
    @Column(nullable = false)
    private Long id;
    private String instrument;
    @Column(name="INVESTIGATION_ID")
    private Long investigationId;
    @Column(name="NO_RESULTS")
    private Long noResults;
    @Column(name="PI_ID")
    private Long piId;
    @Column(name="START_INDEX")
    private Long startIndex;
    @ManyToOne
    @JoinColumn(name = "INCLUDES")
    private InvInclude invInclude;
    @ManyToOne
    @JoinColumn(name = "VIEW_ID")
    private SimpleView simpleView;

    public InvestigationView() {
    }

    public InvestigationView(Long id, InvInclude invInclude, String instrument,
                             Long investigationId, Long noResults, Long piId,
                             Long startIndex, SimpleView simpleView) {
        this.id = id;
        this.invInclude = invInclude;
        this.instrument = instrument;
        this.investigationId = investigationId;
        this.noResults = noResults;
        this.piId = piId;
        this.startIndex = startIndex;
        this.simpleView = simpleView;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    public Long getNoResults() {
        return noResults;
    }

    public void setNoResults(Long noResults) {
        this.noResults = noResults;
    }

    public Long getPiId() {
        return piId;
    }

    public void setPiId(Long piId) {
        this.piId = piId;
    }

    public Long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Long startIndex) {
        this.startIndex = startIndex;
    }


    public InvInclude getInvInclude() {
        return invInclude;
    }

    public void setInvInclude(InvInclude invInclude) {
        this.invInclude = invInclude;
    }

    public SimpleView getSimpleView() {
        return simpleView;
    }

    public void setSimpleView(SimpleView simpleView) {
        this.simpleView = simpleView;
    }
}
