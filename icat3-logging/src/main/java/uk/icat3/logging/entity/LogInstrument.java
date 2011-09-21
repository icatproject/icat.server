package uk.icat3.logging.entity;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@NamedQueries({
  @NamedQuery(name = "LogInstrument.findAll", query = "select o from LogInstrument o")
})
@Table(name = "LOG_INSTRUMENT")
public class LogInstrument implements Serializable {
            @TableGenerator(name = "INSTRUMENT_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "INSTRUMENT_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "INSTRUMENT_GENERATOR")
    @Column(name="INSTRUMENT_ID", nullable = false)
    private Long instrumentId;
    private String name;
    private String type;
    @OneToMany(mappedBy = "logInstrument")
    private List<AdvInstrument> advInstrumentList;

    public LogInstrument() {
    }

    public LogInstrument(Long instrumentId, String name, String type) {
        this.instrumentId = instrumentId;
        this.name = name;
        this.type = type;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AdvInstrument> getAdvInstrumentList() {
        return advInstrumentList;
    }

    public void setAdvInstrumentList(List<AdvInstrument> advInstrumentList) {
        this.advInstrumentList = advInstrumentList;
    }

    public AdvInstrument addAdvInstrument(AdvInstrument advInstrument) {
        getAdvInstrumentList().add(advInstrument);
        advInstrument.setLogInstrument(this);
        return advInstrument;
    }

    public AdvInstrument removeAdvInstrument(AdvInstrument advInstrument) {
        getAdvInstrumentList().remove(advInstrument);
        advInstrument.setLogInstrument(null);
        return advInstrument;
    }
}
