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
  @NamedQuery(name = "LogInvestigator.findAll", query = "select o from LogInvestigator o")
})
@Table(name = "LOG_INVESTIGATOR")
public class LogInvestigator implements Serializable {
        @TableGenerator(name = "INVESTIGATOR_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "INVESTIGATOR_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "INVESTIGATOR_GENERATOR")
    @Column(nullable = false)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "logInvestigator")
    private List<AdvInvestigator> advInvestigatorList;

    public LogInvestigator() {
    }

    public LogInvestigator(Long id, String name) {
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

    public List<AdvInvestigator> getAdvInvestigatorList() {
        return advInvestigatorList;
    }

    public void setAdvInvestigatorList(List<AdvInvestigator> advInvestigatorList) {
        this.advInvestigatorList = advInvestigatorList;
    }

    public AdvInvestigator addAdvInvestigator(AdvInvestigator advInvestigator) {
        getAdvInvestigatorList().add(advInvestigator);
        advInvestigator.setLogInvestigator(this);
        return advInvestigator;
    }

    public AdvInvestigator removeAdvInvestigator(AdvInvestigator advInvestigator) {
        getAdvInvestigatorList().remove(advInvestigator);
        advInvestigator.setLogInvestigator(null);
        return advInvestigator;
    }
}
