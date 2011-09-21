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
  @NamedQuery(name = "AdvInstrument.findAll", query = "select o from AdvInstrument o")
})
@Table(name = "ADV_INSTRUMENT")
@IdClass(AdvInstrumentPK.class)
public class AdvInstrument implements Serializable {
    @Id
    @Column(name="ADV_ID", nullable = false, insertable = false, updatable = false)
    private Long advId;
    @Id
    @Column(name="INSTRUMENT_ID", nullable = false, insertable = false,
            updatable = false)
    private Long instrumentId;
    @ManyToOne
    @JoinColumn(name = "INSTRUMENT_ID")
    private LogInstrument logInstrument;
    @ManyToOne
    @JoinColumn(name = "ADV_ID")
    private AdvancedSearch advancedSearch;

    public AdvInstrument() {
    }

    public AdvInstrument(AdvancedSearch advancedSearch, LogInstrument logInstrument) {
        this.advancedSearch = advancedSearch;
        this.logInstrument = logInstrument;
    }

    public Long getAdvId() {
        return advId;
    }

    public void setAdvId(Long advId) {
        this.advId = advId;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public LogInstrument getLogInstrument() {
        return logInstrument;
    }

    public void setLogInstrument(LogInstrument logInstrument) {
        this.logInstrument = logInstrument;
        if (logInstrument != null) {
            this.instrumentId = logInstrument.getInstrumentId();
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
